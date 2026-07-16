import SwiftUI
import LoadcastCore

/// 설정 — 디자인 화면 10.
/// MVP: 프로필/앱 정보 요약. 계정(로그아웃·탈퇴)은 Supabase 인증 연동 시 활성화 (docs/AUTH.md).
struct SettingsView: View {
    @EnvironmentObject var store: RecordStore

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("설정").font(.title2.weight(.semibold)).foregroundStyle(LoadcastColors.text)

                SquareCard {
                    Text("프로필").font(.headline)
                    Text("로그인 연동 후 닉네임·운동 목적·종목이 표시됩니다.")
                        .font(.caption).foregroundStyle(LoadcastColors.neutral600)
                }

                SquareCard {
                    Text("데이터").font(.headline)
                    Text("저장된 운동 기록 · \(store.records.count)건")
                        .font(.subheadline).foregroundStyle(LoadcastColors.neutral800)
                    Text("현재 기록은 기기에 로컬 저장됩니다. 계정 연동 시 클라우드 동기화 예정.")
                        .font(.caption2).foregroundStyle(LoadcastColors.neutral500)
                }

                SquareCard {
                    Text("앱 정보").font(.headline)
                    Text("LOADCAST · v0.1.0").font(.subheadline).foregroundStyle(LoadcastColors.neutral800)
                }
            }
            .padding(18)
        }
        .background(LoadcastColors.bg)
    }
}
