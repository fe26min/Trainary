import SwiftUI

/// 주간 분석 스텁.
/// TODO: 디자인 화면 08 — 4지표 요약 · 부하 상태 카드(LoadStatus) · 요일별 막대(라이브러리 없이 사각형 높이) · 이전 기간 비교.
struct AnalysisView: View {
    var body: some View {
        ZStack {
            LoadcastColors.bg.ignoresSafeArea()
            Text("분석 — 구현 예정")
                .foregroundStyle(LoadcastColors.neutral600)
        }
    }
}
