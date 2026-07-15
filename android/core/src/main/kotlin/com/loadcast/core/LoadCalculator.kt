package com.loadcast.core

import java.time.LocalDate

object LoadCalculator {

    /** 부하(점) = 운동 시간(분) × 강도 계수. docs/SPEC.md §2. */
    fun sessionLoad(durationMin: Int, intensity: Intensity): Int =
        durationMin * intensity.coefficient

    /** [from, to] (양끝 포함) 윈도우의 부하 합. */
    fun totalLoad(records: List<WorkoutRecord>, from: LocalDate, to: LocalDate): Int =
        records.filter { it.date in from..to }.sumOf { it.load }
}
