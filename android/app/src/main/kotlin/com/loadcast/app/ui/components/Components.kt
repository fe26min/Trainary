package com.loadcast.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.loadcast.app.ui.Strings
import com.loadcast.app.ui.theme.LoadcastColors
import com.loadcast.core.Intensity
import com.loadcast.core.LoadStatus
import com.loadcast.core.RecommendationState

/** 준비도/강도 계열 색 묶음 — (본색, 100 틴트, 800 텍스트). */
data class StatusPalette(val main: Color, val tint: Color, val onTint: Color)

fun paletteOf(state: RecommendationState): StatusPalette = when (state) {
    RecommendationState.HIGH_OK ->
        StatusPalette(LoadcastColors.Ok, LoadcastColors.Ok100, LoadcastColors.Ok800)
    RecommendationState.MODERATE ->
        StatusPalette(LoadcastColors.Warn, LoadcastColors.Warn100, LoadcastColors.Warn800)
    RecommendationState.RECOVERY ->
        StatusPalette(LoadcastColors.Rest, LoadcastColors.Rest100, LoadcastColors.Rest800)
}

fun paletteOf(intensity: Intensity): StatusPalette = when (intensity) {
    Intensity.LOW ->
        StatusPalette(LoadcastColors.Ok, LoadcastColors.Ok100, LoadcastColors.Ok800)
    Intensity.MODERATE ->
        StatusPalette(LoadcastColors.Warn, LoadcastColors.Warn100, LoadcastColors.Warn800)
    Intensity.HIGH ->
        StatusPalette(LoadcastColors.Rest, LoadcastColors.Rest100, LoadcastColors.Rest800)
}

fun paletteOf(status: LoadStatus): StatusPalette = when (status) {
    LoadStatus.LOW, LoadStatus.OPTIMAL ->
        StatusPalette(LoadcastColors.Ok, LoadcastColors.Ok100, LoadcastColors.Ok800)
    LoadStatus.CAUTION ->
        StatusPalette(LoadcastColors.Warn, LoadcastColors.Warn100, LoadcastColors.Warn800)
    LoadStatus.OVERLOAD ->
        StatusPalette(LoadcastColors.Rest, LoadcastColors.Rest100, LoadcastColors.Rest800)
    LoadStatus.UNKNOWN ->
        StatusPalette(LoadcastColors.Neutral500, LoadcastColors.Neutral100, LoadcastColors.Neutral700)
}

/** 상태 배지 — 색만으로 구분하지 않도록 항상 텍스트 라벨 포함 (디자인 접근성 원칙). */
@Composable
fun StatusBadge(text: String, palette: StatusPalette, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = palette.onTint,
        modifier = modifier
            .background(palette.tint)
            .border(1.dp, palette.main.copy(alpha = 0.45f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
fun IntensityBadge(intensity: Intensity, modifier: Modifier = Modifier) {
    StatusBadge(
        text = "${Strings.intensityLabel(intensity)} 강도",
        palette = paletteOf(intensity),
        modifier = modifier,
    )
}

/** 각진 모서리 흰색 카드 (Industry 디자인 시스템 — radius 0). 선택적 상단 액센트 스트립. */
@Composable
fun SquareCard(
    modifier: Modifier = Modifier,
    topAccent: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LoadcastColors.Surface)
            .border(1.dp, LoadcastColors.Divider),
    ) {
        if (topAccent != null) {
            Box(Modifier.fillMaxWidth().height(4.dp).background(topAccent))
        }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = LoadcastColors.Text)
}
