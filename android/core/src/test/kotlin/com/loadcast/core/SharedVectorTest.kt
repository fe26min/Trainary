package com.loadcast.core

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate

/**
 * spec/test-vectors.json 공유 벡터 검증.
 * iOS(LoadcastCore)의 SharedVectorTests와 반드시 같은 파일·같은 기대값을 사용한다.
 */
class SharedVectorTest {

    private val vectors: JsonObject by lazy {
        val path = System.getProperty("loadcast.vectors.path")
            ?: error("loadcast.vectors.path system property not set")
        JsonParser.parseString(File(path).readText()).asJsonObject
    }

    private fun JsonObject.records(): List<WorkoutRecord> =
        getAsJsonArray("records").map { el ->
            val o = el.asJsonObject
            WorkoutRecord(
                date = LocalDate.parse(o.get("date").asString),
                type = WorkoutType.valueOf(o.get("type").asString),
                durationMin = o.get("durationMin").asInt,
                intensity = Intensity.valueOf(o.get("intensity").asString),
            )
        }

    private fun cases(key: String): List<JsonObject> =
        vectors.getAsJsonArray(key).map { it.asJsonObject }

    @Test
    fun loadCases() {
        for (c in cases("loadCases")) {
            val actual = LoadCalculator.sessionLoad(
                c.get("durationMin").asInt,
                Intensity.valueOf(c.get("intensity").asString),
            )
            assertEquals(c.get("name").asString, c.get("expectedLoad").asInt, actual)
        }
    }

    @Test
    fun acwrCases() {
        for (c in cases("acwrCases")) {
            val name = c.get("name").asString
            val acwr = Acwr.ratio(c.get("acuteLoad").asInt, c.get("chronicLoad").asInt)
            if (c.get("expectedAcwr").isJsonNull) {
                assertNull(name, acwr)
            } else {
                assertNotNull(name, acwr)
                assertEquals(name, c.get("expectedAcwr").asDouble, acwr!!, 1e-12)
            }
            assertEquals(name, c.get("expectedStatus").asString, Acwr.status(acwr).name)
        }
    }

    @Test
    fun weeklySummaryCases() {
        for (c in cases("weeklySummaryCases")) {
            val name = c.get("name").asString
            val expected = c.getAsJsonObject("expected")
            val summary = WeeklyAnalyzer.summarize(c.records(), LocalDate.parse(c.get("today").asString))
            assertEquals(name, expected.get("totalMinutes").asInt, summary.totalMinutes)
            assertEquals(name, expected.get("totalLoad").asInt, summary.totalLoad)
            assertEquals(name, expected.get("highCount").asInt, summary.highCount)
            assertEquals(name, expected.get("restDays").asInt, summary.restDays)
        }
    }

    @Test
    fun fatigueCases() {
        for (c in cases("fatigueCases")) {
            val name = c.get("name").asString
            val actual = BodyFatigue.estimate(c.records(), LocalDate.parse(c.get("today").asString))
            for ((group, expectedLevel) in c.getAsJsonObject("expected").entrySet()) {
                assertEquals(
                    "$name / $group",
                    expectedLevel.asString,
                    actual.getValue(BodyGroup.valueOf(group)).name,
                )
            }
        }
    }

    @Test
    fun recommendationCases() {
        for (c in cases("recommendationCases")) {
            val name = c.get("name").asString
            val expected = c.getAsJsonObject("expected")
            val result = Recommender.recommend(
                records = c.records(),
                today = LocalDate.parse(c.get("today").asString),
                firstUseDate = LocalDate.parse(c.get("firstUseDate").asString),
            )
            when (expected.get("kind").asString) {
                "INSUFFICIENT_DATA" -> {
                    assertTrue(name, result is RecommendationResult.InsufficientData)
                    result as RecommendationResult.InsufficientData
                    assertEquals(name, expected.get("recordedCount").asInt, result.recordedCount)
                    assertEquals(name, expected.get("requiredCount").asInt, result.requiredCount)
                }
                "RECOMMENDATION" -> {
                    assertTrue(name, result is RecommendationResult.Recommendation)
                    result as RecommendationResult.Recommendation
                    assertEquals(name, expected.get("state").asString, result.state.name)
                    assertEquals(name, expected.get("estimate").asBoolean, result.estimate)
                    assertEquals(
                        name,
                        expected.getAsJsonArray("reasonCodes").map { it.asString },
                        result.reasons.map { it.name },
                    )
                    if (expected.get("acwr").isJsonNull) {
                        assertNull(name, result.acwr)
                    } else {
                        assertNotNull(name, result.acwr)
                        assertEquals(name, expected.get("acwr").asDouble, result.acwr!!, 1e-12)
                    }
                    assertEquals(name, expected.get("loadStatus").asString, result.loadStatus.name)
                }
                else -> error("unknown kind in vector: $name")
            }
        }
    }

    @Test
    fun vectorsCoverEveryRecommendationStateAndReason() {
        val states = mutableSetOf<String>()
        val reasons = mutableSetOf<String>()
        for (c in cases("recommendationCases")) {
            val expected = c.getAsJsonObject("expected")
            if (expected.get("kind").asString == "RECOMMENDATION") {
                states += expected.get("state").asString
                expected.getAsJsonArray("reasonCodes").forEach { reasons += it.asString }
            }
        }
        assertEquals(RecommendationState.entries.map { it.name }.toSet(), states)
        assertEquals(ReasonCode.entries.map { it.name }.toSet(), reasons)
    }
}
