public enum LoadCalculator {

    /// 부하(점) = 운동 시간(분) × 강도 계수. docs/SPEC.md §2.
    public static func sessionLoad(durationMin: Int, intensity: Intensity) -> Int {
        durationMin * intensity.coefficient
    }

    /// [from, to] (양끝 포함) 윈도우의 부하 합.
    public static func totalLoad(records: [WorkoutRecord], from: DayDate, to: DayDate) -> Int {
        records
            .filter { $0.date >= from && $0.date <= to }
            .reduce(0) { $0 + $1.load }
    }
}
