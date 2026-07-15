package com.loadcast.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.loadcast.app.ui.theme.LoadcastColors
import com.loadcast.core.RecommendationResult
import com.loadcast.core.RecommendationState
import com.loadcast.core.Recommender
import com.loadcast.core.WorkoutRecord
import java.time.LocalDate

/**
 * 홈 대시보드 스텁 — 코어 로직 연동 확인용.
 * TODO: 디자인 화면 04(히어로 추천 카드 · 현재 상태 요약 · 부위별 피로 · 최근 7일 · 최근 운동) 구현.
 * TODO: 저장소(로컬 DB + Supabase) 연결 후 샘플 데이터 제거.
 */
@Composable
fun HomeScreen(
    records: List<WorkoutRecord> = emptyList(),
    today: LocalDate = LocalDate.now(),
    firstUseDate: LocalDate = today,
) {
    val result = remember(records, today, firstUseDate) {
        Recommender.recommend(records, today, firstUseDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LoadcastColors.Bg)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("오늘 운동 상태를\n확인해보세요.", style = MaterialTheme.typography.headlineSmall)

        when (result) {
            is RecommendationResult.InsufficientData -> InsufficientDataCard(
                recordedCount = result.recordedCount,
                requiredCount = result.requiredCount,
            )
            is RecommendationResult.Recommendation -> RecommendationCard(
                state = result.state,
                estimate = result.estimate,
            )
        }
    }
}

@Composable
private fun InsufficientDataCard(recordedCount: Int, requiredCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LoadcastColors.Surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("아직 추천을 만들기 위한\n운동 기록이 부족합니다.", style = MaterialTheme.typography.titleMedium)
            Text(
                "$recordedCount / $requiredCount 기록됨 — 최소 ${requiredCount}개의 운동을 기록하면 분석할 수 있어요.",
                style = MaterialTheme.typography.bodySmall,
                color = LoadcastColors.Neutral600,
            )
        }
    }
}

@Composable
private fun RecommendationCard(state: RecommendationState, estimate: Boolean) {
    val (label, tint, container) = when (state) {
        RecommendationState.HIGH_OK -> Triple("고강도 가능", LoadcastColors.Ok800, LoadcastColors.Ok100)
        RecommendationState.MODERATE -> Triple("중강도 권장", LoadcastColors.Warn800, LoadcastColors.Warn100)
        RecommendationState.RECOVERY -> Triple("회복 권장", LoadcastColors.Rest800, LoadcastColors.Rest100)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("오늘 운동 추천", style = MaterialTheme.typography.labelSmall, color = tint)
            Text(label, style = MaterialTheme.typography.headlineMedium, color = tint)
            if (estimate) {
                Text("초기 추정 — 기록이 4주 쌓이면 더 정확해져요.", style = MaterialTheme.typography.bodySmall, color = tint)
            }
        }
    }
}
