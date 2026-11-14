package com.example.sht.data

import java.time.LocalDate

object StreakCalculator {

    fun calculateStreak(dates: List<LocalDate>, today: LocalDate): Int {
        if (dates.isEmpty()) return 0

        val unique = dates
            .filter { it <= today }
            .distinct()
            .sortedDescending()

        if (unique.isEmpty()) return 0

        val latest = unique.first()
        if (latest.isBefore(today.minusDays(1))) return 0

        // Идём по списку и считаем непрерывную последовательность
        var streak = 1
        var expected = latest.minusDays(1)
        for (i in 1 until unique.size) {
            if (unique[i] == expected) {
                streak++
                expected = expected.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }
}
