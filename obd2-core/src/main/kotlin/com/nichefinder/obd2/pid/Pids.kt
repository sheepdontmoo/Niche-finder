package com.nichefinder.obd2.pid

/**
 * SAE J1979 Mode 01 PID definitions with decode formulas.
 * Values decode from raw data bytes A, B, C, D per the standard.
 */
data class PidValue(val pid: Pid, val value: Double) {
    val display: String get() = "${pid.format(value)} ${pid.unit}"
}

enum class Pid(
    val code: Int,
    val label: String,
    val unit: String,
    val bytes: Int,
    private val decoder: (List<Int>) -> Double,
    private val decimals: Int = 0,
) {
    ENGINE_LOAD(0x04, "Engine load", "%", 1, { d -> d[0] * 100.0 / 255 }, 1),
    COOLANT_TEMP(0x05, "Coolant temperature", "°C", 1, { d -> d[0] - 40.0 }),
    SHORT_FUEL_TRIM_1(0x06, "Short term fuel trim B1", "%", 1, { d -> d[0] / 1.28 - 100 }, 1),
    LONG_FUEL_TRIM_1(0x07, "Long term fuel trim B1", "%", 1, { d -> d[0] / 1.28 - 100 }, 1),
    FUEL_PRESSURE(0x0A, "Fuel pressure", "kPa", 1, { d -> d[0] * 3.0 }),
    INTAKE_MAP(0x0B, "Intake manifold pressure", "kPa", 1, { d -> d[0].toDouble() }),
    RPM(0x0C, "Engine RPM", "rpm", 2, { d -> (d[0] * 256 + d[1]) / 4.0 }),
    SPEED(0x0D, "Vehicle speed", "km/h", 1, { d -> d[0].toDouble() }),
    TIMING_ADVANCE(0x0E, "Timing advance", "°", 1, { d -> d[0] / 2.0 - 64 }, 1),
    INTAKE_TEMP(0x0F, "Intake air temperature", "°C", 1, { d -> d[0] - 40.0 }),
    MAF(0x10, "MAF air flow", "g/s", 2, { d -> (d[0] * 256 + d[1]) / 100.0 }, 2),
    THROTTLE(0x11, "Throttle position", "%", 1, { d -> d[0] * 100.0 / 255 }, 1),
    RUNTIME(0x1F, "Run time since start", "s", 2, { d -> d[0] * 256.0 + d[1] }),
    FUEL_LEVEL(0x2F, "Fuel level", "%", 1, { d -> d[0] * 100.0 / 255 }, 1),
    DISTANCE_SINCE_CLEARED(0x31, "Distance since codes cleared", "km", 2, { d -> d[0] * 256.0 + d[1] }),
    CONTROL_MODULE_VOLTAGE(0x42, "Control module voltage", "V", 2, { d -> (d[0] * 256 + d[1]) / 1000.0 }, 2),
    AMBIENT_TEMP(0x46, "Ambient air temperature", "°C", 1, { d -> d[0] - 40.0 }),
    OIL_TEMP(0x5C, "Engine oil temperature", "°C", 1, { d -> d[0] - 40.0 });

    val command: String get() = "01%02X".format(code)

    fun decode(data: List<Int>): PidValue {
        require(data.size >= bytes) { "$label needs $bytes bytes, got ${data.size}" }
        return PidValue(this, decoder(data.take(bytes)))
    }

    fun format(value: Double): String =
        if (decimals == 0) value.toLong().toString() else "%.${decimals}f".format(value)

    companion object {
        fun byCode(code: Int): Pid? = entries.firstOrNull { it.code == code }
    }
}

/**
 * Mode 01 PID 00/20/40/60 support bitmaps: each response is 4 bytes where the
 * MSB of byte A = PID (base+1) supported, down to LSB of byte D = (base+32).
 */
object PidSupport {
    fun decodeBitmap(basePid: Int, data: List<Int>): Set<Int> {
        val supported = mutableSetOf<Int>()
        data.take(4).forEachIndexed { byteIdx, byte ->
            for (bit in 0 until 8) {
                if (byte and (0x80 shr bit) != 0) supported += basePid + byteIdx * 8 + bit + 1
            }
        }
        return supported
    }
}
