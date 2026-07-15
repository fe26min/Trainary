package com.loadcast.core

import java.time.LocalDate

data class WeeklySummary(
    val totalMinutes: Int,
    val totalLoad: Int,
    val highCount: Int,
    val restDays: Int,
)

data class WeekComparison(
    val minutesDelta: Int,
    val loadDelta: Int,
    val highCountDelta: Int,
)

object WeeklyAnalyzer {

    /** 급성 윈도우 [today-6, today] 요약. 휴식일 정의는 docs/SPEC.md §3. */
    fun summarize(records: List<WorkoutRecord>, today: LocalDate): WeeklySummary {
        val from = today.minusDays(6)
        val inWindow = records.filter { it.date in from..today }
        return WeeklySummary(
            totalMinutes = inWindow.sumOf { it.durationMin },
            totalLoad = inWindow.sumOf { it.load },
            highCount = inWindow.count { it.intensity == Intensity.HIGH },
            restDays = restDays(records, today),
        )
    }

    /** [today-6, today-1] 중 기록이 없는 날의 수. 오늘은 제외. */
    fun restDays(records: List<WorkoutRecord>, today: LocalDate): Int {
        val datesWithRecord = records.mapTo(HashSet()) { it.date }
        return (1..6L).count { today.minusDays(it) !in datesWithRecord }
    }

    /**
     * 이전 기간 [today-13, today-7] 대비 증감.
     * 이전 기간 기록이 0건이면 null — UI는 "비교할 이전 기록이 아직 없어요" 표시 (docs/SPEC.md §6).
     */
    fun compareWithPrevious(records: List<WorkoutRecord>, today: LocalDate): WeekComparison? {
        val prevFrom = today.minusDays(13)
        val prevTo = today.minusDays(7)
        val previous = records.filter { it.date in prevFrom..prevTo }
        if (previous.isEmpty()) return null

        val current = records.filter { it.date in today.minusDays(6)..today }
        return WeekComparison(
            minutesDelta = current.sumOf { it.durationMin } - previous.sumOf { it.durationMin },
            loadDelta = current.sumOf { it.load } - previous.sumOf { it.load },
            highCountDelta = current.count { it.intensity == Intensity.HIGH } -
                previous.count { it.intensity == Intensity.HIGH },
        )
    }
}
