package com.loadcast.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RecordCodecTest {

    private val sample = listOf(
        WorkoutRecord(
            date = LocalDate.of(2026, 7, 14),
            type = WorkoutType.CROSSFIT,
            durationMin = 60,
            intensity = Intensity.HIGH,
            id = "id-1",
            satisfaction = 4,
            memo = "오늘 WOD 정말 힘들었다.",
        ),
        WorkoutRecord(
            date = LocalDate.of(2026, 7, 12),
            type = WorkoutType.OTHER,
            durationMin = 35,
            intensity = Intensity.LOW,
            id = "id-2",
            customTypeLabel = "클라이밍",
        ),
    )

    @Test
    fun roundTripPreservesAllFields() {
        val decoded = RecordCodec.fromJson(RecordCodec.toJson(sample))
        assertEquals(sample, decoded)
    }

    @Test
    fun emptyAndBlankInputDecodeToEmptyList() {
        assertTrue(RecordCodec.fromJson("").isEmpty())
        assertTrue(RecordCodec.fromJson("  ").isEmpty())
        assertTrue(RecordCodec.fromJson("[]").isEmpty())
    }

    @Test
    fun dateIsSerializedAsIsoString() {
        val json = RecordCodec.toJson(sample)
        assertTrue(json.contains("\"2026-07-14\""))
    }
}
