package com.nichefinder.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nichefinder.obd2.ObdSession
import com.nichefinder.obd2.dtc.Dtc
import com.nichefinder.obd2.pid.Pid
import com.nichefinder.obd2.pid.PidValue
import com.nichefinder.obd2.sim.DemoElm327
import com.nichefinder.obd2.transport.ObdTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ConnectionState {
    data object Idle : ConnectionState
    data object Connecting : ConnectionState
    data class Connected(val protocol: String, val demo: Boolean) : ConnectionState
    data class Error(val message: String) : ConnectionState
}

data class DtcUi(val code: String, val system: String, val description: String)

data class UiState(
    val connection: ConnectionState = ConnectionState.Idle,
    val gauges: Map<Pid, PidValue> = emptyMap(),
    val storedCodes: List<DtcUi> = emptyList(),
    val pendingCodes: List<DtcUi> = emptyList(),
    val codesReadOnce: Boolean = false,
    val imperialUnits: Boolean = false,
)

class ObdViewModel : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var transport: ObdTransport? = null
    private var session: ObdSession? = null
    private var pollJob: Job? = null

    /** Gauges polled each cycle, in priority order, filtered by what the car supports. */
    private val dashboardPids = listOf(
        Pid.RPM, Pid.SPEED, Pid.COOLANT_TEMP, Pid.ENGINE_LOAD,
        Pid.THROTTLE, Pid.CONTROL_MODULE_VOLTAGE, Pid.INTAKE_TEMP,
    )

    fun connectDemo() = connectWith(DemoElm327(), demo = true)

    fun connect(t: ObdTransport) = connectWith(t, demo = false)

    private fun connectWith(t: ObdTransport, demo: Boolean) {
        disconnect()
        _state.update { it.copy(connection = ConnectionState.Connecting) }
        viewModelScope.launch {
            try {
                t.connect()
                val s = ObdSession(t)
                s.connect()
                s.discover()
                transport = t
                session = s
                _state.update {
                    it.copy(connection = ConnectionState.Connected(s.protocolLabel, demo))
                }
                startPolling(s)
            } catch (e: Exception) {
                runCatching { t.disconnect() }
                _state.update {
                    it.copy(connection = ConnectionState.Error(e.message ?: "Connection failed"))
                }
            }
        }
    }

    private fun startPolling(s: ObdSession) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            val pids = dashboardPids.filter { it.code in s.supportedPids }
            while (true) {
                for (pid in pids) {
                    try {
                        val v = s.read(pid)
                        _state.update { it.copy(gauges = it.gauges + (pid to v)) }
                    } catch (_: Exception) {
                        // transient NO DATA on one PID shouldn't kill the loop
                    }
                }
                delay(POLL_PAUSE_MS)
            }
        }
    }

    fun readCodes() {
        val s = session ?: return
        viewModelScope.launch {
            try {
                val stored = s.readStoredCodes().map { it.toUi() }
                val pending = s.readPendingCodes().map { it.toUi() }
                _state.update {
                    it.copy(storedCodes = stored, pendingCodes = pending, codesReadOnce = true)
                }
            } catch (e: Exception) {
                _state.update { it.copy(connection = ConnectionState.Error(e.message ?: "Read failed")) }
            }
        }
    }

    fun clearCodes() {
        val s = session ?: return
        viewModelScope.launch {
            runCatching { s.clearCodes() }
            readCodes()
        }
    }

    fun setImperial(imperial: Boolean) = _state.update { it.copy(imperialUnits = imperial) }

    fun disconnect() {
        pollJob?.cancel()
        pollJob = null
        val t = transport
        transport = null
        session = null
        if (t != null) viewModelScope.launch { runCatching { t.disconnect() } }
        _state.update { UiState(imperialUnits = it.imperialUnits) }
    }

    override fun onCleared() = disconnect()

    private fun Dtc.toUi() = DtcUi(code, system, DtcDescriptions.describe(code))

    companion object {
        private const val POLL_PAUSE_MS = 150L
    }
}
