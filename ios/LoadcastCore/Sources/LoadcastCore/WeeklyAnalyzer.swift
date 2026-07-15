public struct WeeklySummary: Equatable, Sendable {
    public let totalMinutes: Int
    public let totalLoad: Int
    public let highCount: Int
    public let restDays: Int

    public init(totalMinutes: Int, totalLoad: Int, highCount: Int, restDays: Int) {
        self.totalMinutes = totalMinutes
        self.totalLoad = totalLoad
        self.highCount = highCount
        self.restDays = restDays
    }
}

public struct WeekComparison: Equatable, Sendable {
    public let minutesDelta: Int
    public let loadDelta: Int
    public let highCountDelta: Int

    public init(minutesDelta: Int, loadDelta: Int, highCountDelta: Int) {
        self.minutesDelta = minutesDelta
        self.loadDelta = loadDelta
        self.highCountDelta = highCountDelta
    }
}

public enum WeeklyAnalyzer {

    /// 급성 윈도우 [today-6, today] 요약. 휴식일 정의는 docs/SPEC.md §3.
    public static func summarize(records: [WorkoutRecord], today: DayDate) -> WeeklySummary {
        let from = today.adding(days: -6)
        let inWindow = records.filter { $0.date >= from && $0.date <= today }
        return WeeklySummary(
            totalMinutes: inWindow.reduce(0) { $0 + $1.durationMin },
            totalLoad: inWindow.reduce(0) { $0 + $1.load },
            highCount: inWindow.filter { $0.intensity == .high }.count,
            restDays: restDays(records: records, today: today)
        )
    }

    /// [today-6, today-1] 중 기록이 없는 날의 수. 오늘은 제외.
    public static func restDays(records: [WorkoutRecord], today: DayDate) -> Int {
        let datesWithRecord = Set(records.map(\.date))
        return (1...6).filter { !datesWithRecord.contains(today.adding(days: -$0)) }.count
    }

    /// 이전 기간 [today-13, today-7] 대비 증감.
    /// 이전 기간 기록이 0건이면 nil — UI는 "비교할 이전 기록이 아직 없어요" 표시 (docs/SPEC.md §6).
    public static func compareWithPrevious(records: [WorkoutRecord], today: DayDate) -> WeekComparison? {
        let prevFrom = today.adding(days: -13)
        let prevTo = today.adding(days: -7)
        let previous = records.filter { $0.date >= prevFrom && $0.date <= prevTo }
        guard !previous.isEmpty else { return nil }

        let curFrom = today.adding(days: -6)
        let current = records.filter { $0.date >= curFrom && $0.date <= today }
        return WeekComparison(
            minutesDelta: current.reduce(0) { $0 + $1.durationMin } - previous.reduce(0) { $0 + $1.durationMin },
            loadDelta: current.reduce(0) { $0 + $1.load } - previous.reduce(0) { $0 + $1.load },
            highCountDelta: current.filter { $0.intensity == .high }.count -
                previous.filter { $0.intensity == .high }.count
        )
    }
}
