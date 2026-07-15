package com.loadcast.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * 디자인은 라이트 모드 기준 (Industry 디자인 시스템).
 * 카드·버튼은 각진 모서리(radius 0), 모달만 약한 곡률 — Shape는 컴포넌트 레벨에서 처리.
 * TODO: Pretendard(한글) + Barlow Condensed(숫자·영문) 폰트 페어링 적용 (docs/SPEC.md 참고)
 */
private val LightColors = lightColorScheme(
    primary = LoadcastColors.Accent,
    onPrimary = LoadcastColors.Surface,
    primaryContainer = LoadcastColors.Accent100,
    onPrimaryContainer = LoadcastColors.Accent800,
    secondary = LoadcastColors.Neutral700,
    background = LoadcastColors.Bg,
    onBackground = LoadcastColors.Text,
    surface = LoadcastColors.Surface,
    onSurface = LoadcastColors.Text,
    surfaceVariant = LoadcastColors.Neutral100,
    onSurfaceVariant = LoadcastColors.Neutral700,
    outline = LoadcastColors.Neutral300,
    error = LoadcastColors.Rest,
)

@Composable
fun LoadcastTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
