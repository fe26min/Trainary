import SwiftUI
import LoadcastCore

/// 각진 모서리 흰색 카드 (Industry 디자인 시스템 — radius 0) + 선택적 상단 액센트 스트립.
struct SquareCard<Content: View>: View {
    var topAccent: Color?
    @ViewBuilder var content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            content
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(LoadcastColors.surface)
        .overlay(alignment: .top) {
            if let topAccent { Rectangle().fill(topAccent).frame(height: 4) }
        }
        .overlay(Rectangle().stroke(LoadcastColors.divider, lineWidth: 1))
    }
}

/// 상태 배지 — 색만으로 구분하지 않도록 항상 텍스트 라벨 포함 (디자인 접근성 원칙).
struct StatusBadge: View {
    let text: String
    let palette: StatusPalette

    var body: some View {
        Text(text)
            .font(.caption.weight(.semibold))
            .foregroundStyle(palette.onTint)
            .padding(.horizontal, 10)
            .padding(.vertical, 5)
            .background(palette.tint)
            .overlay(Rectangle().stroke(palette.main.opacity(0.45), lineWidth: 1))
    }
}

/// 각진 주 실행 버튼.
struct PrimaryButton: View {
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.headline)
                .foregroundStyle(LoadcastColors.surface)
                .frame(maxWidth: .infinity)
                .frame(height: 52)
                .background(LoadcastColors.accent)
        }
        .buttonStyle(.plain)
    }
}

struct SecondaryButton: View {
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline)
                .foregroundStyle(LoadcastColors.neutral800)
                .frame(maxWidth: .infinity)
                .frame(height: 46)
                .background(LoadcastColors.surface)
                .overlay(Rectangle().stroke(LoadcastColors.divider, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}

/// 선택 칩.
struct Chip: View {
    let text: String
    let selected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(text)
                .font(.subheadline)
                .foregroundStyle(selected ? LoadcastColors.surface : LoadcastColors.neutral700)
                .padding(.horizontal, 14)
                .padding(.vertical, 7)
                .background(selected ? LoadcastColors.accent : LoadcastColors.surface)
                .overlay(Rectangle().stroke(selected ? LoadcastColors.accent : LoadcastColors.divider, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}

struct FieldLabel: View {
    let text: String
    init(_ text: String) { self.text = text }
    var body: some View {
        Text(text).font(.subheadline).foregroundStyle(LoadcastColors.neutral700)
    }
}
