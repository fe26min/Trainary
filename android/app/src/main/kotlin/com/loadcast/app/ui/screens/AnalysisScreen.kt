package com.loadcast.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loadcast.app.ui.Strings
import com.loadcast.app.ui.components.SquareCard
import com.loadcast.app.ui.components.StatusBadge
import com.loadcast.app.ui.components.paletteOf
import com.loadcast.app.ui.theme.LoadcastColors
import com.loadcast.core.Acwr
import com.loadcast.core.Intensity
import com.loadcast.core.LoadCalculator
import com.loadcast.core.WeeklyAnalyzer
import com.loadcast.core.WorkoutRecord
import java.time.LocalDate

/**
 * 주간 분석 — 디자인 화면 08.
 * 4지표 요약 · 부하 상태(LoadStatus) · 요일별 막대(CSS 높이 방식, 차트 라이브러리 없음) · 이전 기간 비교.
 */
@Composable
fun AnalysisScreen(
    records: List<WorkoutRecord>,
    today: LocalDate,
    firstUseDate: LocalDate,
) {
    val summary = remember(records, today) { WeeklyAnalyzer.summarize(records, today) }
    val comparison = remember(records, today) { WeeklyAnalyzer.compareWithPrevious(records, today) }
    val loadStatus = remember(records, today, firstUseDate) {
        val acute = LoadCalculator.totalLoad(records, today.minusDays(6), today)
        val chronic = LoadCalculator.totalLoad(records, today.minusDays(27), today)
        val available = !firstUseDate.isAfter(today.minusDays(27)) && chronic > 0
        Acwr.status(if (available) Acwr.ratio(acute, chronic) else null)
    }
    // 요일별 최고 강도 막대 (월~일)
    val weekBars = remember(records, today) { buildWeekBars(records, today) }

    Column(
        Modifier.fillMaxSize().background(LoadcastColors.Bg).verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column {
            Text("최근 7일", style = MaterialTheme.typography.bodySmall, color = LoadcastColors.Neutral600)
            Text("주간 분석", style = MaterialTheme.typography.headlineSmall, color = LoadcastColors.Text)
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            SummaryTile("총 운동 시간", "${summary.totalMinutes}분", Modifier.weight(1f))
            SummaryTile("총 운동 부하", "${summary.totalLoad}점", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            SummaryTile("고강도 운동", "${summary.highCount}회", Modifier.weight(1f))
            SummaryTile("휴식일", "${summary.restDays}일", Modifier.weight(1f))
        }

        val palette = paletteOf(loadStatus)
        SquareCard(topAccent = palette.main) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge("부하 ${Strings.loadStatusLabel(loadStatus)}", palette)
            }
            Text(Strings.loadStatusDescription(loadStatus), style = MaterialTheme.typography.bodyMedium, color = LoadcastColors.Neutral800)
        }

        SquareCard {
            Text("요일별 운동 현황", style = MaterialTheme.typography.titleMedium)
            Row(
                Modifier.fillMaxWidth().height(96.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                weekBars.forEach { bar ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.height(96.dp)) {
                        val color = bar.intensity?.let { paletteOf(it).main } ?: LoadcastColors.Neutral300
                        Box(Modifier.width(22.dp).height(bar.heightDp.dp).background(color))
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                weekBars.forEach { bar ->
                    Text(bar.weekday, style = MaterialTheme.typography.labelSmall, color = LoadcastColors.Neutral600, textAlign = TextAlign.Center, modifier = Modifier.width(22.dp))
                }
            }
        }

        SquareCard {
            Text("이전 기간 비교", style = MaterialTheme.typography.titleMedium)
            if (comparison == null) {
                Text("비교할 이전 기록이 아직 없어요.", style = MaterialTheme.typography.bodyMedium, color = LoadcastColors.Neutral600)
            } else {
                ComparisonRow("운동 시간", signed(comparison.minutesDelta, "분"))
                ComparisonRow("운동 부하", signed(comparison.loadDelta, "점"))
                ComparisonRow("고강도 운동", signed(comparison.highCountDelta, "회"))
            }
        }
    }
}

@Composable
private fun SummaryTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.background(LoadcastColors.Surface).border(1.dp, LoadcastColors.Divider).padding(14.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = LoadcastColors.Neutral600)
        Text(value, style = MaterialTheme.typography.titleLarge, color = LoadcastColors.Text)
    }
}

@Composable
private fun ComparisonRow(label: String, delta: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = LoadcastColors.Neutral700)
        Text(delta, style = MaterialTheme.typography.titleSmall, color = LoadcastColors.Accent800)
    }
}

private fun signed(value: Int, unit: String): String =
    (if (value >= 0) "+" else "") + "$value$unit"

private data class WeekBar(val weekday: String, val intensity: Intensity?, val heightDp: Int)

/** 최근 7일(월~일 정렬) 각 날짜의 최고 강도로 막대 색·높이 결정. */
private fun buildWeekBars(records: List<WorkoutRecord>, today: LocalDate): List<WeekBar> {
    val start = today.minusDays(6)
    return (0..6).map { offset ->
        val date = start.plusDays(offset.toLong())
        val dayRecords = records.filter { it.date == date }
        val topIntensity = dayRecords.maxByOrNull { it.intensity.coefficient }?.intensity
        val height = when (topIntensity) {
            Intensity.HIGH -> 88
            Intensity.MODERATE -> 56
            Intensity.LOW -> 28
            null -> 8
        }
        WeekBar(Strings.weekdayShort(date), topIntensity, height)
    }
}
