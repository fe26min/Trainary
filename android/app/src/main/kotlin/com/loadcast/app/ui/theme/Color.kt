package com.loadcast.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 디자인 토큰 — 클로드 디자인 "운동 부하 분석 앱 설계" 컬러 섹션.
 * 값은 이 파일에서만 정의하고 화면 코드에 하드코딩하지 않는다.
 */
object LoadcastColors {
    // 기본
    val Bg = Color(0xFFE8EBF1)
    val Surface = Color(0xFFFFFFFF)
    val Text = Color(0xFF141822)
    val Divider = Color(0x1A141822) // rgba(20,24,34,.10)

    // 액센트(스틸 블루) 램프
    val Accent = Color(0xFF2F62EA)
    val Accent100 = Color(0xFFEEF2FE)
    val Accent200 = Color(0xFFDBE4FD)
    val Accent300 = Color(0xFFBCCBFA)
    val Accent700 = Color(0xFF2F4FC2)
    val Accent800 = Color(0xFF26408F)
    val Accent900 = Color(0xFF1E3160)

    // 준비도 3색 — 고강도 가능 / 중강도 권장 / 회복 권장
    val Ok = Color(0xFF12A35D)
    val Ok100 = Color(0xFFE7F6EE)
    val Ok300 = Color(0xFFA6DDBF)
    val Ok700 = Color(0xFF0C7D47)
    val Ok800 = Color(0xFF0A5F37)

    val Warn = Color(0xFFE2891B)
    val Warn100 = Color(0xFFFDF2DF)
    val Warn300 = Color(0xFFF3CE8D)
    val Warn700 = Color(0xFFA5620F)
    val Warn800 = Color(0xFF7C4A0C)

    val Rest = Color(0xFFE0574A)
    val Rest100 = Color(0xFFFDECEB)
    val Rest300 = Color(0xFFF2B2AB)
    val Rest700 = Color(0xFFB23A2E)
    val Rest800 = Color(0xFF8A2B21)

    // 뉴트럴 램프
    val Neutral100 = Color(0xFFF5F5F8)
    val Neutral200 = Color(0xFFE7E7EA)
    val Neutral300 = Color(0xFFD4D4D7)
    val Neutral400 = Color(0xFFB7B7BA)
    val Neutral500 = Color(0xFF98989B)
    val Neutral600 = Color(0xFF7A7A7D)
    val Neutral700 = Color(0xFF5D5D60)
    val Neutral800 = Color(0xFF424244)
    val Neutral900 = Color(0xFF2B2B2D)
}
