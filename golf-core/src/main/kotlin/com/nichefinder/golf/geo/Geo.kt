package com.nichefinder.golf.geo

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LatLng(val lat: Double, val lon: Double)

object Geo {
    private const val EARTH_RADIUS_M = 6_371_000.0
    const val METERS_PER_YARD = 0.9144

    /** Great-circle distance in meters (haversine — sub-meter accurate at golf scale). */
    fun distanceMeters(a: LatLng, b: LatLng): Double {
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLon = Math.toRadians(b.lon - a.lon)
        val s = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(a.lat)) * cos(Math.toRadians(b.lat)) *
            sin(dLon / 2) * sin(dLon / 2)
        return 2 * EARTH_RADIUS_M * atan2(sqrt(s), sqrt(1 - s))
    }

    fun distanceYards(a: LatLng, b: LatLng): Double = distanceMeters(a, b) / METERS_PER_YARD

    /** Initial bearing a→b in degrees [0, 360). */
    fun bearingDegrees(a: LatLng, b: LatLng): Double {
        val dLon = Math.toRadians(b.lon - a.lon)
        val lat1 = Math.toRadians(a.lat)
        val lat2 = Math.toRadians(b.lat)
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }

    /** Centroid of a polygon's vertices — fine for green-sized shapes. */
    fun centroid(points: List<LatLng>): LatLng {
        require(points.isNotEmpty()) { "empty polygon" }
        return LatLng(points.sumOf { it.lat } / points.size, points.sumOf { it.lon } / points.size)
    }
}
