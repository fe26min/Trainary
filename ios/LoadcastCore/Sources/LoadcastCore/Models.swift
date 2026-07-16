import Foundation

/// 운동 강도 3단계. 계수는 docs/SPEC.md §2 (Foster sRPE 축약).
public enum Intensity: String, CaseIterable, Sendable {
    case low = "LOW"
    case moderate = "MODERATE"
    case high = "HIGH"

    public var coefficient: Int {
        switch self {
        case .low: 1
        case .moderate: 2
        case .high: 3
        }
    }
}

public enum WorkoutType: String, CaseIterable, Sendable {
    case crossfit = "CROSSFIT"
    case running = "RUNNING"
    case weight = "WEIGHT"
    case yoga = "YOGA"
    case swimming = "SWIMMING"
    case cycling = "CYCLING"
    case hiking = "HIKING"
    case other = "OTHER"
}

public enum BodyGroup: String, CaseIterable, Sendable {
    case lower = "LOWER"
    case upper = "UPPER"
    case core = "CORE"
    case cardio = "CARDIO"
}

public enum FatigueLevel: String, CaseIterable, Sendable {
    case low = "LOW"
    case moderate = "MODERATE"
    case high = "HIGH"
}

/// 분석 탭 부하 상태 4단계 + 미정의. docs/SPEC.md §4.
public enum LoadStatus: String, CaseIterable, Sendable {
    case low = "LOW"
    case optimal = "OPTIMAL"
    case caution = "CAUTION"
    case overload = "OVERLOAD"
    case unknown = "UNKNOWN"
}

public enum RecommendationState: String, CaseIterable, Sendable {
    case highOk = "HIGH_OK"
    case moderate = "MODERATE"
    case recovery = "RECOVERY"
}

/// 추천 이유 코드. 사용자 노출 문구 매핑은 앱 레이어 책임.
public enum ReasonCode: String, CaseIterable, Sendable {
    case acwrHigh = "ACWR_HIGH"
    case consecutiveHigh = "CONSECUTIVE_HIGH"
    case noRest = "NO_REST"
    case acwrElevated = "ACWR_ELEVATED"
    case yesterdayHigh = "YESTERDAY_HIGH"
    case frequentHigh = "FREQUENT_HIGH"
    case acwrLowRoom = "ACWR_LOW_ROOM"
}

public struct WorkoutRecord: Hashable, Sendable, Identifiable {
    public let id: String
    public let date: DayDate
    public let type: WorkoutType
    public let durationMin: Int
    public let intensity: Intensity
    public let customTypeLabel: String?
    public let satisfaction: Int?
    public let memo: String?

    public init(
        date: DayDate,
        type: WorkoutType,
        durationMin: Int,
        intensity: Intensity,
        id: String = UUID().uuidString,
        customTypeLabel: String? = nil,
        satisfaction: Int? = nil,
        memo: String? = nil
    ) {
        self.id = id
        self.date = date
        self.type = type
        self.durationMin = durationMin
        self.intensity = intensity
        self.customTypeLabel = customTypeLabel
        self.satisfaction = satisfaction
        self.memo = memo
    }

    /// 부하는 저장하지 않고 항상 계산한다 (docs/SPEC.md §1).
    public var load: Int {
        LoadCalculator.sessionLoad(durationMin: durationMin, intensity: intensity)
    }
}
