package com.nichefinder.golf

import com.nichefinder.golf.course.DemoCourse
import com.nichefinder.golf.course.Rangefinder
import com.nichefinder.golf.geo.Geo
import com.nichefinder.golf.geo.LatLng
import com.nichefinder.golf.osm.OsmGolfParser
import com.nichefinder.golf.score.Scorecard
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeoTest {
    @Test fun `haversine matches known distance`() {
        // 0.001 deg latitude = 111.19 m at any longitude
        val a = LatLng(53.3000, -6.2000)
        val b = LatLng(53.3010, -6.2000)
        assertTrue(abs(Geo.distanceMeters(a, b) - 111.19) < 0.5)
    }

    @Test fun `yards conversion is exact`() {
        val a = LatLng(53.3000, -6.2000)
        val b = LatLng(53.3010, -6.2000)
        assertEquals(Geo.distanceMeters(a, b) / 0.9144, Geo.distanceYards(a, b))
    }

    @Test fun `bearing north and east`() {
        val o = LatLng(53.3, -6.2)
        assertTrue(abs(Geo.bearingDegrees(o, LatLng(53.31, -6.2)) - 0.0) < 0.5)     // north
        assertTrue(abs(Geo.bearingDegrees(o, LatLng(53.3, -6.19)) - 90.0) < 1.0)    // east
    }

    @Test fun `centroid of symmetric shape is its center`() {
        val c = Geo.centroid(listOf(LatLng(1.0, 1.0), LatLng(1.0, -1.0), LatLng(-1.0, 1.0), LatLng(-1.0, -1.0)))
        assertEquals(0.0, c.lat)
        assertEquals(0.0, c.lon)
    }
}

class RangefinderTest {
    private val hole1 = DemoCourse.course.hole(1)!!

    @Test fun `front is less than middle is less than back`() {
        val d = Rangefinder.distances(DemoCourse.demoPlayerPosition, hole1)
        assertTrue(d.frontYards < d.middleYards, "front ${d.frontYards} < middle ${d.middleYards}")
        assertTrue(d.middleYards < d.backYards, "middle ${d.middleYards} < back ${d.backYards}")
    }

    @Test fun `demo position is a believable approach shot`() {
        val d = Rangefinder.distances(DemoCourse.demoPlayerPosition, hole1)
        assertTrue(d.middleYards in 130.0..170.0, "expected ~150 yds, got ${d.middleYards}")
    }

    @Test fun `demo course hole lengths are realistic`() {
        fun teeToGreen(n: Int): Double {
            val h = DemoCourse.course.hole(n)!!
            return Geo.distanceYards(h.tee!!, Geo.centroid(h.green))
        }
        assertTrue(teeToGreen(1) in 330.0..430.0, "par 4 length ${teeToGreen(1)}")
        assertTrue(teeToGreen(2) in 130.0..200.0, "par 3 length ${teeToGreen(2)}")
        assertTrue(teeToGreen(3) in 470.0..570.0, "par 5 length ${teeToGreen(3)}")
    }

    @Test fun `nearest hole detection`() {
        assertEquals(1, Rangefinder.nearestHole(DemoCourse.demoPlayerPosition, DemoCourse.course).number)
        val nearThird = LatLng(53.29900, -6.19720)
        assertEquals(3, Rangefinder.nearestHole(nearThird, DemoCourse.course).number)
    }
}

class ScorecardTest {
    @Test fun `totals and to-par`() {
        val card = Scorecard(DemoCourse.course)   // pars: 4, 3, 5
        card.setStrokes(1, 5)  // bogey
        card.setStrokes(2, 2)  // birdie
        card.setStrokes(3, 5)  // par
        assertEquals(12, card.totalStrokes)
        assertEquals(0, card.toPar)
        assertEquals(3, card.holesPlayed)
    }

    @Test fun `stableford points`() {
        val card = Scorecard(DemoCourse.course)
        card.setStrokes(1, 4)  // par -> 2
        card.setStrokes(2, 2)  // birdie -> 3
        card.setStrokes(3, 8)  // triple -> 0
        assertEquals(5, card.stablefordPoints)
    }

    @Test fun `labels`() {
        val card = Scorecard(DemoCourse.course)
        card.setStrokes(1, 3)
        card.setStrokes(2, 3)
        card.setStrokes(3, 7)
        assertEquals("Birdie", card.labelFor(1))
        assertEquals("Par", card.labelFor(2))
        assertEquals("Double bogey", card.labelFor(3))
        assertEquals("", card.labelFor(99).takeIf { card.course.hole(99) == null } ?: "")
    }

    @Test fun `clearing a hole`() {
        val card = Scorecard(DemoCourse.course)
        card.setStrokes(1, 4)
        card.setStrokes(1, 0)
        assertEquals(0, card.holesPlayed)
    }
}

class OsmParserTest {
    // Two holes; green polygons offset so hole 1's path ends near green A, hole 2's near green B.
    private val overpassJson = """
    {"elements":[
      {"type":"node","id":1,"lat":53.3000,"lon":-6.2000},
      {"type":"node","id":2,"lat":53.3030,"lon":-6.2000},
      {"type":"node","id":10,"lat":53.30310,"lon":-6.20010},
      {"type":"node","id":11,"lat":53.30320,"lon":-6.19990},
      {"type":"node","id":12,"lat":53.30300,"lon":-6.19985},
      {"type":"node","id":3,"lat":53.3032,"lon":-6.1998},
      {"type":"node","id":4,"lat":53.3032,"lon":-6.1975},
      {"type":"node","id":20,"lat":53.30310,"lon":-6.19760},
      {"type":"node","id":21,"lat":53.30330,"lon":-6.19740},
      {"type":"node","id":22,"lat":53.30310,"lon":-6.19730},
      {"type":"way","id":100,"nodes":[1,2],"tags":{"golf":"hole","ref":"1","par":"4"}},
      {"type":"way","id":101,"nodes":[10,11,12],"tags":{"golf":"green"}},
      {"type":"way","id":102,"nodes":[3,4],"tags":{"golf":"hole","ref":"2","par":"3"}},
      {"type":"way","id":103,"nodes":[20,21,22],"tags":{"golf":"green"}}
    ]}
    """

    @Test fun `parses holes with pars and matches greens by proximity`() {
        val course = OsmGolfParser.parse(overpassJson, "Test GC")
        assertEquals(2, course.holes.size)
        assertEquals(4, course.hole(1)!!.par)
        assertEquals(3, course.hole(2)!!.par)
        assertEquals(7, course.totalPar)
        // hole 1's matched green should be the one near its path end (nodes 10-12, lon ~ -6.1999x)
        assertTrue(course.hole(1)!!.green.all { it.lon < -6.1998 })
        assertTrue(course.hole(2)!!.green.all { it.lon > -6.1977 })
    }

    @Test fun `overlapping courses dedupe by tee proximity`() {
        // Two "hole 1" ways (St Andrews scenario). Player is near the first tee.
        val json = """
        {"elements":[
          {"type":"node","id":1,"lat":53.3000,"lon":-6.2000},
          {"type":"node","id":2,"lat":53.3030,"lon":-6.2000},
          {"type":"node","id":10,"lat":53.30310,"lon":-6.20010},
          {"type":"node","id":11,"lat":53.30320,"lon":-6.19990},
          {"type":"node","id":12,"lat":53.30300,"lon":-6.19985},
          {"type":"node","id":5,"lat":53.3200,"lon":-6.2200},
          {"type":"node","id":6,"lat":53.3230,"lon":-6.2200},
          {"type":"node","id":20,"lat":53.32310,"lon":-6.22010},
          {"type":"node","id":21,"lat":53.32320,"lon":-6.21990},
          {"type":"node","id":22,"lat":53.32300,"lon":-6.21985},
          {"type":"way","id":100,"nodes":[1,2],"tags":{"golf":"hole","ref":"1","par":"4"}},
          {"type":"way","id":101,"nodes":[10,11,12],"tags":{"golf":"green"}},
          {"type":"way","id":102,"nodes":[5,6],"tags":{"golf":"hole","ref":"1","par":"5"}},
          {"type":"way","id":103,"nodes":[20,21,22],"tags":{"golf":"green"}}
        ]}
        """
        val nearFirstTee = LatLng(53.3001, -6.2001)
        val course = OsmGolfParser.parse(json, "Overlap GC", near = nearFirstTee)
        assertEquals(1, course.holes.size)
        assertEquals(4, course.hole(1)!!.par)   // kept the nearer course's par-4, not the far par-5
    }

    @Test fun `query builder embeds coordinates`() {
        val q = OsmGolfParser.overpassQuery(LatLng(53.3, -6.2), 1500)
        assertTrue("around:1500,53.3,-6.2" in q)
        assertTrue("golf" in q)
    }
}

class SerializationTest {
    @Test fun `course survives JSON round-trip for offline caching`() {
        val json = kotlinx.serialization.json.Json.encodeToString(
            com.nichefinder.golf.course.Course.serializer(), DemoCourse.course)
        val back = kotlinx.serialization.json.Json.decodeFromString(
            com.nichefinder.golf.course.Course.serializer(), json)
        assertEquals(DemoCourse.course, back)
        assertEquals(12, back.totalPar)
    }
}
