package com.loadcast.core

import java.time.LocalDate

sealed interface RecommendationResult {

    /** 기록 3건 미만 — 홈 히어로를 "데이터 부족" 카드로 대체. */
    data class InsufficientData(
        val recordedCount: Int,
        val requiredCount: Int = Recommender.REQUIRED_RECORDS,
    ) : RecommendationResult

    data class Recommendation(
        val state: RecommendationState,
        /** true면 ACWR을 쓸 수 없는 초기 기간 — UI에 "초기 추정" 배지. */
        val estimate: Boolean,
        /** 매치된 조건 코드, 규칙 순서대로 최대 3개. */
        val reasons: List<ReasonCode>,
        val acwr: Double?,
        val loadStatus: LoadStatus,
    ) : RecommendationResult
}

/** 오늘 운동 추천 판정. 규칙 전문은 docs/SPEC.md §5. */
object Recommender {

    const val REQUIRED_RECORDS = 3
    private const val MAX_REASONS = 3

    fun recommend(
        records: List<WorkoutRecord>,
        today: LocalDate,
        firstUseDate: LocalDate,
    ): RecommendationResult {
        if (records.size < REQUIRED_RECORDS) {
            return RecommendationResult.InsufficientData(recordedCount = records.size)
        }

        val acuteLoad = LoadCalculator.totalLoad(records, today.minusDays(6), today)
        val chronicLoad = LoadCalculator.totalLoad(records, today.minusDays(27), today)
        val acwrAvailable = !firstUseDate.isAfter(today.minusDays(27)) && chronicLoad > 0
        val acwr = if (acwrAvailable) Acwr.ratio(acuteLoad, chronicLoad) else null

        val yesterdayHigh = records.any {
            it.date == today.minusDays(1) && it.intensity == Intensity.HIGH
        }
        val highLast3Days = records.count {
            it.intensity == Intensity.HIGH && it.date in today.minusDays(2)..today
        }
        val highLast7Days = records.count {
            it.intensity == Intensity.HIGH && it.date in today.minusDays(6)..today
        }
        val restDays = WeeklyAnalyzer.restDays(records, today)

        // 1순위: 회복 권장
        val recoveryReasons = buildList {
            if (acwr != null && acwr > 1.5) add(ReasonCode.ACWR_HIGH)
            if (yesterdayHigh && highLast3Days >= 2) add(ReasonCode.CONSECUTIVE_HIGH)
            if (restDays == 0) add(ReasonCode.NO_REST)
        }
        if (recoveryReasons.isNotEmpty()) {
            return result(RecommendationState.RECOVERY, recoveryReasons, acwr, acwrAvailable)
        }

        // 2순위: 중강도 권장
        val moderateReasons = buildList {
            if (acwr != null && acwr > 1.3 && acwr <= 1.5) add(ReasonCode.ACWR_ELEVATED)
            if (yesterdayHigh) add(ReasonCode.YESTERDAY_HIGH)
            if (highLast7Days >= 2) add(ReasonCode.FREQUENT_HIGH)
        }
        if (moderateReasons.isNotEmpty()) {
            return result(RecommendationState.MODERATE, moderateReasons, acwr, acwrAvailable)
        }

        // 3순위: 고강도 가능 (+ 부하 여유 보조 문구)
        val highOkReasons =
            if (acwr != null && acwr < 0.8) listOf(ReasonCode.ACWR_LOW_ROOM) else emptyList()
        return result(RecommendationState.HIGH_OK, highOkReasons, acwr, acwrAvailable)
    }

    private fun result(
        state: RecommendationState,
        reasons: List<ReasonCode>,
        acwr: Double?,
        acwrAvailable: Boolean,
    ) = RecommendationResult.Recommendation(
        state = state,
        estimate = !acwrAvailable,
        reasons = reasons.take(MAX_REASONS),
        acwr = acwr,
        loadStatus = Acwr.status(acwr),
    )
}
