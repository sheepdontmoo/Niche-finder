package com.nichefinder.golf.score

import com.nichefinder.golf.course.Course

/** Strokes per hole number; 0/absent = not played yet. */
class Scorecard(val course: Course) {
    private val strokes = mutableMapOf<Int, Int>()

    fun setStrokes(holeNumber: Int, count: Int) {
        require(course.hole(holeNumber) != null) { "No hole $holeNumber on ${course.name}" }
        if (count <= 0) strokes.remove(holeNumber) else strokes[holeNumber] = count
    }

    fun strokes(holeNumber: Int): Int = strokes[holeNumber] ?: 0

    val holesPlayed: Int get() = strokes.size
    val totalStrokes: Int get() = strokes.values.sum()

    /** Score vs par over the holes actually played (e.g. -2, 0, +5). */
    val toPar: Int
        get() = strokes.entries.sumOf { (hole, s) -> s - (course.hole(hole)?.par ?: 0) }

    /** Standard Stableford points (par=2pts, birdie=3, bogey=1, worse=0). */
    val stablefordPoints: Int
        get() = strokes.entries.sumOf { (hole, s) ->
            val par = course.hole(hole)?.par ?: return@sumOf 0
            (2 + (par - s)).coerceAtLeast(0)
        }

    fun labelFor(holeNumber: Int): String {
        val s = strokes(holeNumber)
        val par = course.hole(holeNumber)?.par ?: return ""
        if (s == 0) return ""
        return when (s - par) {
            -3 -> "Albatross"
            -2 -> "Eagle"
            -1 -> "Birdie"
            0 -> "Par"
            1 -> "Bogey"
            2 -> "Double bogey"
            else -> if (s == 1) "Ace!" else "+${s - par}"
        }
    }
}
