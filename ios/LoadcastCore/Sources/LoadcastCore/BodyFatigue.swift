/// 종목 → 부위 그룹 매핑과 부위별 피로 추정. docs/SPEC.md §7.
public enum BodyFatigue {

    public static let typeToGroups: [WorkoutType: Set<BodyGroup>] = [
        .crossfit: [.lower, .upper, .cardio],
        .running: [.lower, .cardio],
        .weight: [.lower, .upper, .core],
        .yoga: [.core],
        .swimming: [.upper, .cardio],
        .cycling: [.lower, .cardio],
        .hiking: [.lower, .cardio],
        .other: [.cardio],
    ]

    /// 최근 3일 [today-2, today] 그룹별 부하 합을 3구간으로 버킷링.
    public static func estimate(records: [WorkoutRecord], today: DayDate) -> [BodyGroup: FatigueLevel] {
        let from = today.adding(days: -2)
        var sums: [BodyGroup: Int] = Dictionary(uniqueKeysWithValues: BodyGroup.allCases.map { ($0, 0) })
        for record in records where record.date >= from && record.date <= today {
            for group in typeToGroups[record.type] ?? [] {
                sums[group, default: 0] += record.load
            }
        }
        return sums.mapValues(bucket)
    }

    private static func bucket(_ load: Int) -> FatigueLevel {
        switch load {
        case ..<100: .low
        case ..<250: .moderate
        default: .high
        }
    }
}
