import SwiftUI
import LoadcastCore

/// 디자인 토큰 — 클로드 디자인 "운동 부하 분석 앱 설계" 컬러 섹션.
/// 값은 이 파일에서만 정의하고 화면 코드에 하드코딩하지 않는다.
/// TODO: Pretendard(한글) + Barlow Condensed(숫자·영문) 폰트 페어링 적용 (docs/SPEC.md 참고)
enum LoadcastColors {
    // 기본
    static let bg = Color(hex: 0xE8EBF1)
    static let surface = Color(hex: 0xFFFFFF)
    static let text = Color(hex: 0x141822)
    static let divider = Color(hex: 0x141822).opacity(0.10)

    // 액센트(스틸 블루) 램프
    static let accent = Color(hex: 0x2F62EA)
    static let accent100 = Color(hex: 0xEEF2FE)
    static let accent200 = Color(hex: 0xDBE4FD)
    static let accent300 = Color(hex: 0xBCCBFA)
    static let accent700 = Color(hex: 0x2F4FC2)
    static let accent800 = Color(hex: 0x26408F)
    static let accent900 = Color(hex: 0x1E3160)

    // 준비도 3색 — 고강도 가능 / 중강도 권장 / 회복 권장
    static let ok = Color(hex: 0x12A35D)
    static let ok100 = Color(hex: 0xE7F6EE)
    static let ok300 = Color(hex: 0xA6DDBF)
    static let ok700 = Color(hex: 0x0C7D47)
    static let ok800 = Color(hex: 0x0A5F37)

    static let warn = Color(hex: 0xE2891B)
    static let warn100 = Color(hex: 0xFDF2DF)
    static let warn300 = Color(hex: 0xF3CE8D)
    static let warn700 = Color(hex: 0xA5620F)
    static let warn800 = Color(hex: 0x7C4A0C)

    static let rest = Color(hex: 0xE0574A)
    static let rest100 = Color(hex: 0xFDECEB)
    static let rest300 = Color(hex: 0xF2B2AB)
    static let rest700 = Color(hex: 0xB23A2E)
    static let rest800 = Color(hex: 0x8A2B21)

    // 뉴트럴 램프
    static let neutral100 = Color(hex: 0xF5F5F8)
    static let neutral200 = Color(hex: 0xE7E7EA)
    static let neutral300 = Color(hex: 0xD4D4D7)
    static let neutral400 = Color(hex: 0xB7B7BA)
    static let neutral500 = Color(hex: 0x98989B)
    static let neutral600 = Color(hex: 0x7A7A7D)
    static let neutral700 = Color(hex: 0x5D5D60)
    static let neutral800 = Color(hex: 0x424244)
    static let neutral900 = Color(hex: 0x2B2B2D)
}

/// 준비도/강도/부하 상태 계열 색 묶음 — (본색, 100 틴트, 800 텍스트).
struct StatusPalette {
    let main: Color
    let tint: Color
    let onTint: Color
}

enum Palette {
    static func of(_ state: RecommendationState) -> StatusPalette {
        switch state {
        case .highOk: StatusPalette(main: LoadcastColors.ok, tint: LoadcastColors.ok100, onTint: LoadcastColors.ok800)
        case .moderate: StatusPalette(main: LoadcastColors.warn, tint: LoadcastColors.warn100, onTint: LoadcastColors.warn800)
        case .recovery: StatusPalette(main: LoadcastColors.rest, tint: LoadcastColors.rest100, onTint: LoadcastColors.rest800)
        }
    }

    static func of(_ intensity: Intensity) -> StatusPalette {
        switch intensity {
        case .low: StatusPalette(main: LoadcastColors.ok, tint: LoadcastColors.ok100, onTint: LoadcastColors.ok800)
        case .moderate: StatusPalette(main: LoadcastColors.warn, tint: LoadcastColors.warn100, onTint: LoadcastColors.warn800)
        case .high: StatusPalette(main: LoadcastColors.rest, tint: LoadcastColors.rest100, onTint: LoadcastColors.rest800)
        }
    }

    static func of(_ status: LoadStatus) -> StatusPalette {
        switch status {
        case .low, .optimal: StatusPalette(main: LoadcastColors.ok, tint: LoadcastColors.ok100, onTint: LoadcastColors.ok800)
        case .caution: StatusPalette(main: LoadcastColors.warn, tint: LoadcastColors.warn100, onTint: LoadcastColors.warn800)
        case .overload: StatusPalette(main: LoadcastColors.rest, tint: LoadcastColors.rest100, onTint: LoadcastColors.rest800)
        case .unknown: StatusPalette(main: LoadcastColors.neutral500, tint: LoadcastColors.neutral100, onTint: LoadcastColors.neutral700)
        }
    }

    static func of(_ level: FatigueLevel) -> StatusPalette {
        switch level {
        case .low: of(Intensity.low)
        case .moderate: of(Intensity.moderate)
        case .high: of(Intensity.high)
        }
    }
}

extension Color {
    init(hex: UInt32) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255,
            opacity: 1
        )
    }
}
