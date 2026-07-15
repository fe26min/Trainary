package com.loadcast.core

object Acwr {

    /**
     * ACWR = 급성(7일) 부하 ÷ (만성(28일) 부하 ÷ 4).
     * 만성 부하가 0 이하이면 미정의(null). docs/SPEC.md §4.
     */
    fun ratio(acuteLoad: Int, chronicLoad: Int): Double? =
        if (chronicLoad <= 0) null else acuteLoad / (chronicLoad / 4.0)

    /** 경계 포함 규칙은 docs/SPEC.md §4 표와 동일. */
    fun status(acwr: Double?): LoadStatus = when {
        acwr == null -> LoadStatus.UNKNOWN
        acwr < 0.8 -> LoadStatus.LOW
        acwr <= 1.3 -> LoadStatus.OPTIMAL
        acwr <= 1.5 -> LoadStatus.CAUTION
        else -> LoadStatus.OVERLOAD
    }
}
