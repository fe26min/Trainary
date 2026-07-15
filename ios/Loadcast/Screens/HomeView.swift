import SwiftUI
import LoadcastCore

/// 홈 대시보드 스텁 — 코어 로직 연동 확인용.
/// TODO: 디자인 화면 04(히어로 추천 카드 · 현재 상태 요약 · 부위별 피로 · 최근 7일 · 최근 운동) 구현.
/// TODO: 저장소(로컬 DB + Supabase) 연결 후 샘플 데이터 제거.
struct HomeView: View {
    var records: [WorkoutRecord] = []
    var today: DayDate = Self.currentDay()
    var firstUseDate: DayDate = Self.currentDay()

    private var result: RecommendationResult {
        Recommender.recommend(records: records, today: today, firstUseDate: firstUseDate)
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("오늘 운동 상태를\n확인해보세요.")
                    .font(.title2.weight(.semibold))
                    .foregroundStyle(LoadcastColors.text)

                switch result {
                case .insufficientData(let recordedCount, let requiredCount):
                    insufficientDataCard(recordedCount: recordedCount, requiredCount: requiredCount)
                case .recommendation(let recommendation):
                    recommendationCard(recommendation)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(18)
        }
        .background(LoadcastColors.bg)
    }

    private func insufficientDataCard(recordedCount: Int, requiredCount: Int) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("아직 추천을 만들기 위한\n운동 기록이 부족합니다.")
                .font(.headline)
                .foregroundStyle(LoadcastColors.text)
            Text("\(recordedCount) / \(requiredCount) 기록됨 — 최소 \(requiredCount)개의 운동을 기록하면 분석할 수 있어요.")
                .font(.caption)
                .foregroundStyle(LoadcastColors.neutral600)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(LoadcastColors.surface)
    }

    private func recommendationCard(_ recommendation: Recommendation) -> some View {
        let (label, tint, container): (String, Color, Color) = switch recommendation.state {
        case .highOk: ("고강도 가능", LoadcastColors.ok800, LoadcastColors.ok100)
        case .moderate: ("중강도 권장", LoadcastColors.warn800, LoadcastColors.warn100)
        case .recovery: ("회복 권장", LoadcastColors.rest800, LoadcastColors.rest100)
        }
        return VStack(alignment: .leading, spacing: 4) {
            Text("오늘 운동 추천")
                .font(.caption2)
                .foregroundStyle(tint)
            Text(label)
                .font(.title.weight(.semibold))
                .foregroundStyle(tint)
            if recommendation.estimate {
                Text("초기 추정 — 기록이 4주 쌓이면 더 정확해져요.")
                    .font(.caption)
                    .foregroundStyle(tint)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(container)
    }

    /// 기기 로컬 타임존 기준 오늘 → 코어의 순수 날짜로 변환.
    static func currentDay() -> DayDate {
        let parts = Calendar.current.dateComponents([.year, .month, .day], from: Date())
        return DayDate(year: parts.year!, month: parts.month!, day: parts.day!)
    }
}
