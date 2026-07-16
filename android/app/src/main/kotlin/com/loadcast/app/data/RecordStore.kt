package com.loadcast.app.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.loadcast.core.RecordCodec
import com.loadcast.core.WorkoutRecord
import java.io.File
import java.time.LocalDate

/**
 * 기록 로컬 저장소 — 파일(JSON) 기반.
 * MVP 단계: 파일 하나 + 동기 IO (기록 수백 건 수준에선 충분).
 * TODO: Supabase 연동 시 이 저장소를 원격 동기화 캐시로 전환 (docs/AUTH.md).
 */
class RecordStore(private val file: File) {

    /** 항상 날짜 내림차순 정렬 유지. Compose가 직접 관찰한다. */
    val records: SnapshotStateList<WorkoutRecord> = mutableStateListOf()

    init {
        val json = if (file.exists()) file.readText() else ""
        records.addAll(RecordCodec.fromJson(json).sortedByDescending { it.date })
    }

    fun find(id: String): WorkoutRecord? = records.find { it.id == id }

    /** 가입일 대용 — 가장 이른 기록 날짜 (없으면 오늘). TODO: 인증 연동 후 계정 생성일 사용. */
    fun firstUseDate(today: LocalDate): LocalDate =
        records.minOfOrNull { it.date } ?: today

    fun upsert(record: WorkoutRecord) {
        records.removeAll { it.id == record.id }
        records.add(record)
        records.sortByDescending { it.date }
        persist()
    }

    fun delete(id: String) {
        records.removeAll { it.id == id }
        persist()
    }

    private fun persist() {
        file.writeText(RecordCodec.toJson(records.toList()))
    }

    companion object {
        fun create(context: Context): RecordStore =
            RecordStore(File(context.filesDir, "records.json"))
    }
}
