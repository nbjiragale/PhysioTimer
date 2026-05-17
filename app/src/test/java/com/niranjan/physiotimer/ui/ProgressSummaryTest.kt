package com.niranjan.physiotimer.ui

import com.niranjan.physiotimer.data.SessionRecord
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressSummaryTest {
    @Test
    fun computeProgressSummary_usesRecordedSessionsAcrossBuckets() {
        val now = calendarMillis(2026, Calendar.APRIL, 30, 10, 0, 0)
        val startOfToday = startOfDay(now)

        val sessions = listOf(
            SessionRecord(
                id = 1,
                exerciseId = 10L,
                exerciseName = "Shoulder rehab",
                startedAt = startOfToday + (2 * 60 * 60 * 1_000L),
                elapsedSeconds = 300
            ),
            SessionRecord(
                id = 2,
                exerciseId = 10L,
                exerciseName = "Shoulder rehab",
                startedAt = startOfToday + (6 * 60 * 60 * 1_000L),
                elapsedSeconds = 600
            ),
            SessionRecord(
                id = 3,
                exerciseId = 20L,
                exerciseName = "Knee flexion",
                startedAt = startOfToday - (3L * 24L * 60L * 60L * 1_000L),
                elapsedSeconds = 900
            ),
            SessionRecord(
                id = 4,
                exerciseId = 30L,
                exerciseName = "Back mobility",
                startedAt = startOfToday - (10L * 24L * 60L * 60L * 1_000L),
                elapsedSeconds = 1_200
            ),
            SessionRecord(
                id = 5,
                exerciseId = 40L,
                exerciseName = "Invalid",
                startedAt = 0L,
                elapsedSeconds = 5_000
            )
        )

        val summary = computeProgressSummary(sessions, nowMillis = now)

        assertEquals(2, summary.today.sessionCount)
        assertEquals(1, summary.today.routineCount)
        assertEquals(900, summary.today.totalSeconds)

        assertEquals(3, summary.last7Days.sessionCount)
        assertEquals(2, summary.last7Days.routineCount)
        assertEquals(1_800, summary.last7Days.totalSeconds)

        assertEquals(4, summary.allTime.sessionCount)
        assertEquals(3, summary.allTime.routineCount)
        assertEquals(3_000, summary.allTime.totalSeconds)
    }

    private fun calendarMillis(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int
    ): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun startOfDay(timeMillis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
