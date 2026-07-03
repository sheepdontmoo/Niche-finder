package com.nichefinder.golf.osm

import com.nichefinder.golf.course.Course
import com.nichefinder.golf.course.Hole
import com.nichefinder.golf.geo.Geo
import com.nichefinder.golf.geo.LatLng
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Builds a Course from an Overpass API JSON response. OpenStreetMap has
 * surprisingly good golf coverage: greens are `golf=green` ways, hole paths
 * are `golf=hole` ways carrying `ref` (hole number) and `par` tags.
 *
 * Expected Overpass query shape (the app issues this):
 *   [out:json];
 *   way(around:2000,LAT,LON)["golf"~"green|hole"];
 *   (._;>;);
 *   out body;
 *
 * Greens are matched to holes by proximity to each hole path's far end.
 * This importer is the bootstrap for the course database; community
 * corrections layer on top later.
 */
object OsmGolfParser {

    /**
     * @param near When several courses share the land (St Andrews has seven —
     * a real case found in live testing), multiple ways carry the same hole
     * ref. Passing the player/query position keeps, per hole number, only the
     * hole whose tee is nearest — i.e. the course you're standing on.
     */
    fun parse(overpassJson: String, courseName: String, near: LatLng? = null): Course {
        val root = Json.parseToJsonElement(overpassJson).jsonObject
        val elements = root["elements"]?.jsonArray ?: return Course(courseName, emptyList())

        // First pass: node id -> coordinate
        val nodes = mutableMapOf<Long, LatLng>()
        for (el in elements) {
            val o = el.jsonObject
            if (o["type"]?.jsonPrimitive?.content == "node") {
                nodes[o["id"]!!.jsonPrimitive.content.toLong()] =
                    LatLng(o["lat"]!!.jsonPrimitive.content.toDouble(), o["lon"]!!.jsonPrimitive.content.toDouble())
            }
        }

        data class HolePath(val number: Int, val par: Int, val path: List<LatLng>)

        val greens = mutableListOf<List<LatLng>>()
        val holePaths = mutableListOf<HolePath>()

        for (el in elements) {
            val o = el.jsonObject
            if (o["type"]?.jsonPrimitive?.content != "way") continue
            val tags = o["tags"]?.jsonObject ?: continue
            val coords = o["nds"]?.jsonArray?.mapNotNull { nodes[it.jsonPrimitive.content.toLong()] }
                ?: o["nodes"]?.jsonArray?.mapNotNull { nodes[it.jsonPrimitive.content.toLong()] }
                ?: continue
            when (tags["golf"]?.jsonPrimitive?.content) {
                "green" -> if (coords.size >= 3) greens += coords
                "hole" -> {
                    val number = tags["ref"]?.jsonPrimitive?.content?.toIntOrNull() ?: continue
                    val par = tags["par"]?.jsonPrimitive?.content?.toIntOrNull() ?: 4
                    if (coords.isNotEmpty()) holePaths += HolePath(number, par, coords)
                }
            }
        }

        // Overlapping courses: per hole number keep the path whose tee is
        // nearest the query point, so "hole 1" means THIS course's hole 1.
        val dedupedPaths = if (near != null) {
            holePaths.groupBy { it.number }.map { (_, candidates) ->
                candidates.minBy { Geo.distanceMeters(near, it.path.first()) }
            }
        } else holePaths

        // Match each hole to the green nearest the end of its path (tee is the start).
        val holes = dedupedPaths.sortedBy { it.number }.mapNotNull { hp ->
            val pin = hp.path.last()
            val green = greens.minByOrNull { g -> Geo.distanceMeters(pin, Geo.centroid(g)) }
                ?: return@mapNotNull null
            Hole(number = hp.number, par = hp.par, green = green, tee = hp.path.first())
        }

        return Course(courseName, holes)
    }

    /** The Overpass query the app sends for a course near a coordinate. */
    fun overpassQuery(center: LatLng, radiusMeters: Int = 2000): String =
        "[out:json][timeout:30];way(around:$radiusMeters,${center.lat},${center.lon})[\"golf\"~\"green|hole\"];(._;>;);out body;"
}
