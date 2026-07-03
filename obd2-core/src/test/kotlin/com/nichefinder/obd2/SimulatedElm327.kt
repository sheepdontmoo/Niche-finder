package com.nichefinder.obd2

import com.nichefinder.obd2.transport.ObdTransport

/**
 * Simulates a cheap ELM327 v1.5 clone on a warm 4-cylinder at idle-ish load:
 * responds with realistic CAN (ISO 15765-4, 11-bit) frames including headers,
 * a stored P0301 + P0420, and clone quirks (SEARCHING... noise, '?' for
 * unsupported AT commands).
 */
class SimulatedElm327 : ObdTransport {
    override var isConnected: Boolean = false
        private set

    val sentCommands = mutableListOf<String>()

    override suspend fun connect() { isConnected = true }
    override suspend fun disconnect() { isConnected = false }

    override suspend fun exchange(command: String): String {
        sentCommands += command
        return when (command.uppercase()) {
            "ATZ" -> "\r\rELM327 v1.5\r"
            "ATE0", "ATL0", "ATS0", "ATH1" -> "OK\r"
            "ATSP0" -> "OK\r"
            "ATDPN" -> "A6\r"
            // PID support bitmaps: 00-20 and 21-40 advertised, no 41-60
            "0100" -> "SEARCHING...\r7E8064100BE3FA813\r"
            "0120" -> "7E80641209005A009\r"    // bit for 0x40 NOT set -> discovery stops
            // live data
            "010C" -> "7E804410C1AF8\r"        // RPM: (0x1A*256+0xF8)/4 = 1726
            "010D" -> "7E803410D4B\r"          // speed: 0x4B = 75 km/h
            "0105" -> "7E80341057B\r"          // coolant: 0x7B-40 = 83 °C
            "0111" -> "7E80341112E\r"          // throttle: 0x2E*100/255 = 18.0%
            "0142" -> "7E80441423A2C\r"        // voltage: 0x3A2C/1000 = 14.892 V
            // codes: 2 stored (P0301, P0420), none pending
            "03" -> "7E8064302030104 20\r"
            "07" -> "NO DATA\r"
            "04" -> "7E8014 4\r"
            else -> "?\r"
        } + "\r>"
    }
}
