import Foundation

/// Info.plist(← Config.xcconfig ← Secrets.xcconfig)에서 주입된 키를 읽는 단일 지점.
/// 값이 비어 있으면 해당 기능(소셜 로그인 등)을 비활성화하는 식으로 방어적으로 사용한다.
/// 실제 키 관리 방식은 docs/SECRETS.md 참고.
enum AppSecrets {
    private static func string(_ key: String) -> String {
        (Bundle.main.object(forInfoDictionaryKey: key) as? String)?
            .trimmingCharacters(in: .whitespaces) ?? ""
    }

    /// Supabase는 호스트만 저장(xcconfig의 "//" 주석 오인 회피)하고 https:// 를 코드에서 붙인다.
    static var supabaseURL: String {
        let host = string("SupabaseHost")
        return host.isEmpty ? "" : "https://\(host)"
    }
    static var supabaseAnonKey: String { string("SupabaseAnonKey") }
    static var kakaoNativeAppKey: String { string("KakaoNativeAppKey") }
    static var naverClientId: String { string("NaverClientId") }

    /// 소셜 로그인 버튼 노출 여부 판단용 — 키가 있으면 해당 프로바이더 활성.
    static var isSupabaseConfigured: Bool { !supabaseURL.isEmpty && !supabaseAnonKey.isEmpty }
}
