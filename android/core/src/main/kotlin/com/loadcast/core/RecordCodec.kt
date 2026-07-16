package com.loadcast.core

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

/**
 * 기록 목록 ↔ JSON 직렬화 (로컬 파일 저장용).
 * iOS(LoadcastCore RecordCodec)와 같은 포맷: 날짜는 "yyyy-MM-dd" 문자열, enum은 이름 문자열.
 */
object RecordCodec {

    private val gson = GsonBuilder()
        .registerTypeAdapter(
            LocalDate::class.java,
            JsonSerializer<LocalDate> { src, _, _ -> JsonPrimitive(src.toString()) },
        )
        .registerTypeAdapter(
            LocalDate::class.java,
            JsonDeserializer { json, _, _ -> LocalDate.parse(json.asString) },
        )
        .setPrettyPrinting()
        .create()

    private val listType = object : TypeToken<List<WorkoutRecord>>() {}.type

    fun toJson(records: List<WorkoutRecord>): String = gson.toJson(records, listType)

    fun fromJson(json: String): List<WorkoutRecord> =
        if (json.isBlank()) emptyList() else gson.fromJson(json, listType) ?: emptyList()
}
