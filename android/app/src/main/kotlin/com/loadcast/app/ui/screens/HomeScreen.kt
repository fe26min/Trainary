package com.loadcast.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loadcast.app.ui.Strings
import com.loadcast.app.ui.components.SquareCard
import com.loadcast.app.ui.components.StatusBadge
import com.loadcast.app.ui.components.paletteOf
import com.loadcast.app.ui.theme.LoadcastColors
import com.loadcast.core.BodyFatigue
import com.loadcast.core.BodyGroup
import com.loadcast.core.RecommendationResult
import com.loadcast.core.Recommender
import com.loadcast.core.WeeklyAnalyzer
import com.loadcast.core.WorkoutRecord
import java.time.LocalDate

/**
 * 홈 대시보드 — 디자인 화면 04.
 * 히어로 추천 · 현재 상태 요약 · 부위별 피로(추정) · 최근 7일 · 최근 운동.
 * 기록 < 3건이면 히어로를 데이터 부족 카드로 대체(디자인 화면 19).
 */
@Composable
fun HomeScreen(
    records: List<WorkoutRecord>,
    today: LocalDate,
    firstUseDate: LocalDate,
    onOpenRecommendation: () -> Unit,
    onOpenAnalysis: () -> Unit,
    onAdd: () -> Unit,
    onOpenRecord: (String) -> Unit,
) {
    val result = remember(records, today, firstUseDate) { Recommender.recommend(records, today, firstUseDate) }
    val summary = remember(records, today) { WeeklyAnalyzer.summarize(records, today) }
    val fatigue = remember(records, today) { BodyFatigue.estimate(records, today) }

    Column(
        Modifier.fillMaxSize().background(LoadcastColors.Bg).verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column {
            Text(Strings.longDate(today), style = MaterialTheme.typography.bodySmall, color = LoadcastColors.Neutral600)
            Text("오늘 운동 상태를\n확인해보세요.", style = MaterialTheme.typography.headlineSmall, color = LoadcastColors.Text)
        }

        when (result) {
            is RecommendationResult.InsufficientData -> InsufficientHero(result.recordedCount, result.requiredCount, onAdd)
            is RecommendationResult.Recommendation -> RecommendationHero(result, onOpenRecommendation)
        }

        if (records.isNotEmpty()) {
            CurrentStatusCard(summary)
            BodyFatigueCard(fatigue)
            RecentWeekCard(summary, onOpenAnalysis)
        }

        PrimaryAddButton(onAdd)

        if (records.isNotEmpty()) {
            RecentRecords(records.take(2), onOpenRecord)
        }
    }
}

@Composable
private fun RecommendationHero(rec: RecommendationResult.Recommendation, onOpen: () -> Unit) {
    val palette = paletteOf(rec.state)
    SquareCard(topAccent = palette.main) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("오늘 운동 추천 · TODAY", style = MaterialTheme.typography.labelSmall, color = LoadcastColors.Accent)
            StatusBadge(Strings.stateLabel(rec.state), palette)
        }
        Text(Strings.stateHeadline(rec.state), style = MaterialTheme.typography.headlineMedium, color = LoadcastColors.Text)
        if (rec.estimate) {
            Text("초기 추정 — 기록이 4주 쌓이면 더 정확해져요.", style = MaterialTheme.typography.bodySmall, color = palette.onTint)
        }
        Text(Strings.stateDescription(rec.state), style = MaterialTheme.typography.bodyMedium, color = LoadcastColors.Neutral800)
        LinkRow(text = "추천 상세 보기", onClick = onOpen)
    }
}

@Composable
private fun InsufficientHero(recorded: Int, required: Int, onAdd: () -> Unit) {
    SquareCard(topAccent = LoadcastColors.Accent) {
        Text("아직 추천을 만들기 위한\n운동 기록이 부족합니다.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("최소 ${required}개의 운동을 기록하면 최근 운동량을 분석할 수 있어요.", style = MaterialTheme.typography.bodySmall, color = LoadcastColors.Neutral600)
        Text("$recorded / $required 기록됨", style = MaterialTheme.typography.labelMedium, color = LoadcastColors.Accent)
    }
}

@Composable
private fun CurrentStatusCard(summary: com.loadcast.core.WeeklySummary) {
    SquareCard {
        Text("현재 상태 요약", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            MetricCell("총 시간", "${summary.totalMinutes}분", Modifier.weight(1f))
            MetricCell("총 부하", "${summary.totalLoad}점", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            MetricCell("고강도", "${summary.highCount}회", Modifier.weight(1f))
            MetricCell("휴식일", "${summary.restDays}일", Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.background(LoadcastColors.Surface).border(1.dp, LoadcastColors.Divider).padding(12.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = LoadcastColors.Neutral600)
        Text(value, style = MaterialTheme.typography.titleMedium, color = LoadcastColors.Text)
    }
}

@Composable
private fun BodyFatigueCard(fatigue: Map<BodyGroup, com.loadcast.core.FatigueLevel>) {
    SquareCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("부위별 피로", style = MaterialTheme.typography.titleMedium)
            Box(Modifier.background(LoadcastColors.Neutral100).padding(horizontal = 9.dp, vertical = 3.dp)) {
                Text("추정", style = MaterialTheme.typography.labelSmall, color = LoadcastColors.Neutral600)
            }
        }
        listOf(
            BodyGroup.LOWER to "하체", BodyGroup.UPPER to "상체",
            BodyGroup.CORE to "코어", BodyGroup.CARDIO to "심폐",
        ).forEach { (group, label) ->
            val level = fatigue[group] ?: com.loadcast.core.FatigueLevel.LOW
            val palette = when (level) {
                com.loadcast.core.FatigueLevel.LOW -> paletteOf(com.loadcast.core.Intensity.LOW)
                com.loadcast.core.FatigueLevel.MODERATE -> paletteOf(com.loadcast.core.Intensity.MODERATE)
                com.loadcast.core.FatigueLevel.HIGH -> paletteOf(com.loadcast.core.Intensity.HIGH)
            }
            val fraction = when (level) {
                com.loadcast.core.FatigueLevel.LOW -> 0.32f
                com.loadcast.core.FatigueLevel.MODERATE -> 0.6f
                com.loadcast.core.FatigueLevel.HIGH -> 0.85f
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = LoadcastColors.Neutral700, modifier = Modifier.width(38.dp))
                Box(Modifier.weight(1f).height(8.dp).background(LoadcastColors.Neutral200)) {
                    Box(Modifier.fillMaxWidth(fraction).height(8.dp).background(palette.main))
                }
                Text(Strings.fatigueLabel(level), style = MaterialTheme.typography.labelMedium, color = palette.onTint, modifier = Modifier.width(32.dp))
            }
        }
        Text(
            "최근 운동 종류를 바탕으로 추정한 값이에요. 정밀한 근육별 측정은 추후 제공됩니다.",
            style = MaterialTheme.typography.labelSmall,
            color = LoadcastColors.Neutral500,
        )
    }
}

@Composable
private fun RecentWeekCard(summary: com.loadcast.core.WeeklySummary, onOpenAnalysis: () -> Unit) {
    SquareCard {
        Text("최근 7일 운동", style = MaterialTheme.typography.titleMedium)
        Text("총 ${summary.totalMinutes}분 · ${summary.totalLoad}점 · 고강도 ${summary.highCount}회", style = MaterialTheme.typography.bodyMedium, color = LoadcastColors.Neutral800)
        LinkRow(text = "분석 자세히 보기", onClick = onOpenAnalysis)
    }
}

@Composable
private fun PrimaryAddButton(onAdd: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(50.dp).background(LoadcastColors.Accent).clickable(onClick = onAdd),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Add, null, tint = LoadcastColors.Surface)
            Text("  운동 기록 추가", color = LoadcastColors.Surface, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RecentRecords(records: List<WorkoutRecord>, onOpenRecord: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("최근 운동", style = MaterialTheme.typography.titleMedium)
        records.forEach { record ->
            Row(
                Modifier.fillMaxWidth().background(LoadcastColors.Surface).border(1.dp, LoadcastColors.Divider)
                    .clickable { onOpenRecord(record.id) }.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(Strings.recordTypeLabel(record), style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${Strings.shortDate(record.date)} · ${record.durationMin}분 · ${Strings.intensityLabel(record.intensity)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LoadcastColors.Neutral600,
                    )
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = LoadcastColors.Neutral400)
            }
        }
    }
}

@Composable
private fun LinkRow(text: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(top = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = LoadcastColors.Accent)
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = LoadcastColors.Accent, modifier = Modifier.size(16.dp))
    }
}
