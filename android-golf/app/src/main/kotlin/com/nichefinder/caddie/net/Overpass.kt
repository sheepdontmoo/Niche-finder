package com.nichefinder.caddie.net

import com.nichefinder.golf.course.Course
import com.nichefinder.golf.geo.LatLng
import com.nichefinder.golf.osm.OsmGolfParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Fetches golf holes/greens around a coordinate from the Overpass API
 * (OpenStreetMap's query service) and builds a Course via golf-core's parser.
 *
 * The public servers rate-limit and 504 under load (seen in live testing),
 * so we try mirrors in order before giving up.
 */
object Overpass {
    private val ENDPOINTS = listOf(
        "https://overpass-api.de/api/interpreter",
        "https://overpass.kumi.systems/api/interpreter",
        "https://overpass.private.coffee/api/interpreter",
    )

    suspend fun courseNear(center: LatLng, name: String): Course = withContext(Dispatchers.IO) {
        val query = OsmGolfParser.overpassQuery(center)
        var lastError: Exception? = null
        for (endpoint in ENDPOINTS) {
            try {
                val body = post(endpoint, query)
                return@withContext OsmGolfParser.parse(body, name, near = center)
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw lastError ?: IllegalStateException("No Overpass endpoint reachable")
    }

    private fun post(endpoint: String, query: String): String {
        val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 12_000
            readTimeout = 35_000
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        }
        try {
            conn.outputStream.use { it.write(("data=" + URLEncoder.encode(query, "UTF-8")).toByteArray()) }
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}
