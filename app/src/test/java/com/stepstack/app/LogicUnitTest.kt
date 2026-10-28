package com.stepstack.app

import com.stepstack.app.data.Challenge
import com.stepstack.app.data.ChallengeRepository
import com.stepstack.app.data.ChallengeType
import com.stepstack.app.data.DailyActivity
import com.stepstack.app.data.LeaderboardEntry
import com.stepstack.app.data.LeaderboardPeriod
import com.stepstack.app.data.LeaderboardRepository
import com.stepstack.app.data.currentStreakDays
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class LogicUnitTest {

    @Test
    fun testStreakCalculation() {
        val today = LocalDate.now().toEpochDay()
        
        // 3 consecutive days meeting goal ending yesterday
        val history = listOf(
            DailyActivity(epochDay = today - 3, steps = 10000, workoutMinutes = 0, goalForDay = 8000),
            DailyActivity(epochDay = today - 2, steps = 9000, workoutMinutes = 0, goalForDay = 8000),
            DailyActivity(epochDay = today - 1, steps = 8500, workoutMinutes = 0, goalForDay = 8000),
            DailyActivity(epochDay = today, steps = 1000, workoutMinutes = 0, goalForDay = 8000) // Today not yet met
        )

        val streak = history.currentStreakDays(today)
        assertEquals(3, streak)
    }

    @Test
    fun testWeekStartIsMonday() {
        val now = LocalDate.now()
        val mondayEpoch = LeaderboardRepository.weekStartEpoch()
        val mondayDate = LocalDate.ofEpochDay(mondayEpoch)

        assertEquals(DayOfWeek.MONDAY, mondayDate.dayOfWeek)
        assertEquals(true, mondayEpoch <= now.toEpochDay())
    }

    @Test
    fun testLeaderboardMyValueCalculation() {
        val today = LocalDate.now().toEpochDay()
        val weekStart = LocalDate.now().with(DayOfWeek.MONDAY).toEpochDay()

        val history = listOf(
            DailyActivity(epochDay = today - 10, steps = 5000, workoutMinutes = 0),
            DailyActivity(epochDay = weekStart, steps = 7000, workoutMinutes = 0),
            DailyActivity(epochDay = today, steps = 3000, workoutMinutes = 0)
        )

        val todayValue = LeaderboardRepository.myValue(LeaderboardPeriod.TODAY, history)
        assertEquals(3000, todayValue)

        val weekValue = LeaderboardRepository.myValue(LeaderboardPeriod.WEEK, history)
        assertEquals(if (today == weekStart) 3000 else 10000, weekValue)
    }

    @Test
    fun testChallengeDayRangeAndTotalDays() {
        val zone = ZoneId.systemDefault()
        val startMs = LocalDate.of(2025, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli()
        val endMs = LocalDate.of(2025, 1, 7).atStartOfDay(zone).toInstant().toEpochMilli()

        val challenge = Challenge(
            id = "c1",
            title = "Sprint",
            type = ChallengeType.RACE_TOTAL_STEPS,
            startDate = startMs,
            endDate = endMs
        )

        val totalDays = ChallengeRepository.totalDays(challenge)
        assertEquals(7, totalDays)
    }
}
