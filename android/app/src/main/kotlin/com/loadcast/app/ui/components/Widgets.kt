package com.loadcast.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loadcast.app.ui.theme.LoadcastColors

@Composable
fun FormTopBar(title: String, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(LoadcastColors.Bg).padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = LoadcastColors.Text)
        }
        Text(title, style = MaterialTheme.typography.titleLarge, color = LoadcastColors.Text)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChipFlow(
    items: List<T>,
    isSelected: (T) -> Boolean,
    label: (T) -> String,
    onClick: (T) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        items.forEach { item -> Chip(text = label(item), selected = isSelected(item)) { onClick(item) } }
    }
}

/** 가로 스크롤 필터 칩 행 (기록 목록용). */
@Composable
fun ChipRow(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        labels.forEachIndexed { i, label -> Chip(text = label, selected = i == selectedIndex) { onSelect(i) } }
    }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .background(if (selected) LoadcastColors.Accent else LoadcastColors.Surface)
            .border(1.dp, if (selected) LoadcastColors.Accent else LoadcastColors.Divider)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) LoadcastColors.Surface else LoadcastColors.Neutral700,
        )
    }
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.fillMaxWidth().height(52.dp).background(LoadcastColors.Accent).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = LoadcastColors.Surface, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SecondaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.fillMaxWidth().height(46.dp).background(LoadcastColors.Surface).border(1.dp, LoadcastColors.Divider).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = LoadcastColors.Neutral800, style = MaterialTheme.typography.titleSmall)
    }
}

/** 삭제 확인 모달 — 디자인 화면 14. 삭제는 진한 스틸(Accent900) 강조, 취소는 보조. */
@Composable
fun DeleteConfirmDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
    ConfirmDialog(
        title = "운동 기록을 삭제할까요?",
        message = "삭제한 기록은 되돌릴 수 없으며, 최근 7일 분석에서도 제외됩니다.",
        confirmText = "삭제",
        confirmColor = LoadcastColors.Accent900,
        onCancel = onCancel,
        onConfirm = onConfirm,
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    confirmColor: Color,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Box(
                Modifier.background(confirmColor).clickable(onClick = onConfirm).padding(horizontal = 20.dp, vertical = 10.dp),
            ) { Text(confirmText, color = LoadcastColors.Surface) }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("취소", color = LoadcastColors.Neutral700) } },
        containerColor = LoadcastColors.Surface,
    )
}
