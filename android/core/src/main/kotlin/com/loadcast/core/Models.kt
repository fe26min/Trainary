package com.loadcast.core

import java.time.LocalDate

/** 운동 강도 3단계. 계수는 docs/SPEC.md §2 (Foster sRPE 축약). */
enum class Intensity(val coefficient: Int) {
    LOW(1),
    MODERATE(2),
    HIGH(3),
}

enum class WorkoutType {
    CROSSFIT, RUNNING, WEIGHT, YOGA, SWIMMING, CYCLING, HIKING, OTHER,
}

enum class BodyGroup { LOWER, UPPER, CORE, CARDIO }

enum class FatigueLevel { LOW, MODERATE, HIGH }

/** 분석 탭 부하 상태 4단계 + 미정의. docs/SPEC.md §4. */
enum class LoadStatus { LOW, OPTIMAL, CAUTION, OVERLOAD, UNKNOWN }

enum class RecommendationState { HIGH_OK, MODERATE, RECOVERY }

/** 추천 이유 코드. 사용자 노출 문구 매핑은 앱 레이어 책임. */
enum class ReasonCode {
    ACWR_HIGH,
    CONSECUTIVE_HIGH,
    NO_REST,
    ACWR_ELEVATED,
    YESTERDAY_HIGH,
    FREQUENT_HIGH,
    ACWR_LOW_ROOM,
}

data class WorkoutRecord(
    val date: LocalDate,
    val type: WorkoutType,
    val durationMin: Int,
    val intensity: Intensity,
    val customTypeLabel: String? = null,
    val satisfaction: Int? = null,
    val memo: String? = null,
) {
    /** 부하는 저장하지 않고 항상 계산한다 (docs/SPEC.md §1). */
    val load: Int get() = LoadCalculator.sessionLoad(durationMin, intensity)
}
