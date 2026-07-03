package com.nichefinder.golf.course

import com.nichefinder.golf.geo.LatLng

/**
 * A fictional 3-hole seaside course for demo mode and tests. Distances are
 * realistic: par 4 (~380 yds), par 3 (~165 yds), par 5 (~520 yds).
 * Coordinates sit off the Irish coast — pure invention, no real course copied.
 */
object DemoCourse {

    // ~0.0001 deg latitude ≈ 11.1 m ≈ 12.1 yds
    private fun green(centerLat: Double, centerLon: Double, radiusDeg: Double = 0.00012) = listOf(
        LatLng(centerLat + radiusDeg, centerLon),
        LatLng(centerLat + radiusDeg * 0.5, centerLon + radiusDeg),
        LatLng(centerLat - radiusDeg * 0.5, centerLon + radiusDeg),
        LatLng(centerLat - radiusDeg, centerLon),
        LatLng(centerLat - radiusDeg * 0.5, centerLon - radiusDeg),
        LatLng(centerLat + radiusDeg * 0.5, centerLon - radiusDeg),
    )

    val course = Course(
        name = "Demo Links",
        location = "Demo mode — no GPS needed",
        holes = listOf(
            // Hole 1: par 4, tee to green center ≈ 380 yds heading north
            Hole(number = 1, par = 4, tee = LatLng(53.3000, -6.2000), green = green(53.30313, -6.2000)),
            // Hole 2: par 3, ≈ 165 yds heading east
            Hole(number = 2, par = 3, tee = LatLng(53.30330, -6.19980), green = green(53.30330, -6.19754)),
            // Hole 3: par 5, ≈ 520 yds heading south
            Hole(number = 3, par = 5, tee = LatLng(53.30310, -6.19720), green = green(53.29882, -6.19720)),
        ),
    )

    /** A believable player position: mid-fairway on hole 1, ~150 yds from the green center. */
    val demoPlayerPosition = LatLng(53.30190, -6.2000)
}
