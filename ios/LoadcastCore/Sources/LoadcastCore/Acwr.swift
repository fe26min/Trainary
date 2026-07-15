public enum Acwr {

    /// ACWR = 급성(7일) 부하 ÷ (만성(28일) 부하 ÷ 4).
    /// 만성 부하가 0 이하이면 미정의(nil). docs/SPEC.md §4.
    public static func ratio(acuteLoad: Int, chronicLoad: Int) -> Double? {
        guard chronicLoad > 0 else { return nil }
        return Double(acuteLoad) / (Double(chronicLoad) / 4.0)
    }

    /// 경계 포함 규칙은 docs/SPEC.md §4 표와 동일.
    public static func status(_ acwr: Double?) -> LoadStatus {
        guard let acwr else { return .unknown }
        switch acwr {
        case ..<0.8: return .low
        case ...1.3: return .optimal
        case ...1.5: return .caution
        default: return .overload
        }
    }
}
