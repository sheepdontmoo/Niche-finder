package com.nichefinder.obd2

import com.nichefinder.obd2.dtc.Dtc
import com.nichefinder.obd2.dtc.DtcParser
import com.nichefinder.obd2.elm.ObdProtocol
import com.nichefinder.obd2.elm.ObdResponse
import com.nichefinder.obd2.pid.Pid
import com.nichefinder.obd2.pid.PidSupport
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PidDecodingTest {
    @Test fun `rpm decodes per J1979 formula`() {
        assertEquals(1726.0, Pid.RPM.decode(listOf(0x1A, 0xF8)).value)
    }

    @Test fun `coolant temp has minus-40 offset`() {
        assertEquals(83.0, Pid.COOLANT_TEMP.decode(listOf(0x7B)).value)
        assertEquals(-40.0, Pid.COOLANT_TEMP.decode(listOf(0x00)).value)
    }

    @Test fun `speed is raw byte`() {
        assertEquals(75.0, Pid.SPEED.decode(listOf(0x4B)).value)
    }

    @Test fun `fuel trim centers at zero`() {
        assertEquals(0.0, Pid.SHORT_FUEL_TRIM_1.decode(listOf(128)).value)
    }

    @Test fun `voltage scales by 1000`() {
        assertEquals(14.892, Pid.CONTROL_MODULE_VOLTAGE.decode(listOf(0x3A, 0x2C)).value)
    }

    @Test fun `display formatting respects unit and decimals`() {
        assertEquals("1726 rpm", Pid.RPM.decode(listOf(0x1A, 0xF8)).display)
        assertEquals("14.89 V", Pid.CONTROL_MODULE_VOLTAGE.decode(listOf(0x3A, 0x2C)).display)
    }

    @Test fun `short data rejected`() {
        assertFailsWith<IllegalArgumentException> { Pid.RPM.decode(listOf(0x1A)) }
    }

    @Test fun `support bitmap decodes standard BE3FA813 pattern`() {
        val supported = PidSupport.decodeBitmap(0x00, listOf(0xBE, 0x3F, 0xA8, 0x13))
        // 0xBE = 1011 1110 -> PIDs 01, 03, 04, 05, 06, 07
        assertTrue(0x01 in supported)
        assertTrue(0x02 !in supported)
        assertTrue(0x0C in supported)  // RPM advertised
        assertTrue(0x20 in supported)  // next-bitmap bit (LSB of 0x13)
    }
}

class DtcTest {
    @Test fun `dtc letter comes from top two bits`() {
        assertEquals("P0301", Dtc.fromBytes(0x03, 0x01).code)
        assertEquals("P0420", Dtc.fromBytes(0x04, 0x20).code)
        assertEquals("C1234", Dtc.fromBytes(0x52, 0x34).code)
        assertEquals("B0001", Dtc.fromBytes(0x80, 0x01).code)
        assertEquals("U0100", Dtc.fromBytes(0xC1, 0x00).code)
    }

    @Test fun `parser strips CAN count byte and zero padding`() {
        // "43 02 0301 0420" -> count=2, codes P0301 P0420
        val codes = DtcParser.parse(listOf(0x02, 0x03, 0x01, 0x04, 0x20))
        assertEquals(listOf("P0301", "P0420"), codes.map { it.code })
        // padded frame: trailing 0000 ignored
        val padded = DtcParser.parse(listOf(0x02, 0x03, 0x01, 0x04, 0x20, 0x00, 0x00))
        assertEquals(2, padded.size)
    }

    @Test fun `system labels map from letter`() {
        assertEquals("Powertrain", Dtc("P0301").system)
        assertEquals("Network", Dtc("U0100").system)
    }
}

class ObdResponseTest {
    @Test fun `positive echo adds 0x40 to mode`() {
        assertEquals("4100", ObdResponse.positiveEcho("0100"))
        assertEquals("43", ObdResponse.positiveEcho("03"))
        assertEquals("4902", ObdResponse.positiveEcho("0902"))
    }

    @Test fun `parses headered CAN reply and strips echo`() {
        val r = ObdResponse.parse("010C", "7E804410C1AF8\r\r>")
        assertEquals(listOf(0x1A, 0xF8), r.primaryData())
    }

    @Test fun `error vocabulary yields empty response`() {
        assertTrue(ObdResponse.parse("010C", "NO DATA\r>").isError())
        assertTrue(ObdResponse.parse("010C", "CAN ERROR\r>").isError())
        assertTrue(ObdResponse.parse("010C", "UNABLE TO CONNECT\r>").isError())
    }

    @Test fun `searching noise is ignored`() {
        val r = ObdResponse.parse("0100", "SEARCHING...\r7E8064100BE3FA813\r>")
        assertEquals(listOf(0xBE, 0x3F, 0xA8, 0x13), r.primaryData())
    }
}

class ObdSessionIntegrationTest {
    @Test fun `full session against simulated clone adapter`() = runTest {
        val sim = SimulatedElm327()
        val session = ObdSession(sim)

        session.connect()
        assertEquals(ObdProtocol.ISO_15765_CAN_11_500.label, session.protocolLabel)

        val pids = session.discover()
        assertTrue(Pid.RPM.code in pids)
        assertTrue(0x21 in pids)          // second bitmap reached
        assertTrue(0x41 !in pids)         // discovery stopped where support ended

        assertEquals(1726.0, session.read(Pid.RPM).value)
        assertEquals(75.0, session.read(Pid.SPEED).value)
        assertEquals(83.0, session.read(Pid.COOLANT_TEMP).value)
        assertEquals("14.89 V", session.read(Pid.CONTROL_MODULE_VOLTAGE).display)

        assertEquals(listOf("P0301", "P0420"), session.readStoredCodes().map { it.code })
        assertTrue(session.readPendingCodes().isEmpty())

        // init sequence actually ran in order
        assertEquals(listOf("ATZ", "ATE0", "ATL0", "ATS0", "ATH1", "ATSP0"), sim.sentCommands.take(6))
    }
}
