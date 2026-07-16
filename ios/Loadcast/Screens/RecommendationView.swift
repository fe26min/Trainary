import SwiftUI
import LoadcastCore

/// 오늘 운동 추천 상세 — 디자인 화면 09.
/// 상태·권장 강도 · 설명 · 이유(≤3) · 추천 운동(≤3) · 주의 운동 · 면책 문구.
struct RecommendationView: View {
    @EnvironmentObject var store: RecordStore
    @Environment(\.dismiss) private var dismiss
    private let today = Today.value()

    private var result: RecommendationResult {
        Recommender.recommend(records: store.records, today: today, firstUseDate: store.firstUseDate(today: today))
    }

    var body: some View {
        Group {
            if case .recommendation(let rec) = result {
                content(rec)
            } else {
                VStack(alignment: .leading, spacing: 16) {
                    Text("아직 추천을 만들기 위한 운동 기록이 부족합니다.").font(.headline)
                    NavigationLink(value: Route.add) {
                        Text("운동 기록 추가").font(.headline).foregroundStyle(LoadcastColors.surface)
                            .frame(maxWidth: .infinity).frame(height: 52).background(LoadcastColors.accent)
                    }.buttonStyle(.plain)
                    Spacer()
                }
                .padding(18)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
            }
        }
        .background(LoadcastColors.bg)
        .navigationTitle("오늘 운동 추천")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func content(_ rec: Recommendation) -> some View {
        let palette = Palette.of(rec.state)
        return ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                StatusBadge(text: Strings.stateLabel(rec.state), palette: palette)
                Text(Strings.stateHeadline(rec.state)).font(.title.weight(.semibold)).foregroundStyle(LoadcastColors.text)
                Text(Strings.stateDescription(rec.state)).font(.subheadline).foregroundStyle(LoadcastColors.neutral800)

                if !rec.reasons.isEmpty {
                    SquareCard {
                        Text("추천 이유").font(.headline)
                        ForEach(rec.reasons, id: \.self) { code in
                            Text("· \(Strings.reasonLabel(code))").font(.subheadline).foregroundStyle(LoadcastColors.neutral800)
                        }
                    }
                }

                SquareCard {
                    Text("추천 운동").font(.headline)
                    ForEach(Strings.suggestedWorkouts(rec.state), id: \.0) { name, detail in
                        VStack(alignment: .leading, spacing: 2) {
                            Text(name).font(.subheadline.weight(.medium)).foregroundStyle(LoadcastColors.text)
                            Text(detail).font(.caption).foregroundStyle(LoadcastColors.neutral600)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }
                }

                if let caution = Strings.cautionText(rec.state) {
                    VStack(alignment: .leading, spacing: 3) {
                        Text("주의 운동").font(.subheadline.weight(.semibold))
                        Text(caution).font(.caption).foregroundStyle(LoadcastColors.neutral700)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(15)
                    .background(LoadcastColors.neutral100)
                }

                Text(Strings.disclaimer).font(.caption2).foregroundStyle(LoadcastColors.neutral500)

                NavigationLink(value: Route.add) {
                    Text("운동 기록 추가").font(.headline).foregroundStyle(LoadcastColors.surface)
                        .frame(maxWidth: .infinity).frame(height: 50).background(LoadcastColors.accent)
                }.buttonStyle(.plain)
                SecondaryButton(title: "홈으로 돌아가기") { dismiss() }
            }
            .padding(18)
        }
    }
}
