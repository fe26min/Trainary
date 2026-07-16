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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loadcast.app.ui.Strings
import com.loadcast.app.ui.components.ChipFlow
import com.loadcast.app.ui.components.DeleteConfirmDialog
import com.loadcast.app.ui.components.FormTopBar
import com.loadcast.app.ui.components.PrimaryButton
import com.loadcast.app.ui.components.paletteOf
import com.loadcast.app.ui.theme.LoadcastColors
import com.loadcast.core.Intensity
import com.loadcast.core.LoadCalculator
import com.loadcast.core.WorkoutRecord
import com.loadcast.core.WorkoutType
import java.time.LocalDate

/**
 * 운동 기록 추가/수정 폼 — 디자인 화면 06·07.
 * 필수 4개(날짜·종류·시간·강도)만 검증. 부하 = 시간 × 강도계수 자동 계산.
 * 삭제는 하단 약한 텍스트 버튼 → 확인 모달(디자인 화면 14).
 */
@Composable
fun RecordFormScreen(
    existing: WorkoutRecord?,
    today: LocalDate,
    onSave: (WorkoutRecord) -> Unit,
    onDelete: (String) -> Unit,
    onBack: () -> Unit,
) {
    val isEdit = existing != null
    var type by remember { mutableStateOf(existing?.type ?: WorkoutType.CROSSFIT) }
    var customLabel by remember { mutableStateOf(existing?.customTypeLabel ?: "") }
    var duration by remember { mutableStateOf(existing?.durationMin ?: 60) }
    var intensity by remember { mutableStateOf(existing?.intensity ?: Intensity.MODERATE) }
    var satisfaction by remember { mutableStateOf(existing?.satisfaction) }
    var memo by remember { mutableStateOf(existing?.memo ?: "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val date = existing?.date ?: today
    val estimatedLoad = LoadCalculator.sessionLoad(duration, intensity)

    Column(Modifier.fillMaxSize().background(LoadcastColors.Bg)) {
        FormTopBar(
            title = if (isEdit) "운동 기록 수정" else "운동 기록 추가",
            onBack = onBack,
        )
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            FieldLabel("운동 날짜")
            ReadonlyField(Strings.formDate(date))

            FieldLabel("운동 종류")
            TypeSelector(selected = type, onSelect = { type = it })
            if (type == WorkoutType.OTHER) {
                OutlinedTextField(
                    value = customLabel,
                    onValueChange = { customLabel = it },
                    placeholder = { Text("예: 클라이밍, 복싱") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            FieldLabel("운동 시간")
            DurationStepper(duration = duration, onChange = { duration = it })

            FieldLabel("운동 강도")
            IntensitySelector(selected = intensity, onSelect = { intensity = it })

            EstimatedLoadRow(estimatedLoad)

            FieldLabel("운동 만족도 (선택)")
            SatisfactionSelector(value = satisfaction, onSelect = { satisfaction = it })

            FieldLabel("메모 (선택)")
            OutlinedTextField(
                value = memo,
                onValueChange = { memo = it },
                placeholder = { Text("운동 내용이나 오늘 컨디션을 간단히 기록해보세요.") },
                modifier = Modifier.fillMaxWidth().height(96.dp),
            )

            PrimaryButton(
                text = if (isEdit) "변경 내용 저장" else "운동 기록 저장",
            ) {
                onSave(
                    (existing ?: WorkoutRecord(date = date, type = type, durationMin = duration, intensity = intensity))
                        .copy(
                            type = type,
                            durationMin = duration,
                            intensity = intensity,
                            customTypeLabel = customLabel.takeIf { type == WorkoutType.OTHER && it.isNotBlank() },
                            satisfaction = satisfaction,
                            memo = memo.takeIf { it.isNotBlank() },
                        ),
                )
            }

            if (isEdit) {
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("  운동 기록 삭제", color = LoadcastColors.Neutral600)
                }
            }
        }
    }

    if (showDeleteConfirm && existing != null) {
        DeleteConfirmDialog(
            onCancel = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete(existing.id)
            },
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, style = androidx.compose.material3.MaterialTheme.typography.labelLarge, color = LoadcastColors.Neutral700)
}

@Composable
private fun ReadonlyField(text: String) {
    Box(
        Modifier.fillMaxWidth().height(48.dp).background(LoadcastColors.Surface)
            .border(1.dp, LoadcastColors.Divider).padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart,
    ) { Text(text) }
}

@Composable
private fun TypeSelector(selected: WorkoutType, onSelect: (WorkoutType) -> Unit) {
    ChipFlow(
        items = WorkoutType.entries,
        isSelected = { it == selected },
        label = { Strings.typeLabel(it) },
        onClick = onSelect,
    )
}

@Composable
private fun IntensitySelector(selected: Intensity, onSelect: (Intensity) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Intensity.entries.forEach { level ->
            val isSel = level == selected
            val palette = paletteOf(level)
            Column(
                Modifier.weight(1f).height(80.dp)
                    .background(if (isSel) palette.tint else LoadcastColors.Surface)
                    .border(
                        if (isSel) 2.dp else 1.dp,
                        if (isSel) palette.main else LoadcastColors.Divider,
                    )
                    .clickable { onSelect(level) }
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        Strings.intensityLabel(level),
                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                        color = if (isSel) palette.onTint else LoadcastColors.Neutral700,
                    )
                    if (isSel) Icon(Icons.Filled.Check, null, tint = palette.onTint, modifier = Modifier.size(16.dp))
                }
                Text(
                    Strings.intensityDescription(level),
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = if (isSel) palette.onTint else LoadcastColors.Neutral500,
                )
            }
        }
    }
}

@Composable
private fun EstimatedLoadRow(load: Int) {
    Row(
        Modifier.fillMaxWidth().background(LoadcastColors.Accent100)
            .border(1.dp, LoadcastColors.Accent300).padding(horizontal = 15.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("예상 운동 부하", color = LoadcastColors.Accent800)
        Text(
            "$load 점",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            color = LoadcastColors.Accent800,
        )
    }
}

@Composable
private fun SatisfactionSelector(value: Int?, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (1..5).forEach { n ->
            val isSel = value == n
            Box(
                Modifier.weight(1f).height(44.dp)
                    .background(if (isSel) LoadcastColors.Accent else LoadcastColors.Surface)
                    .border(if (isSel) 2.dp else 1.dp, if (isSel) LoadcastColors.Accent else LoadcastColors.Divider)
                    .clickable { onSelect(n) },
                contentAlignment = Alignment.Center,
            ) {
                Text("$n", color = if (isSel) LoadcastColors.Surface else LoadcastColors.Neutral500)
            }
        }
    }
}

@Composable
private fun DurationStepper(duration: Int, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        StepperButton("−") { onChange((duration - 5).coerceAtLeast(1)) }
        Box(
            Modifier.weight(1f).height(46.dp).background(LoadcastColors.Surface).border(1.dp, LoadcastColors.Divider),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$duration 분",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                color = LoadcastColors.Text,
            )
        }
        StepperButton("＋") { onChange(duration + 5) }
    }
}

@Composable
private fun StepperButton(symbol: String, onClick: () -> Unit) {
    Box(
        Modifier.size(46.dp).background(LoadcastColors.Surface).border(1.dp, LoadcastColors.Divider).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(symbol, style = androidx.compose.material3.MaterialTheme.typography.titleLarge) }
}
