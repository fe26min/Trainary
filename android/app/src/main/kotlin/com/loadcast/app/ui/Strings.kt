package com.loadcast.app.ui

import com.loadcast.core.FatigueLevel
import com.loadcast.core.Intensity
import com.loadcast.core.LoadStatus
import com.loadcast.core.ReasonCode
import com.loadcast.core.RecommendationState
import com.loadcast.core.WorkoutRecord
import com.loadcast.core.WorkoutType
import java.time.LocalDate

/** 코어의 코드/enum → 사용자 노출 한국어 문구 매핑. (docs/SPEC.md §5 — 단정적 의학 표현 금지) */
object Strings {

    fun typeLabel(type: WorkoutType): String = when (type) {
        WorkoutType.CROSSFIT -> "크로스핏"
        WorkoutType.RUNNING -> "러닝"
        WorkoutType.WEIGHT -> "웨이트"
        WorkoutType.YOGA -> "요가"
        WorkoutType.SWIMMING -> "수영"
        WorkoutType.CYCLING -> "사이클"
        WorkoutType.HIKING -> "등산"
        WorkoutType.OTHER -> "기타"
    }

    fun recordTypeLabel(record: WorkoutRecord): String =
        record.customTypeLabel?.takeIf { it.isNotBlank() } ?: typeLabel(record.type)

    fun intensityLabel(intensity: Intensity): String = when (intensity) {
        Intensity.LOW -> "낮음"
        Intensity.MODERATE -> "중간"
        Intensity.HIGH -> "높음"
    }

    fun intensityDescription(intensity: Intensity): String = when (intensity) {
        Intensity.LOW -> "대화 가능한 회복 운동"
        Intensity.MODERATE -> "땀이 나고 집중이 필요"
        Intensity.HIGH -> "숨이 차고 회복 부담 큼"
    }

    fun stateLabel(state: RecommendationState): String = when (state) {
        RecommendationState.HIGH_OK -> "고강도 가능"
        RecommendationState.MODERATE -> "중강도 권장"
        RecommendationState.RECOVERY -> "회복 권장"
    }

    fun stateHeadline(state: RecommendationState): String = when (state) {
        RecommendationState.HIGH_OK -> "권장 강도 · 높음"
        RecommendationState.MODERATE -> "권장 강도 · 중간"
        RecommendationState.RECOVERY -> "권장 강도 · 회복"
    }

    fun stateDescription(state: RecommendationState): String = when (state) {
        RecommendationState.HIGH_OK ->
            "오늘은 고강도 운동이 가능한 상태로 보여요. 컨디션을 살피며 강도 높은 훈련을 진행해도 좋아요."
        RecommendationState.MODERATE ->
            "오늘은 중간 강도 운동을 권장해요. Zone 2 러닝이나 가벼운 웨이트가 좋아요."
        RecommendationState.RECOVERY ->
            "오늘은 회복을 권장해요. 가벼운 움직임과 휴식으로 몸을 회복시켜 주세요."
    }

    fun reasonLabel(code: ReasonCode): String = when (code) {
        ReasonCode.ACWR_HIGH -> "최근 7일 부하가 평소보다 크게 높음"
        ReasonCode.CONSECUTIVE_HIGH -> "고강도 운동 연속 수행"
        ReasonCode.NO_REST -> "최근 7일 휴식일 없음"
        ReasonCode.ACWR_ELEVATED -> "최근 7일 부하가 평소보다 다소 높음"
        ReasonCode.YESTERDAY_HIGH -> "어제 고강도 운동 수행"
        ReasonCode.FREQUENT_HIGH -> "최근 7일 고강도 운동 2회 이상"
        ReasonCode.ACWR_LOW_ROOM -> "운동량을 조금 늘려도 좋아요"
    }

    /** 추천 운동 3종 — (이름, 상세). 디자인 화면 09. */
    fun suggestedWorkouts(state: RecommendationState): List<Pair<String, String>> = when (state) {
        RecommendationState.HIGH_OK -> listOf(
            "인터벌 러닝" to "20–40분 · 컨디션에 맞게",
            "고강도 웨이트" to "45–60분 · 충분한 세트 간 휴식",
            "크로스핏 WOD" to "40–60분 · 폼 유지 가능한 범위",
        )
        RecommendationState.MODERATE -> listOf(
            "Zone 2 러닝" to "30–45분 · 대화 가능한 페이스",
            "가벼운 웨이트" to "40–60분 · 낮은 중량·고반복",
            "요가 · 스트레칭" to "30분 · 회복 위주",
        )
        RecommendationState.RECOVERY -> listOf(
            "걷기" to "20–30분 · 낮은 심박 유지",
            "요가 · 스트레칭" to "20–30분 · 이완 위주",
            "폼롤러 · 모빌리티" to "15분 · 뭉친 부위 위주",
        )
    }

    fun cautionText(state: RecommendationState): String? = when (state) {
        RecommendationState.HIGH_OK -> null
        RecommendationState.MODERATE -> "오늘은 고강도 인터벌과 경쟁성 WOD를 피하는 것이 좋아요."
        RecommendationState.RECOVERY -> "오늘은 중강도 이상 운동을 피하는 것이 좋아요."
    }

    const val DISCLAIMER = "본 추천은 기록 기반 참고 정보이며 의학적 판단이 아닙니다. 몸 상태를 우선 살펴주세요."

    fun loadStatusLabel(status: LoadStatus): String = when (status) {
        LoadStatus.LOW -> "낮음"
        LoadStatus.OPTIMAL -> "적정"
        LoadStatus.CAUTION -> "주의"
        LoadStatus.OVERLOAD -> "과부하"
        LoadStatus.UNKNOWN -> "데이터 부족"
    }

    fun loadStatusDescription(status: LoadStatus): String = when (status) {
        LoadStatus.LOW -> "최근 7일 운동량이 평소보다 적어요. 조금씩 늘려도 좋아요."
        LoadStatus.OPTIMAL -> "최근 7일 운동 부하가 적정 범위예요. 지금 페이스를 유지해 보세요."
        LoadStatus.CAUTION -> "최근 7일 운동량이 평소보다 늘었어요. 오늘은 중강도 이하를 권장해요."
        LoadStatus.OVERLOAD -> "최근 7일 운동량이 평소보다 크게 늘었어요. 회복을 우선해 주세요."
        LoadStatus.UNKNOWN -> "기록이 4주 쌓이면 부하 상태를 분석할 수 있어요."
    }

    fun fatigueLabel(level: FatigueLevel): String = when (level) {
        FatigueLevel.LOW -> "낮음"
        FatigueLevel.MODERATE -> "보통"
        FatigueLevel.HIGH -> "높음"
    }

    private val WEEKDAYS_KO = arrayOf("월", "화", "수", "목", "금", "토", "일")

    fun weekdayShort(date: LocalDate): String = WEEKDAYS_KO[date.dayOfWeek.value - 1]

    fun longDate(date: LocalDate): String =
        "${date.year}년 ${date.monthValue}월 ${date.dayOfMonth}일 ${weekdayShort(date)}요일"

    fun shortDate(date: LocalDate): String = "${date.monthValue}월 ${date.dayOfMonth}일"

    fun formDate(date: LocalDate): String {
        val mm = date.monthValue.toString().padStart(2, '0')
        val dd = date.dayOfMonth.toString().padStart(2, '0')
        return "${date.year}. $mm. $dd (${weekdayShort(date)})"
    }
}
