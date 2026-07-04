package com.nichefinder.obd2

import com.nichefinder.obd2.dtc.Dtc
import com.nichefinder.obd2.dtc.DtcParser
import com.nichefinder.obd2.elm.Elm327
import com.nichefinder.obd2.pid.Pid
import com.nichefinder.obd2.pid.PidSupport
import com.nichefinder.obd2.pid.PidValue
import com.nichefinder.obd2.transport.ObdTransport
import com.nichefinder.obd2.transport.ObdTransportException

/**
 * High-level session: what the app layer talks to.
 * connect() -> discover() -> read gauges / codes.
 */
class ObdSession(transport: ObdTransport) {
    private val elm = Elm327(transport)

    var supportedPids: Set<Int> = emptySet()
        private set

    suspend fun connect() {
        elm.initialize()
        elm.probeVehicle()
    }

    val protocolLabel: String get() = elm.detectedProtocol.label

    /** Walk the PID-support bitmaps (0100, 0120, 0140...) to learn what the car offers. */
    suspend fun discover(): Set<Int> {
        val found = mutableSetOf<Int>()
        var base = 0x00
        while (base <= 0x60) {
            val resp = elm.request("01%02X".format(base))
            if (resp.isError()) break
            val chunk = PidSupport.decodeBitmap(base, resp.primaryData())
            found += chunk
            if ((base + 0x20) !in chunk) break  // next bitmap not supported
            base += 0x20
        }
        supportedPids = found
        return found
    }

    suspend fun read(pid: Pid): PidValue {
        val resp = elm.request(pid.command)
        if (resp.isError()) throw ObdTransportException("No data for ${pid.label}: ${resp.raw}")
        return pid.decode(resp.primaryData())
    }

    suspend fun readStoredCodes(): List<Dtc> {
        val resp = elm.request("03")
        if (resp.isError()) return emptyList()
        return resp.byEcu.values.flatMap { DtcParser.parse(it) }.distinct()
    }

    suspend fun readPendingCodes(): List<Dtc> {
        val resp = elm.request("07")
        if (resp.isError()) return emptyList()
        return resp.byEcu.values.flatMap { DtcParser.parse(it) }.distinct()
    }

    /** Mode 04: clears codes and the MIL. Destructive — the app must confirm with the user. */
    suspend fun clearCodes(): Boolean = !elm.request("04").isError()
}
