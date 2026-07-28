package dev.seyone.quotatracker.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

object WeekUtils {

    /**
     * Calculates the start (Monday 00:00:00.000) and end (Sunday 23:59:59.999) 
     * epoch timestamps for the local week containing [now].
     */
    fun getCurrentWeekRange(
        now: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Pair<Long, Long> {
        val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), zoneId)
        val monday = ldt.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .with(LocalTime.MIN)
        val sunday = ldt.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            .with(LocalTime.MAX)

        val startEpoch = monday.atZone(zoneId).toInstant().toEpochMilli()
        val endEpoch = sunday.atZone(zoneId).toInstant().toEpochMilli()

        return Pair(startEpoch, endEpoch)
    }

    /**
     * Formats a timestamp into ISO week string format "YYYY-W##", e.g. "2026-W11".
     */
    fun getWeekString(
        timestamp: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val date = Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekNumber = date.get(weekFields.weekOfWeekBasedYear())
        val year = date.get(weekFields.weekBasedYear())
        return String.format(Locale.getDefault(), "%d-W%02d", year, weekNumber)
    }

    /**
     * Helper to split a duration starting at [startTimestamp] across midnight boundaries.
     * Returns a list of Pair<Long, Int> where first is timestamp and second is durationMinutes for that segment.
     */
    fun splitLogAcrossMidnight(
        startTimestamp: Long,
        durationMinutes: Int,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<Pair<Long, Int>> {
        if (durationMinutes <= 0) return emptyList()

        val results = mutableListOf<Pair<Long, Int>>()
        var currentStart = startTimestamp
        var remainingMinutes = durationMinutes

        while (remainingMinutes > 0) {
            val currentLdt = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentStart), zoneId)
            val nextMidnightLdt = currentLdt.toLocalDate().plusDays(1).atStartOfDay()
            val nextMidnightEpoch = nextMidnightLdt.atZone(zoneId).toInstant().toEpochMilli()

            val millisUntilMidnight = nextMidnightEpoch - currentStart
            val minutesUntilMidnight = (millisUntilMidnight / (60 * 1000)).toInt()

            if (minutesUntilMidnight <= 0) {
                currentStart = nextMidnightEpoch
                continue
            }

            if (remainingMinutes <= minutesUntilMidnight) {
                results.add(Pair(currentStart, remainingMinutes))
                break
            } else {
                results.add(Pair(currentStart, minutesUntilMidnight))
                remainingMinutes -= minutesUntilMidnight
                currentStart = nextMidnightEpoch
            }
        }

        return results
    }
}
