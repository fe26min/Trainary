public struct Recommendation: Equatable, Sendable {
    public let state: RecommendationState
    /// true면 ACWR을 쓸 수 없는 초기 기간 — UI에 "초기 추정" 배지.
    public let estimate: Bool
    /// 매치된 조건 코드, 규칙 순서대로 최대 3개.
    public let reasons: [ReasonCode]
    public let acwr: Double?
    public let loadStatus: LoadStatus
}

public enum RecommendationResult: Equatable, Sendable {
    /// 기록 3건 미만 — 홈 히어로를 "데이터 부족" 카드로 대체.
    case insufficientData(recordedCount: Int, requiredCount: Int)
    case recommendation(Recommendation)
}

/// 오늘 운동 추천 판정. 규칙 전문은 docs/SPEC.md §5.
public enum Recommender {

    public static let requiredRecords = 3
    private static let maxReasons = 3

    public static func recommend(
        records: [WorkoutRecord],
        today: DayDate,
        firstUseDate: DayDate
    ) -> RecommendationResult {
        guard records.count >= requiredRecords else {
            return .insufficientData(recordedCount: records.count, requiredCount: requiredRecords)
        }

        let acuteLoad = LoadCalculator.totalLoad(records: records, from: today.adding(days: -6), to: today)
        let chronicLoad = LoadCalculator.totalLoad(records: records, from: today.adding(days: -27), to: today)
        let acwrAvailable = firstUseDate <= today.adding(days: -27) && chronicLoad > 0
        let acwr = acwrAvailable ? Acwr.ratio(acuteLoad: acuteLoad, chronicLoad: chronicLoad) : nil

        let yesterday = today.adding(days: -1)
        let yesterdayHigh = records.contains { $0.date == yesterday && $0.intensity == .high }
        let threeDaysAgo = today.adding(days: -2)
        let highLast3Days = records.filter {
            $0.intensity == .high && $0.date >= threeDaysAgo && $0.date <= today
        }.count
        let weekAgo = today.adding(days: -6)
        let highLast7Days = records.filter {
            $0.intensity == .high && $0.date >= weekAgo && $0.date <= today
        }.count
        let restDays = WeeklyAnalyzer.restDays(records: records, today: today)

        // 1순위: 회복 권장
        var recoveryReasons: [ReasonCode] = []
        if let acwr, acwr > 1.5 { recoveryReasons.append(.acwrHigh) }
        if yesterdayHigh && highLast3Days >= 2 { recoveryReasons.append(.consecutiveHigh) }
        if restDays == 0 { recoveryReasons.append(.noRest) }
        if !recoveryReasons.isEmpty {
            return result(.recovery, recoveryReasons, acwr, acwrAvailable)
        }

        // 2순위: 중강도 권장
        var moderateReasons: [ReasonCode] = []
        if let acwr, acwr > 1.3, acwr <= 1.5 { moderateReasons.append(.acwrElevated) }
        if yesterdayHigh { moderateReasons.append(.yesterdayHigh) }
        if highLast7Days >= 2 { moderateReasons.append(.frequentHigh) }
        if !moderateReasons.isEmpty {
            return result(.moderate, moderateReasons, acwr, acwrAvailable)
        }

        // 3순위: 고강도 가능 (+ 부하 여유 보조 문구)
        var highOkReasons: [ReasonCode] = []
        if let acwr, acwr < 0.8 { highOkReasons.append(.acwrLowRoom) }
        return result(.highOk, highOkReasons, acwr, acwrAvailable)
    }

    private static func result(
        _ state: RecommendationState,
        _ reasons: [ReasonCode],
        _ acwr: Double?,
        _ acwrAvailable: Bool
    ) -> RecommendationResult {
        .recommendation(Recommendation(
            state: state,
            estimate: !acwrAvailable,
            reasons: Array(reasons.prefix(maxReasons)),
            acwr: acwr,
            loadStatus: Acwr.status(acwr)
        ))
    }
}
