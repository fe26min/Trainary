import Foundation
import LoadcastCore

/// 코어의 코드/enum → 사용자 노출 한국어 문구 매핑. (docs/SPEC.md §5 — 단정적 의학 표현 금지)
/// Android의 Strings.kt와 동일한 문구를 유지한다.
enum Strings {

    static func typeLabel(_ type: WorkoutType) -> String {
        switch type {
        case .crossfit: "크로스핏"
        case .running: "러닝"
        case .weight: "웨이트"
        case .yoga: "요가"
        case .swimming: "수영"
        case .cycling: "사이클"
        case .hiking: "등산"
        case .other: "기타"
        }
    }

    static func recordTypeLabel(_ record: WorkoutRecord) -> String {
        if let label = record.customTypeLabel, !label.isEmpty { return label }
        return typeLabel(record.type)
    }

    static func intensityLabel(_ intensity: Intensity) -> String {
        switch intensity {
        case .low: "낮음"
        case .moderate: "중간"
        case .high: "높음"
        }
    }

    static func intensityDescription(_ intensity: Intensity) -> String {
        switch intensity {
        case .low: "대화 가능한 회복 운동"
        case .moderate: "땀이 나고 집중이 필요"
        case .high: "숨이 차고 회복 부담 큼"
        }
    }

    static func stateLabel(_ state: RecommendationState) -> String {
        switch state {
        case .highOk: "고강도 가능"
        case .moderate: "중강도 권장"
        case .recovery: "회복 권장"
        }
    }

    static func stateHeadline(_ state: RecommendationState) -> String {
        switch state {
        case .highOk: "권장 강도 · 높음"
        case .moderate: "권장 강도 · 중간"
        case .recovery: "권장 강도 · 회복"
        }
    }

    static func stateDescription(_ state: RecommendationState) -> String {
        switch state {
        case .highOk: "오늘은 고강도 운동이 가능한 상태로 보여요. 컨디션을 살피며 강도 높은 훈련을 진행해도 좋아요."
        case .moderate: "오늘은 중간 강도 운동을 권장해요. Zone 2 러닝이나 가벼운 웨이트가 좋아요."
        case .recovery: "오늘은 회복을 권장해요. 가벼운 움직임과 휴식으로 몸을 회복시켜 주세요."
        }
    }

    static func reasonLabel(_ code: ReasonCode) -> String {
        switch code {
        case .acwrHigh: "최근 7일 부하가 평소보다 크게 높음"
        case .consecutiveHigh: "고강도 운동 연속 수행"
        case .noRest: "최근 7일 휴식일 없음"
        case .acwrElevated: "최근 7일 부하가 평소보다 다소 높음"
        case .yesterdayHigh: "어제 고강도 운동 수행"
        case .frequentHigh: "최근 7일 고강도 운동 2회 이상"
        case .acwrLowRoom: "운동량을 조금 늘려도 좋아요"
        }
    }

    static func suggestedWorkouts(_ state: RecommendationState) -> [(String, String)] {
        switch state {
        case .highOk: [
            ("인터벌 러닝", "20–40분 · 컨디션에 맞게"),
            ("고강도 웨이트", "45–60분 · 충분한 세트 간 휴식"),
            ("크로스핏 WOD", "40–60분 · 폼 유지 가능한 범위"),
        ]
        case .moderate: [
            ("Zone 2 러닝", "30–45분 · 대화 가능한 페이스"),
            ("가벼운 웨이트", "40–60분 · 낮은 중량·고반복"),
            ("요가 · 스트레칭", "30분 · 회복 위주"),
        ]
        case .recovery: [
            ("걷기", "20–30분 · 낮은 심박 유지"),
            ("요가 · 스트레칭", "20–30분 · 이완 위주"),
            ("폼롤러 · 모빌리티", "15분 · 뭉친 부위 위주"),
        ]
        }
    }

    static func cautionText(_ state: RecommendationState) -> String? {
        switch state {
        case .highOk: nil
        case .moderate: "오늘은 고강도 인터벌과 경쟁성 WOD를 피하는 것이 좋아요."
        case .recovery: "오늘은 중강도 이상 운동을 피하는 것이 좋아요."
        }
    }

    static let disclaimer = "본 추천은 기록 기반 참고 정보이며 의학적 판단이 아닙니다. 몸 상태를 우선 살펴주세요."

    static func loadStatusLabel(_ status: LoadStatus) -> String {
        switch status {
        case .low: "낮음"
        case .optimal: "적정"
        case .caution: "주의"
        case .overload: "과부하"
        case .unknown: "데이터 부족"
        }
    }

    static func loadStatusDescription(_ status: LoadStatus) -> String {
        switch status {
        case .low: "최근 7일 운동량이 평소보다 적어요. 조금씩 늘려도 좋아요."
        case .optimal: "최근 7일 운동 부하가 적정 범위예요. 지금 페이스를 유지해 보세요."
        case .caution: "최근 7일 운동량이 평소보다 늘었어요. 오늘은 중강도 이하를 권장해요."
        case .overload: "최근 7일 운동량이 평소보다 크게 늘었어요. 회복을 우선해 주세요."
        case .unknown: "기록이 4주 쌓이면 부하 상태를 분석할 수 있어요."
        }
    }

    static func fatigueLabel(_ level: FatigueLevel) -> String {
        switch level {
        case .low: "낮음"
        case .moderate: "보통"
        case .high: "높음"
        }
    }

    private static let weekdaysKo = ["월", "화", "수", "목", "금", "토", "일"]

    /// DayDate의 요일 — 1970-01-01(목)을 기준으로 계산 (Foundation Calendar 미사용).
    static func weekdayShort(_ date: DayDate) -> String {
        // 1970-01-01 = 목요일(월=0 기준 index 3)
        let index = ((date.daysSinceEpoch % 7) + 7 + 3) % 7
        return weekdaysKo[index]
    }

    static func longDate(_ date: DayDate) -> String {
        let c = date.components
        return "\(c.year)년 \(c.month)월 \(c.day)일 \(weekdayShort(date))요일"
    }

    static func shortDate(_ date: DayDate) -> String {
        let c = date.components
        return "\(c.month)월 \(c.day)일"
    }

    static func formDate(_ date: DayDate) -> String {
        let c = date.components
        let mm = String(format: "%02d", c.month)
        let dd = String(format: "%02d", c.day)
        return "\(c.year). \(mm). \(dd) (\(weekdayShort(date)))"
    }
}
