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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loadcast.app.ui.Strings
import com.loadcast.app.ui.components.ChipRow
import com.loadcast.app.ui.components.IntensityBadge
import com.loadcast.app.ui.components.PrimaryButton
import com.loadcast.app.ui.theme.LoadcastColors
import com.loadcast.core.WorkoutRecord
import com.loadcast.core.WorkoutType

/**
 * 운동 기록 목록 — 디자인 화면 05.
 * 필터 칩(클라이언트 필터) · 카드 전체가 수정 진입 영역(카드 내 액션 버튼 없음) · 빈 상태.
 */
@Composable
fun RecordsScreen(
    records: List<WorkoutRecord>,
    onAdd: () -> Unit,
    onOpenRecord: (String) -> Unit,
) {
    // 전체 + 실제 등장하는 종류만 필터로 노출 (커스텀 종류 포함은 향후 확장)
    val filterTypes = remember(records) {
        listOf<WorkoutType?>(null) + WorkoutType.entries.filter { t -> records.any { it.type == t } }
    }
    var selectedFilter by remember { mutableIntStateOf(0) }
    if (selectedFilter >= filterTypes.size) selectedFilter = 0

    val filterType = filterTypes.getOrNull(selectedFilter)
    val visible = remember(records, filterType) {
        if (filterType == null) records else records.filter { it.type == filterType }
    }

    Column(Modifier.fillMaxSize().background(LoadcastColors.Bg)) {
        Row(
            Modifier.fillMaxWidth().padding(start = 18.dp, end = 14.dp, top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("운동 기록", style = MaterialTheme.typography.headlineSmall, color = LoadcastColors.Text)
            Box(
                Modifier.size(38.dp).background(LoadcastColors.Accent).clickable(onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.Add, "기록 추가", tint = LoadcastColors.Surface) }
        }

        if (records.isEmpty()) {
            EmptyRecords(onAdd)
            return
        }

        ChipRow(
            labels = filterTypes.map { it?.let(Strings::typeLabel) ?: "전체" },
            selectedIndex = selectedFilter,
            onSelect = { selectedFilter = it },
        )

        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp),
        ) {
            items(visible, key = { it.id }) { record ->
                RecordCard(record) { onOpenRecord(record.id) }
            }
        }
    }
}

@Composable
private fun RecordCard(record: WorkoutRecord, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(LoadcastColors.Surface).border(1.dp, LoadcastColors.Divider)
            .clickable(onClick = onClick).padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(Strings.recordTypeLabel(record), style = MaterialTheme.typography.titleMedium, color = LoadcastColors.Text)
            IntensityBadge(record.intensity)
        }
        Text(
            "${Strings.shortDate(record.date)} · ${record.durationMin}분 · 부하 ${record.load}점",
            style = MaterialTheme.typography.bodySmall,
            color = LoadcastColors.Neutral600,
        )
        record.memo?.takeIf { it.isNotBlank() }?.let {
            Text("“$it”", style = MaterialTheme.typography.bodySmall, color = LoadcastColors.Neutral500, maxLines = 1)
        }
    }
}

@Composable
private fun EmptyRecords(onAdd: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(34.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(64.dp).background(LoadcastColors.Neutral100),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Filled.Inbox, null, tint = LoadcastColors.Neutral500, modifier = Modifier.size(30.dp)) }
        Text("아직 운동 기록이 없습니다.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "첫 운동을 기록하고 오늘의 추천을 받아보세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = LoadcastColors.Neutral600,
        )
        PrimaryButton(text = "운동 기록 추가", modifier = Modifier.padding(top = 4.dp), onClick = onAdd)
    }
}
