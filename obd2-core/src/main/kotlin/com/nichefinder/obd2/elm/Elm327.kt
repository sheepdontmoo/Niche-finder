package com.nichefinder.obd2.elm

import com.nichefinder.obd2.transport.ObdTransport
import com.nichefinder.obd2.transport.ObdTransportException

/**
 * Driver for ELM327 and compatible interpreters (including the ubiquitous
 * v1.5 clones and STN11xx/STN22xx chips). Owns adapter initialization,
 * protocol detection, and request/response hygiene.
 */
class Elm327(private val transport: ObdTransport) {

    var adapterId: String = ""
        private set
    var detectedProtocol: ObdProtocol = ObdProtocol.UNKNOWN
        private set

    /**
     * Standard init sequence. Tolerant of clone quirks: unknown AT commands
     * answer '?' and are treated as non-fatal.
     */
    suspend fun initialize() {
        adapterId = exchangeAt("ATZ")          // reset; echoes version banner e.g. "ELM327 v1.5"
        exchangeAt("ATE0")                     // echo off
        exchangeAt("ATL0")                     // linefeeds off
        exchangeAt("ATS0")                     // spaces off (halves payload size on clones)
        exchangeAt("ATH1")                     // headers on (needed for multi-ECU parsing)
        exchangeAt("ATSP0")                    // auto protocol
    }

    /** Confirm the car answers, then record which protocol the ELM negotiated. */
    suspend fun probeVehicle() {
        val reply = request("0100")
        if (reply.isError()) throw ObdTransportException("Vehicle not responding: ${reply.raw}")
        val dp = exchangeAt("ATDPN")           // e.g. "A6" = auto-detected ISO 15765-4 CAN 11/500
        detectedProtocol = ObdProtocol.fromDpn(dp)
    }

    suspend fun request(hexCommand: String): ObdResponse {
        val raw = transport.exchange(hexCommand)
        return ObdResponse.parse(hexCommand, raw)
    }

    private suspend fun exchangeAt(cmd: String): String = transport.exchange(cmd).trim()
}

enum class ObdProtocol(val dpnCode: Char, val label: String) {
    ISO_15765_CAN_11_500('6', "ISO 15765-4 CAN (11-bit, 500k)"),
    ISO_15765_CAN_29_500('7', "ISO 15765-4 CAN (29-bit, 500k)"),
    ISO_15765_CAN_11_250('8', "ISO 15765-4 CAN (11-bit, 250k)"),
    ISO_15765_CAN_29_250('9', "ISO 15765-4 CAN (29-bit, 250k)"),
    ISO_14230_KWP2000('5', "ISO 14230-4 KWP (fast init)"),
    ISO_9141_2('3', "ISO 9141-2"),
    SAE_J1850_PWM('1', "SAE J1850 PWM"),
    SAE_J1850_VPW('2', "SAE J1850 VPW"),
    UNKNOWN('0', "Unknown");

    companion object {
        /** ATDPN answers like "6" or "A6" (A prefix = auto-detected). */
        fun fromDpn(dpn: String): ObdProtocol {
            val code = dpn.trim().removePrefix("A").firstOrNull() ?: return UNKNOWN
            return entries.firstOrNull { it.dpnCode == code } ?: UNKNOWN
        }
    }
}

/**
 * A cleaned OBD response. Handles the error vocabulary real adapters emit and
 * normalizes multi-line/multi-ECU replies into per-ECU hex payloads.
 */
class ObdResponse private constructor(
    val command: String,
    val raw: String,
    /** ECU header (e.g. "7E8") -> contiguous data bytes, mode/PID echo stripped. */
    val byEcu: Map<String, List<Int>>,
) {
    fun isError(): Boolean = byEcu.isEmpty()

    /** Data bytes from the primary ECU (engine, usually 7E8), or empty when errored. */
    fun primaryData(): List<Int> = byEcu.entries.minByOrNull { it.key }?.value ?: emptyList()

    companion object {
        private val ERROR_MARKERS = listOf(
            "NO DATA", "CAN ERROR", "BUS INIT", "BUS ERROR", "UNABLE TO CONNECT",
            "STOPPED", "ERROR", "?", "FB ERROR", "DATA ERROR",
        )

        fun parse(command: String, rawResponse: String): ObdResponse {
            val lines = rawResponse
                .replace(">", "")
                .split('\r', '\n')
                .map { it.trim().replace(" ", "") }
                .filter { it.isNotEmpty() && it != "SEARCHING..." }

            if (lines.isEmpty() || lines.any { l -> ERROR_MARKERS.any { l.contains(it.replace(" ", "")) } }) {
                return ObdResponse(command, rawResponse, emptyMap())
            }

            val expectedEcho = positiveEcho(command)
            val byEcu = mutableMapOf<String, MutableList<Int>>()
            for (line in lines) {
                if (!line.all { it.isDigit() || it in 'A'..'F' || it in 'a'..'f' }) continue
                val (ecu, payload) = splitHeader(line)
                var hex = payload
                // ISO-TP framing on CAN: single frame "07E8 06 41 00 ..." after ATS0
                // shows as leading length nibbles or "1:" style ordering already stripped
                // by the adapter; here we only strip the positive-response echo.
                val echoIdx = hex.indexOf(expectedEcho)
                if (echoIdx >= 0) hex = hex.substring(echoIdx + expectedEcho.length)
                val bytes = hex.chunked(2).mapNotNull { it.toIntOrNull(16) }
                byEcu.getOrPut(ecu) { mutableListOf() }.addAll(bytes)
            }
            return ObdResponse(command, rawResponse, byEcu.filterValues { it.isNotEmpty() })
        }

        /** "0100" -> "4100"; "03" -> "43"; "0902" -> "4902". Mode + 0x40, PID echoed. */
        internal fun positiveEcho(command: String): String {
            val mode = command.substring(0, 2).toInt(16)
            val positive = "%02X".format(mode + 0x40)
            return positive + command.drop(2).take(2)
        }

        /** With ATH1 on 11-bit CAN, lines start with a 3-hex-digit header like 7E8. */
        private fun splitHeader(line: String): Pair<String, String> =
            if (line.length > 3 && (line.startsWith("7E") || line.startsWith("7e"))) {
                line.take(3).uppercase() to line.drop(3).let {
                    // after the header the first byte is the ISO-TP PCI (frame length); drop it
                    if (it.length >= 2) it.drop(2) else it
                }
            } else {
                "" to line
            }
    }
}
