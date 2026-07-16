package com.loadcast.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.loadcast.app.ui.Strings
import com.loadcast.app.ui.components.SquareCard
import com.loadcast.app.ui.components.StatusBadge
import com.loadcast.app.ui.components.FormTopBar
import com.loadcast.app.ui.components.PrimaryButton
import com.loadcast.app.ui.components.SecondaryButton
import com.loadcast.app.ui.components.paletteOf
import com.loadcast.app.ui.theme.LoadcastColors
import com.loadcast.core.RecommendationResult
import com.loadcast.core.Recommender
import com.loadcast.core.WorkoutRecord
import java.time.LocalDate

/**
 * 오늘 운동 추천 상세 — 디자인 화면 09.
 * 상태·권장 강도 · 설명 · 이유(≤3) · 추천 운동(≤3) · 주의 운동 · 면책 문구.
 */
@Composable
fun RecommendationScreen(
    records: List<WorkoutRecord>,
    today: LocalDate,
    firstUseDate: LocalDate,
    onAdd: () -> Unit,
    onBack: () -> Unit,
) {
    val result = remember(records, today, firstUseDate) { Recommender.recommend(records, today, firstUseDate) }

    Column(Modifier.fillMaxSize().background(LoadcastColors.Bg)) {
        FormTopBar(title = "오늘 운동 추천", onBack = onBack)

        val rec = result as? RecommendationResult.Recommendation
        if (rec == null) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("아직 추천을 만들기 위한 운동 기록이 부족합니다.", style = MaterialTheme.typography.titleMedium)
                PrimaryButton("운동 기록 추가", onClick = onAdd)
            }
            return
        }

        val palette = paletteOf(rec.state)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StatusBadge(Strings.stateLabel(rec.state), palette)
            Text(Strings.stateHeadline(rec.state), style = MaterialTheme.typography.headlineMedium, color = LoadcastColors.Text)
            Text(Strings.stateDescription(rec.state), style = MaterialTheme.typography.bodyMedium, color = LoadcastColors.Neutral800)

            if (rec.reasons.isNotEmpty()) {
                SquareCard {
                    Text("추천 이유", style = MaterialTheme.typography.titleMedium)
                    rec.reasons.forEach { code ->
                        Text("· ${Strings.reasonLabel(code)}", style = MaterialTheme.typography.bodyMedium, color = LoadcastColors.Neutral800)
                    }
                }
            }

            SquareCard {
                Text("추천 운동", style = MaterialTheme.typography.titleMedium)
                Strings.suggestedWorkouts(rec.state).forEach { (name, detail) ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text(name, style = MaterialTheme.typography.titleSmall, color = LoadcastColors.Text)
                        Text(detail, style = MaterialTheme.typography.bodySmall, color = LoadcastColors.Neutral600)
                    }
                }
            }

            Strings.cautionText(rec.state)?.let { caution ->
                Column(
                    Modifier.fillMaxWidth().background(LoadcastColors.Neutral100)
                        .padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text("주의 운동", style = MaterialTheme.typography.titleSmall)
                    Text(caution, style = MaterialTheme.typography.bodySmall, color = LoadcastColors.Neutral700)
                }
            }

            Text(Strings.DISCLAIMER, style = MaterialTheme.typography.labelSmall, color = LoadcastColors.Neutral500)

            PrimaryButton("운동 기록 추가", onClick = onAdd)
            SecondaryButton("홈으로 돌아가기", onClick = onBack)
        }
    }
}
