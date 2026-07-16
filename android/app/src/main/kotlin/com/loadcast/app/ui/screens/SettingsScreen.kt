package com.loadcast.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.loadcast.app.ui.components.SquareCard
import com.loadcast.app.ui.theme.LoadcastColors

/**
 * 설정 — 디자인 화면 10.
 * MVP: 프로필/앱 정보 요약. 계정(로그아웃·탈퇴)은 Supabase 인증 연동 시 활성화 (docs/AUTH.md).
 */
@Composable
fun SettingsScreen(recordCount: Int) {
    Column(
        Modifier.fillMaxSize().background(LoadcastColors.Bg).verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("설정", style = MaterialTheme.typography.headlineSmall, color = LoadcastColors.Text)

        SquareCard {
            Text("프로필", style = MaterialTheme.typography.titleMedium)
            Text("로그인 연동 후 닉네임·운동 목적·종목이 표시됩니다.", style = MaterialTheme.typography.bodySmall, color = LoadcastColors.Neutral600)
        }

        SquareCard {
            Text("데이터", style = MaterialTheme.typography.titleMedium)
            Text("저장된 운동 기록 · ${recordCount}건", style = MaterialTheme.typography.bodyMedium, color = LoadcastColors.Neutral800)
            Text("현재 기록은 기기에 로컬 저장됩니다. 계정 연동 시 클라우드 동기화 예정.", style = MaterialTheme.typography.labelSmall, color = LoadcastColors.Neutral500)
        }

        SquareCard {
            Text("앱 정보", style = MaterialTheme.typography.titleMedium)
            Text("LOADCAST · v0.1.0", style = MaterialTheme.typography.bodyMedium, color = LoadcastColors.Neutral800)
        }
    }
}
