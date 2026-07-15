/// 시간·타임존이 없는 순수 날짜.
/// 코어 로직이 Foundation의 `Calendar`/`TimeZone`에 의존하지 않도록
/// Kotlin `LocalDate`와 동일한 의미론을 제공한다 (docs/SPEC.md §8).
public struct DayDate: Hashable, Comparable, Sendable {

    /// 1970-01-01 기준 경과 일수.
    public let daysSinceEpoch: Int

    public init(daysSinceEpoch: Int) {
        self.daysSinceEpoch = daysSinceEpoch
    }

    /// 그레고리력 연·월·일로 생성 (Howard Hinnant의 days_from_civil 알고리즘).
    public init(year: Int, month: Int, day: Int) {
        let y = month <= 2 ? year - 1 : year
        let era = (y >= 0 ? y : y - 399) / 400
        let yoe = y - era * 400
        let mp = (month + 9) % 12
        let doy = (153 * mp + 2) / 5 + day - 1
        let doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        self.daysSinceEpoch = era * 146_097 + doe - 719_468
    }

    /// "yyyy-MM-dd" 파싱.
    public static func parse(_ iso: String) -> DayDate? {
        let parts = iso.split(separator: "-")
        guard parts.count == 3,
              let year = Int(parts[0]),
              let month = Int(parts[1]),
              let day = Int(parts[2]),
              (1...12).contains(month),
              (1...31).contains(day)
        else { return nil }
        return DayDate(year: year, month: month, day: day)
    }

    public func adding(days: Int) -> DayDate {
        DayDate(daysSinceEpoch: daysSinceEpoch + days)
    }

    public static func < (lhs: DayDate, rhs: DayDate) -> Bool {
        lhs.daysSinceEpoch < rhs.daysSinceEpoch
    }
}
