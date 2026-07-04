package com.nichefinder.golf.course

import com.nichefinder.golf.geo.Geo
import com.nichefinder.golf.geo.LatLng

@kotlinx.serialization.Serializable
data class Hole(
    val number: Int,
    val par: Int,
    /** Green outline (3+ points). The whole product hinges on this data being right. */
    val green: List<LatLng>,
    val tee: LatLng? = null,
    val strokeIndex: Int? = null,
) {
    init {
        require(green.size >= 3) { "Hole $number: green outline needs 3+ points" }
    }
}

@kotlinx.serialization.Serializable
data class Course(
    val name: String,
    val holes: List<Hole>,
    val location: String? = null,
) {
    val totalPar: Int get() = holes.sumOf { it.par }
    fun hole(number: Int): Hole? = holes.firstOrNull { it.number == number }
}

/** Front/middle/back — the three numbers every rangefinder golfer wants. */
data class GreenDistances(
    val frontYards: Double,
    val middleYards: Double,
    val backYards: Double,
)

object Rangefinder {
    /**
     * Front = closest point of the green outline to the player, back = farthest,
     * middle = distance to the outline's centroid. Matches how laser users and
     * yardage books think about the green.
     */
    fun distances(player: LatLng, hole: Hole): GreenDistances {
        val toVertices = hole.green.map { Geo.distanceYards(player, it) }
        return GreenDistances(
            frontYards = toVertices.min(),
            middleYards = Geo.distanceYards(player, Geo.centroid(hole.green)),
            backYards = toVertices.max(),
        )
    }

    /** The hole the player is standing nearest to — used for auto-advance. */
    fun nearestHole(player: LatLng, course: Course): Hole =
        course.holes.minBy { Geo.distanceMeters(player, Geo.centroid(it.green)) }
}
