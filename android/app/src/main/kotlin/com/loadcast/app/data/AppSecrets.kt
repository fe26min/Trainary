package com.loadcast.app.data

import com.loadcast.app.BuildConfig

/**
 * keys.properties(→ BuildConfig)에서 주입된 키를 읽는 단일 지점.
 * 값이 비어 있으면 해당 기능(소셜 로그인 등)을 비활성화하는 식으로 방어적으로 사용한다.
 * 실제 키 관리 방식은 docs/SECRETS.md 참고.
 */
object AppSecrets {
    val supabaseUrl: String get() = BuildConfig.SUPABASE_URL
    val supabaseAnonKey: String get() = BuildConfig.SUPABASE_ANON_KEY
    val kakaoNativeAppKey: String get() = BuildConfig.KAKAO_NATIVE_APP_KEY
    val googleWebClientId: String get() = BuildConfig.GOOGLE_WEB_CLIENT_ID
    val naverClientId: String get() = BuildConfig.NAVER_CLIENT_ID

    /** 소셜 로그인 버튼 노출 여부 판단용 — 키가 있으면 해당 프로바이더 활성. */
    val isSupabaseConfigured: Boolean
        get() = supabaseUrl.isNotBlank() && supabaseAnonKey.isNotBlank()
}
