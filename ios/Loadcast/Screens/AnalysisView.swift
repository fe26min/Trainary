import SwiftUI
import LoadcastCore

/// 주간 분석 — 디자인 화면 08.
/// 4지표 요약 · 부하 상태(LoadStatus) · 요일별 막대(사각형 높이, 차트 라이브러리 없음) · 이전 기간 비교.
struct AnalysisView: View {
    @EnvironmentObject var store: RecordStore
    private let today = Today.value()

    private var summary: WeeklySummary { WeeklyAnalyzer.summarize(records: store.records, today: today) }
    private var comparison: WeekComparison? { WeeklyAnalyzer.compareWithPrevious(records: store.records, today: today) }

    private var loadStatus: LoadStatus {
        let acute = LoadCalculator.totalLoad(records: store.records, from: today.adding(days: -6), to: today)
        let chronic = LoadCalculator.totalLoad(records: store.records, from: today.adding(days: -27), to: today)
        let available = store.firstUseDate(today: today) <= today.adding(days: -27) && chronic > 0
        return Acwr.status(available ? Acwr.ratio(acuteLoad: acute, chronicLoad: chronic) : nil)
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                VStack(alignment: .leading, spacing: 2) {
                    Text("최근 7일").font(.caption).foregroundStyle(LoadcastColors.neutral600)
                    Text("주간 분석").font(.title2.weight(.semibold)).foregroundStyle(LoadcastColors.text)
                }

                HStack(spacing: 1) {
                    summaryTile("총 운동 시간", "\(summary.totalMinutes)분")
                    summaryTile("총 운동 부하", "\(summary.totalLoad)점")
                }
                HStack(spacing: 1) {
                    summaryTile("고강도 운동", "\(summary.highCount)회")
                    summaryTile("휴식일", "\(summary.restDays)일")
                }

                let palette = Palette.of(loadStatus)
                SquareCard(topAccent: palette.main) {
                    StatusBadge(text: "부하 \(Strings.loadStatusLabel(loadStatus))", palette: palette)
                    Text(Strings.loadStatusDescription(loadStatus)).font(.subheadline).foregroundStyle(LoadcastColors.neutral800)
                }

                weekBarsCard

                SquareCard {
                    Text("이전 기간 비교").font(.headline)
                    if let comparison {
                        comparisonRow("운동 시간", signed(comparison.minutesDelta, "분"))
                        comparisonRow("운동 부하", signed(comparison.loadDelta, "점"))
                        comparisonRow("고강도 운동", signed(comparison.highCountDelta, "회"))
                    } else {
                        Text("비교할 이전 기록이 아직 없어요.").font(.subheadline).foregroundStyle(LoadcastColors.neutral600)
                    }
                }
            }
            .padding(18)
        }
        .background(LoadcastColors.bg)
    }

    private func summaryTile(_ label: String, _ value: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label).font(.caption2).foregroundStyle(LoadcastColors.neutral600)
            Text(value).font(.title2.weight(.semibold)).foregroundStyle(LoadcastColors.text)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(LoadcastColors.surface)
        .overlay(Rectangle().stroke(LoadcastColors.divider, lineWidth: 1))
    }

    private var weekBarsCard: some View {
        SquareCard {
            Text("요일별 운동 현황").font(.headline)
            HStack(alignment: .bottom) {
                ForEach(weekBars, id: \.offset) { bar in
                    VStack {
                        Spacer(minLength: 0)
                        Rectangle().fill(bar.color).frame(width: 22, height: bar.height)
                    }
                    .frame(maxWidth: .infinity, minHeight: 96, maxHeight: 96)
                }
            }
            HStack {
                ForEach(weekBars, id: \.offset) { bar in
                    Text(bar.weekday).font(.caption2).foregroundStyle(LoadcastColors.neutral600)
                        .frame(maxWidth: .infinity)
                }
            }
        }
    }

    private struct WeekBar { let offset: Int; let weekday: String; let color: Color; let height: CGFloat }

    private var weekBars: [WeekBar] {
        let start = today.adding(days: -6)
        return (0...6).map { offset in
            let date = start.adding(days: offset)
            let dayRecords = store.records.filter { $0.date == date }
            let top = dayRecords.max { $0.intensity.coefficient < $1.intensity.coefficient }?.intensity
            let height: CGFloat
            switch top {
            case .high: height = 88
            case .moderate: height = 56
            case .low: height = 28
            case nil: height = 8
            }
            let color = top.map { Palette.of($0).main } ?? LoadcastColors.neutral300
            return WeekBar(offset: offset, weekday: Strings.weekdayShort(date), color: color, height: height)
        }
    }

    private func comparisonRow(_ label: String, _ delta: String) -> some View {
        HStack {
            Text(label).font(.subheadline).foregroundStyle(LoadcastColors.neutral700)
            Spacer()
            Text(delta).font(.subheadline.weight(.semibold)).foregroundStyle(LoadcastColors.accent800)
        }
        .padding(.vertical, 4)
    }

    private func signed(_ value: Int, _ unit: String) -> String {
        (value >= 0 ? "+" : "") + "\(value)\(unit)"
    }
}
