import SwiftUI
import LoadcastCore

@main
struct LoadcastApp: App {
    @StateObject private var store = RecordStore()

    var body: some Scene {
        WindowGroup {
            RootTabView()
                .environmentObject(store)
        }
    }
}

/// 앱 공통 오늘 날짜 — 기기 로컬 타임존 기준 → 코어의 순수 날짜.
enum Today {
    static func value() -> DayDate {
        let parts = Calendar.current.dateComponents([.year, .month, .day], from: Date())
        return DayDate(year: parts.year!, month: parts.month!, day: parts.day!)
    }
}

/// 스택 내 이동 경로.
enum Route: Hashable {
    case add
    case edit(String)
    case recommendation
}

/// 하단 내비게이션 4탭 — 홈 · 기록 · 분석 · 설정 (디자인 문서 섹션 03).
struct RootTabView: View {
    var body: some View {
        TabView {
            NavigationStack { HomeView() }
                .tabItem { Label("홈", systemImage: "house") }
            NavigationStack { RecordsView() }
                .tabItem { Label("기록", systemImage: "list.bullet") }
            NavigationStack { AnalysisView() }
                .tabItem { Label("분석", systemImage: "chart.line.uptrend.xyaxis") }
            NavigationStack { SettingsView() }
                .tabItem { Label("설정", systemImage: "gearshape") }
        }
        .tint(LoadcastColors.accent)
    }
}

/// 스택 목적지 공통 처리 — 각 탭의 NavigationStack에서 재사용.
extension View {
    func loadcastDestinations() -> some View {
        navigationDestination(for: Route.self) { route in
            switch route {
            case .add:
                RecordFormView(existingID: nil)
            case .edit(let id):
                RecordFormView(existingID: id)
            case .recommendation:
                RecommendationView()
            }
        }
    }
}
