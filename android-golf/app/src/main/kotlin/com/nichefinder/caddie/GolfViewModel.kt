package com.nichefinder.caddie

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nichefinder.caddie.net.Overpass
import com.nichefinder.golf.course.Course
import com.nichefinder.golf.course.DemoCourse
import com.nichefinder.golf.course.GreenDistances
import com.nichefinder.golf.course.Rangefinder
import com.nichefinder.golf.geo.Geo
import com.nichefinder.golf.geo.LatLng
import com.nichefinder.golf.score.Scorecard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GolfUiState(
    val course: Course? = null,
    val currentHole: Int = 1,
    val position: LatLng? = null,
    val distances: GreenDistances? = null,
    val gpsActive: Boolean = false,
    val demoMode: Boolean = false,
    val useMeters: Boolean = false,
    val strokesByHole: Map<Int, Int> = emptyMap(),
    val totalStrokes: Int = 0,
    val toPar: Int = 0,
    val stableford: Int = 0,
    val loading: Boolean = false,
    val message: String? = null,
)

class GolfViewModel(app: Application) : AndroidViewModel(app), LocationListener {

    private val _state = MutableStateFlow(GolfUiState())
    val state: StateFlow<GolfUiState> = _state.asStateFlow()

    private var scorecard: Scorecard? = null
    private var demoJob: Job? = null

    // ---- Course selection ----

    fun startDemoRound() {
        stopGps()
        val course = DemoCourse.course
        scorecard = Scorecard(course)
        _state.update {
            GolfUiState(course = course, demoMode = true, useMeters = it.useMeters, currentHole = 1)
        }
        // Simulate walking toward the green: distance ticks down like a real approach.
        demoJob?.cancel()
        demoJob = viewModelScope.launch {
            var pos = DemoCourse.demoPlayerPosition
            while (true) {
                updatePosition(pos)
                val hole = DemoCourse.course.hole(_state.value.currentHole) ?: break
                val greenCenter = Geo.centroid(hole.green)
                // step ~2.5 m toward the green center
                val stepLat = (greenCenter.lat - pos.lat)
                val stepLon = (greenCenter.lon - pos.lon)
                val norm = maxOf(Math.abs(stepLat), Math.abs(stepLon))
                if (norm > 1e-9 && Geo.distanceMeters(pos, greenCenter) > 20) {
                    pos = LatLng(pos.lat + stepLat / norm * 0.000022, pos.lon + stepLon / norm * 0.000022)
                }
                delay(1000)
            }
        }
    }

    fun loadCourseNearMe() {
        val pos = _state.value.position
        if (pos == null) {
            _state.update { it.copy(message = "Waiting for GPS fix — step outside and try again") }
            return
        }
        _state.update { it.copy(loading = true, message = null) }
        viewModelScope.launch {
            try {
                val course = Overpass.courseNear(pos, "Course near me")
                if (course.holes.isEmpty()) {
                    _state.update { it.copy(loading = false, message = "No mapped golf holes found within 2 km. (Course mapping improves weekly.)") }
                } else {
                    scorecard = Scorecard(course)
                    _state.update {
                        it.copy(course = course, demoMode = false, loading = false, currentHole = course.holes.first().number, strokesByHole = emptyMap(), totalStrokes = 0, toPar = 0, stableford = 0)
                    }
                    demoJob?.cancel()
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, message = "Couldn't load course data: ${e.message}") }
            }
        }
    }

    // ---- GPS ----

    @SuppressLint("MissingPermission") // requested in MainActivity before screens render
    fun startGps() {
        val lm = getApplication<Application>().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, this)
            _state.update { it.copy(gpsActive = true, demoMode = false) }
            demoJob?.cancel()
        } catch (e: Exception) {
            _state.update { it.copy(message = "GPS unavailable: ${e.message}") }
        }
    }

    fun stopGps() {
        val lm = getApplication<Application>().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        runCatching { lm.removeUpdates(this) }
        _state.update { it.copy(gpsActive = false) }
    }

    override fun onLocationChanged(location: Location) {
        updatePosition(LatLng(location.latitude, location.longitude))
    }

    private fun updatePosition(pos: LatLng) {
        val course = _state.value.course
        val hole = course?.hole(_state.value.currentHole)
        val d = if (hole != null) Rangefinder.distances(pos, hole) else null
        _state.update { it.copy(position = pos, distances = d) }
    }

    // ---- Hole & score ----

    fun selectHole(number: Int) {
        _state.update { it.copy(currentHole = number) }
        _state.value.position?.let { updatePosition(it) }
    }

    fun setStrokes(hole: Int, strokes: Int) {
        val card = scorecard ?: return
        card.setStrokes(hole, strokes)
        _state.update {
            it.copy(
                strokesByHole = it.course?.holes?.associate { h -> h.number to card.strokes(h.number) } ?: emptyMap(),
                totalStrokes = card.totalStrokes,
                toPar = card.toPar,
                stableford = card.stablefordPoints,
            )
        }
    }

    fun scoreLabel(hole: Int): String = scorecard?.labelFor(hole) ?: ""

    fun setUseMeters(meters: Boolean) = _state.update { it.copy(useMeters = meters) }

    fun dismissMessage() = _state.update { it.copy(message = null) }

    override fun onCleared() {
        demoJob?.cancel()
        stopGps()
    }
}
