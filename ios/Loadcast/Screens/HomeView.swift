import SwiftUI
import LoadcastCore

/// 홈 대시보드 — 디자인 화면 04.
/// 히어로 추천 · 현재 상태 요약 · 부위별 피로(추정) · 최근 7일 · 최근 운동.
/// 기록 < 3건이면 히어로를 데이터 부족 카드로 대체(디자인 화면 19).
struct HomeView: View {
    @EnvironmentObject var store: RecordStore
    private let today = Today.value()

    private var result: RecommendationResult {
        Recommender.recommend(records: store.records, today: today, firstUseDate: store.firstUseDate(today: today))
    }
    private var summary: WeeklySummary {
        WeeklyAnalyzer.summarize(records: store.records, today: today)
    }
    private var fatigue: [BodyGroup: FatigueLevel] {
        BodyFatigue.estimate(records: store.records, today: today)
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(Strings.longDate(today)).font(.caption).foregroundStyle(LoadcastColors.neutral600)
                    Text("오늘 운동 상태를\n확인해보세요.").font(.title2.weight(.semibold)).foregroundStyle(LoadcastColors.text)
                }

                switch result {
                case .insufficientData(let recorded, let required):
                    insufficientHero(recorded: recorded, required: required)
                case .recommendation(let rec):
                    recommendationHero(rec)
                }

                if !store.records.isEmpty {
                    currentStatusCard
                    bodyFatigueCard
                    recentWeekCard
                }

                NavigationLink(value: Route.add) {
                    Label("운동 기록 추가", systemImage: "plus")
                        .font(.headline).foregroundStyle(LoadcastColors.surface)
                        .frame(maxWidth: .infinity).frame(height: 50).background(LoadcastColors.accent)
                }
                .buttonStyle(.plain)

                if !store.records.isEmpty { recentRecords }
            }
            .padding(18)
        }
        .background(LoadcastColors.bg)
        .loadcastDestinations()
    }

    private func recommendationHero(_ rec: Recommendation) -> some View {
        let palette = Palette.of(rec.state)
        return SquareCard(topAccent: palette.main) {
            HStack {
                Text("오늘 운동 추천 · TODAY").font(.caption2).foregroundStyle(LoadcastColors.accent)
                Spacer()
                StatusBadge(text: Strings.stateLabel(rec.state), palette: palette)
            }
            Text(Strings.stateHeadline(rec.state)).font(.title.weight(.semibold)).foregroundStyle(LoadcastColors.text)
            if rec.estimate {
                Text("초기 추정 — 기록이 4주 쌓이면 더 정확해져요.").font(.caption).foregroundStyle(palette.onTint)
            }
            Text(Strings.stateDescription(rec.state)).font(.subheadline).foregroundStyle(LoadcastColors.neutral800)
            NavigationLink(value: Route.recommendation) {
                linkRow("추천 상세 보기")
            }.buttonStyle(.plain)
        }
    }

    private func insufficientHero(recorded: Int, required: Int) -> some View {
        SquareCard(topAccent: LoadcastColors.accent) {
            Text("아직 추천을 만들기 위한\n운동 기록이 부족합니다.").font(.headline).foregroundStyle(LoadcastColors.text)
            Text("최소 \(required)개의 운동을 기록하면 최근 운동량을 분석할 수 있어요.").font(.caption).foregroundStyle(LoadcastColors.neutral600)
            Text("\(recorded) / \(required) 기록됨").font(.subheadline).foregroundStyle(LoadcastColors.accent)
        }
    }

    private var currentStatusCard: some View {
        SquareCard {
            Text("현재 상태 요약").font(.headline)
            HStack(spacing: 1) {
                metricCell("총 시간", "\(summary.totalMinutes)분")
                metricCell("총 부하", "\(summary.totalLoad)점")
            }
            HStack(spacing: 1) {
                metricCell("고강도", "\(summary.highCount)회")
                metricCell("휴식일", "\(summary.restDays)일")
            }
        }
    }

    private func metricCell(_ label: String, _ value: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label).font(.caption2).foregroundStyle(LoadcastColors.neutral600)
            Text(value).font(.title3.weight(.semibold)).foregroundStyle(LoadcastColors.text)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(12)
        .background(LoadcastColors.surface)
        .overlay(Rectangle().stroke(LoadcastColors.divider, lineWidth: 1))
    }

    private var bodyFatigueCard: some View {
        SquareCard {
            HStack {
                Text("부위별 피로").font(.headline)
                Spacer()
                Text("추정").font(.caption2).foregroundStyle(LoadcastColors.neutral600)
                    .padding(.horizontal, 9).padding(.vertical, 3).background(LoadcastColors.neutral100)
            }
            ForEach(fatigueRows, id: \.0) { label, level in
                let palette = Palette.of(level)
                HStack(spacing: 10) {
                    Text(label).font(.caption).foregroundStyle(LoadcastColors.neutral700).frame(width: 38, alignment: .leading)
                    GeometryReader { geo in
                        ZStack(alignment: .leading) {
                            Rectangle().fill(LoadcastColors.neutral200)
                            Rectangle().fill(palette.main).frame(width: geo.size.width * fatigueFraction(level))
                        }
                    }
                    .frame(height: 8)
                    Text(Strings.fatigueLabel(level)).font(.caption.weight(.semibold)).foregroundStyle(palette.onTint).frame(width: 32, alignment: .trailing)
                }
            }
            Text("최근 운동 종류를 바탕으로 추정한 값이에요. 정밀한 근육별 측정은 추후 제공됩니다.")
                .font(.caption2).foregroundStyle(LoadcastColors.neutral500)
        }
    }

    private var fatigueRows: [(String, FatigueLevel)] {
        [("하체", fatigue[.lower] ?? .low),
         ("상체", fatigue[.upper] ?? .low),
         ("코어", fatigue[.core] ?? .low),
         ("심폐", fatigue[.cardio] ?? .low)]
    }

    private func fatigueFraction(_ level: FatigueLevel) -> CGFloat {
        switch level {
        case .low: 0.32
        case .moderate: 0.6
        case .high: 0.85
        }
    }

    private var recentWeekCard: some View {
        SquareCard {
            Text("최근 7일 운동").font(.headline)
            Text("총 \(summary.totalMinutes)분 · \(summary.totalLoad)점 · 고강도 \(summary.highCount)회")
                .font(.subheadline).foregroundStyle(LoadcastColors.neutral800)
            Text("자세한 추이는 분석 탭에서 확인하세요.").font(.caption).foregroundStyle(LoadcastColors.neutral500)
        }
    }

    private var recentRecords: some View {
        VStack(alignment: .leading, spacing: 9) {
            Text("최근 운동").font(.headline)
            ForEach(Array(store.records.prefix(2))) { record in
                NavigationLink(value: Route.edit(record.id)) {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(Strings.recordTypeLabel(record)).font(.subheadline.weight(.medium)).foregroundStyle(LoadcastColors.text)
                            Text("\(Strings.shortDate(record.date)) · \(record.durationMin)분 · \(Strings.intensityLabel(record.intensity))")
                                .font(.caption).foregroundStyle(LoadcastColors.neutral600)
                        }
                        Spacer()
                        Image(systemName: "chevron.right").foregroundStyle(LoadcastColors.neutral400)
                    }
                    .padding(14)
                    .background(LoadcastColors.surface)
                    .overlay(Rectangle().stroke(LoadcastColors.divider, lineWidth: 1))
                }
                .buttonStyle(.plain)
            }
        }
    }

    private func linkRow(_ text: String) -> some View {
        HStack {
            Spacer()
            Text(text).font(.subheadline.weight(.medium)).foregroundStyle(LoadcastColors.accent)
            Image(systemName: "arrow.right").font(.caption).foregroundStyle(LoadcastColors.accent)
            Spacer()
        }
    }
}
