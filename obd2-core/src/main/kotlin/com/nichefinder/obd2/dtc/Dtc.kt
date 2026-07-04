package com.nichefinder.obd2.dtc

/**
 * Diagnostic trouble codes (SAE J2012 encoding). Each DTC is 2 bytes:
 * top 2 bits select the system letter, the rest are BCD-ish hex digits.
 */
data class Dtc(val code: String) {
    val system: String
        get() = when (code.first()) {
            'P' -> "Powertrain"
            'C' -> "Chassis"
            'B' -> "Body"
            'U' -> "Network"
            else -> "Unknown"
        }

    companion object {
        private val LETTERS = charArrayOf('P', 'C', 'B', 'U')

        fun fromBytes(a: Int, b: Int): Dtc {
            val letter = LETTERS[(a shr 6) and 0x03]
            val d1 = (a shr 4) and 0x03
            val d2 = a and 0x0F
            val d3 = (b shr 4) and 0x0F
            val d4 = b and 0x0F
            return Dtc("%c%d%X%X%X".format(letter, d1, d2, d3, d4))
        }
    }
}

/**
 * Parses Mode 03 (stored) / Mode 07 (pending) responses. Data bytes arrive as
 * [count?] then DTC byte pairs; a 0x0000 pair is padding, not a code.
 * On CAN the first data byte is the DTC count — detected by length parity.
 */
object DtcParser {
    fun parse(data: List<Int>): List<Dtc> {
        if (data.isEmpty()) return emptyList()
        // Odd length => leading count byte (CAN format "43 02 01 43 ...")
        val pairs = if (data.size % 2 == 1) data.drop(1) else data
        return pairs.chunked(2)
            .filter { it.size == 2 && !(it[0] == 0 && it[1] == 0) }
            .map { Dtc.fromBytes(it[0], it[1]) }
    }
}
