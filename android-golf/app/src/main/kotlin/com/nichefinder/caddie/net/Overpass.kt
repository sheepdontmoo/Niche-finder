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
 */
object Overpass {
    private const val ENDPOINT = "https://overpass-api.de/api/interpreter"

    suspend fun courseNear(center: LatLng, name: String): Course = withContext(Dispatchers.IO) {
        val query = OsmGolfParser.overpassQuery(center)
        val conn = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        }
        conn.outputStream.use { it.write(("data=" + URLEncoder.encode(query, "UTF-8")).toByteArray()) }
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()
        OsmGolfParser.parse(body, name)
    }
}
