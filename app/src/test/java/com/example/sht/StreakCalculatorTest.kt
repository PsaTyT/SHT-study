package com.example.sht

import com.example.sht.data.StreakCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {

    private val today = LocalDate.of(2026, 10, 16)

    @Test
    fun `empty list returns zero`() {
        val result = StreakCalculator.calculateStreak(emptyList(), today)
        assertEquals(0, result)
    }

    @Test
    fun `only today returns one`() {
        val dates = listOf(today)
        val result = StreakCalculator.calculateStreak(dates, today)
        assertEquals(1, result)
    }

    @Test
    fun `today and yesterday returns two`() {
        val dates = listOf(today, today.minusDays(1))
        val result = StreakCalculator.calculateStreak(dates, today)
        assertEquals(2, result)
    }

    @Test
    fun `five days in a row returns five`() {
        val dates = (0L..4L).map { today.minusDays(it) }
        val result = StreakCalculator.calculateStreak(dates, today)
        assertEquals(5, result)
    }

    @Test
    fun `gap breaks the streak`() {
        // Сегодня, вчера, а потом пропуск (3 дня назад)
        val dates = listOf(today, today.minusDays(1), today.minusDays(3))
        val result = StreakCalculator.calculateStreak(dates, today)
        assertEquals(2, result)
    }

    @Test
    fun `only yesterday is still alive`() {
        val dates = listOf(today.minusDays(1))
        val result = StreakCalculator.calculateStreak(dates, today)
        assertEquals(1, result)
    }

    @Test
    fun `only day before yesterday resets to zero`() {
        val dates = listOf(today.minusDays(2))
        val result = StreakCalculator.calculateStreak(dates, today)
        assertEquals(0, result)
    }

    @Test
    fun `duplicates do not break the streak`() {
        // Дубликаты одной и той же даты не должны увеличивать стрик
        val dates = listOf(today, today, today.minusDays(1), today.minusDays(1))
        val result = StreakCalculator.calculateStreak(dates, today)
        assertEquals(2, result)
    }

    @Test
    fun `future dates do not break current streak`() {
        // Будущие даты (если вдруг попали в базу) не должны ломать логику
        val dates = listOf(today.plusDays(5), today, today.minusDays(1))
        val result = StreakCalculator.calculateStreak(dates, today)
        assertEquals(2, result)
    }
}
