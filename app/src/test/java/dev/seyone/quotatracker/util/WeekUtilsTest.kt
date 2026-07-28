package dev.seyone.quotatracker.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class WeekUtilsTest {

    private val zoneId = ZoneId.of("UTC")

    @Test
    fun testCurrentWeekRange_startsOnMondayAndEndsOnSunday() {
        // Wednesday Jul 29, 2026, 12:00 UTC
        val wednesdayLdt = LocalDateTime.of(2026, 7, 29, 12, 0, 0)
        val epochMillis = wednesdayLdt.toInstant(ZoneOffset.UTC).toEpochMilli()

        val (start, end) = WeekUtils.getCurrentWeekRange(epochMillis, zoneId)

        val startLdt = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(start), zoneId)
        val endLdt = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(end), zoneId)

        assertEquals("MONDAY", startLdt.dayOfWeek.name)
        assertEquals(0, startLdt.hour)
        assertEquals(0, startLdt.minute)
        assertEquals(0, startLdt.second)

        assertEquals("SUNDAY", endLdt.dayOfWeek.name)
        assertEquals(23, endLdt.hour)
        assertEquals(59, endLdt.minute)
        assertEquals(59, endLdt.second)
    }

    @Test
    fun testMidnightSplitting_spansSundayToMonday() {
        // Sunday Jul 26, 2026, 23:00 UTC (60 mins before midnight)
        val sundayNight = LocalDateTime.of(2026, 7, 26, 23, 0, 0)
        val startEpoch = sundayNight.toInstant(ZoneOffset.UTC).toEpochMilli()
        val durationMinutes = 120 // 2 hours

        val segments = WeekUtils.splitLogAcrossMidnight(startEpoch, durationMinutes, zoneId)

        assertEquals(2, segments.size)

        // First segment: 60 mins on Sunday starting 23:00
        assertEquals(60, segments[0].second)
        val seg0Time = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(segments[0].first), zoneId)
        assertEquals(23, seg0Time.hour)
        assertEquals("SUNDAY", seg0Time.dayOfWeek.name)

        // Second segment: 60 mins on Monday starting 00:00
        assertEquals(60, segments[1].second)
        val seg1Time = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(segments[1].first), zoneId)
        assertEquals(0, seg1Time.hour)
        assertEquals(0, seg1Time.minute)
        assertEquals("MONDAY", seg1Time.dayOfWeek.name)
    }

    @Test
    fun testMidnightSplitting_noSplitWithinSameDay() {
        val mondayMorning = LocalDateTime.of(2026, 7, 27, 10, 0, 0)
        val startEpoch = mondayMorning.toInstant(ZoneOffset.UTC).toEpochMilli()
        val durationMinutes = 45

        val segments = WeekUtils.splitLogAcrossMidnight(startEpoch, durationMinutes, zoneId)

        assertEquals(1, segments.size)
        assertEquals(45, segments[0].second)
        assertEquals(startEpoch, segments[0].first)
    }
}
