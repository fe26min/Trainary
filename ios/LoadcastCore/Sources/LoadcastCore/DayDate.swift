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

    /// (연, 월, 일) 분해 (Howard Hinnant의 civil_from_days 알고리즘).
    public var components: (year: Int, month: Int, day: Int) {
        let z = daysSinceEpoch + 719_468
        let era = (z >= 0 ? z : z - 146_096) / 146_097
        let doe = z - era * 146_097
        let yoe = (doe - doe / 1460 + doe / 36_524 - doe / 146_096) / 365
        let y = yoe + era * 400
        let doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        let mp = (5 * doy + 2) / 153
        let day = doy - (153 * mp + 2) / 5 + 1
        let month = mp < 10 ? mp + 3 : mp - 9
        return (month <= 2 ? y + 1 : y, month, day)
    }

    /// "yyyy-MM-dd" 표현 (RecordCodec 및 UI 표기용).
    public var isoString: String {
        let (year, month, day) = components
        func pad(_ n: Int, _ width: Int) -> String {
            let s = String(n)
            return s.count >= width ? s : String(repeating: "0", count: width - s.count) + s
        }
        return "\(pad(year, 4))-\(pad(month, 2))-\(pad(day, 2))"
    }

    public static func < (lhs: DayDate, rhs: DayDate) -> Bool {
        lhs.daysSinceEpoch < rhs.daysSinceEpoch
    }
}
