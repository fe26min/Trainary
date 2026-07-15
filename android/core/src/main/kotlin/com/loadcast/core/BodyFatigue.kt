package com.loadcast.core

import java.time.LocalDate

/** 종목 → 부위 그룹 매핑과 부위별 피로 추정. docs/SPEC.md §7. */
object BodyFatigue {

    val typeToGroups: Map<WorkoutType, Set<BodyGroup>> = mapOf(
        WorkoutType.CROSSFIT to setOf(BodyGroup.LOWER, BodyGroup.UPPER, BodyGroup.CARDIO),
        WorkoutType.RUNNING to setOf(BodyGroup.LOWER, BodyGroup.CARDIO),
        WorkoutType.WEIGHT to setOf(BodyGroup.LOWER, BodyGroup.UPPER, BodyGroup.CORE),
        WorkoutType.YOGA to setOf(BodyGroup.CORE),
        WorkoutType.SWIMMING to setOf(BodyGroup.UPPER, BodyGroup.CARDIO),
        WorkoutType.CYCLING to setOf(BodyGroup.LOWER, BodyGroup.CARDIO),
        WorkoutType.HIKING to setOf(BodyGroup.LOWER, BodyGroup.CARDIO),
        WorkoutType.OTHER to setOf(BodyGroup.CARDIO),
    )

    /** 최근 3일 [today-2, today] 그룹별 부하 합을 3구간으로 버킷링. */
    fun estimate(records: List<WorkoutRecord>, today: LocalDate): Map<BodyGroup, FatigueLevel> {
        val from = today.minusDays(2)
        val sums = BodyGroup.entries.associateWithTo(LinkedHashMap()) { 0 }
        records.filter { it.date in from..today }.forEach { record ->
            typeToGroups.getValue(record.type).forEach { group ->
                sums[group] = sums.getValue(group) + record.load
            }
        }
        return sums.mapValues { (_, load) -> bucket(load) }
    }

    private fun bucket(load: Int): FatigueLevel = when {
        load < 100 -> FatigueLevel.LOW
        load < 250 -> FatigueLevel.MODERATE
        else -> FatigueLevel.HIGH
    }
}
