package com.nichefinder.scanner

/**
 * Plain-language descriptions for common trouble codes, with generic
 * range-based fallbacks. Expanding this into a full SAE J2012 database
 * (and per-manufacturer codes) is a P1 roadmap item.
 */
object DtcDescriptions {

    private val known = mapOf(
        "P0011" to "Camshaft timing over-advanced (Bank 1)",
        "P0016" to "Crankshaft/camshaft position correlation fault",
        "P0101" to "Mass air flow sensor range/performance problem",
        "P0102" to "Mass air flow sensor circuit low input",
        "P0113" to "Intake air temperature sensor circuit high",
        "P0128" to "Engine coolant temperature below thermostat regulating temperature",
        "P0131" to "Oxygen sensor circuit low voltage (Bank 1, Sensor 1)",
        "P0133" to "Oxygen sensor slow response (Bank 1, Sensor 1)",
        "P0171" to "System too lean (Bank 1) — often a vacuum leak or dirty MAF",
        "P0172" to "System too rich (Bank 1)",
        "P0174" to "System too lean (Bank 2)",
        "P0300" to "Random/multiple cylinder misfire detected",
        "P0301" to "Cylinder 1 misfire detected",
        "P0302" to "Cylinder 2 misfire detected",
        "P0303" to "Cylinder 3 misfire detected",
        "P0304" to "Cylinder 4 misfire detected",
        "P0325" to "Knock sensor circuit malfunction (Bank 1)",
        "P0335" to "Crankshaft position sensor circuit malfunction",
        "P0340" to "Camshaft position sensor circuit malfunction",
        "P0401" to "Exhaust gas recirculation flow insufficient",
        "P0420" to "Catalyst efficiency below threshold (Bank 1) — often catalytic converter or O2 sensor",
        "P0430" to "Catalyst efficiency below threshold (Bank 2)",
        "P0440" to "Evaporative emission system malfunction",
        "P0442" to "EVAP system small leak detected — check the gas cap first",
        "P0455" to "EVAP system large leak detected — check the gas cap first",
        "P0456" to "EVAP system very small leak detected",
        "P0500" to "Vehicle speed sensor malfunction",
        "P0505" to "Idle air control system malfunction",
        "P0562" to "System voltage low — check battery and alternator",
        "P0700" to "Transmission control system fault (see transmission codes)",
        "U0100" to "Lost communication with engine control module",
        "U0101" to "Lost communication with transmission control module",
    )

    fun describe(code: String): String {
        known[code]?.let { return it }
        return when {
            code.startsWith("P00") -> "Fuel/air metering or auxiliary emission fault"
            code.startsWith("P01") -> "Fuel and air metering fault"
            code.startsWith("P02") -> "Fuel injector circuit fault"
            code.startsWith("P03") -> "Ignition system or misfire fault"
            code.startsWith("P04") -> "Auxiliary emissions control fault"
            code.startsWith("P05") -> "Vehicle speed control / idle control fault"
            code.startsWith("P06") -> "Computer output circuit fault"
            code.startsWith("P07") || code.startsWith("P08") -> "Transmission fault"
            code.startsWith("P1") -> "Manufacturer-specific powertrain fault"
            code.startsWith("C") -> "Chassis fault (ABS, steering, suspension)"
            code.startsWith("B") -> "Body fault (airbags, HVAC, lighting)"
            code.startsWith("U") -> "Network/communication fault between modules"
            else -> "Unknown code — look it up for your specific vehicle"
        }
    }
}
