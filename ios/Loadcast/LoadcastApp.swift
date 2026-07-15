import SwiftUI

@main
struct LoadcastApp: App {
    var body: some Scene {
        WindowGroup {
            RootTabView()
        }
    }
}

/// 하단 내비게이션 4탭 — 홈 · 기록 · 분석 · 설정 (디자인 문서 섹션 03).
struct RootTabView: View {
    var body: some View {
        TabView {
            HomeView()
                .tabItem { Label("홈", systemImage: "house") }
            RecordsView()
                .tabItem { Label("기록", systemImage: "list.bullet") }
            AnalysisView()
                .tabItem { Label("분석", systemImage: "chart.line.uptrend.xyaxis") }
            SettingsView()
                .tabItem { Label("설정", systemImage: "gearshape") }
        }
        .tint(LoadcastColors.accent)
    }
}
