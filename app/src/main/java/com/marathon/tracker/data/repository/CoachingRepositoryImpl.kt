package com.marathon.tracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.marathon.tracker.data.remote.api.ClaudeApi
import com.marathon.tracker.data.remote.dto.ClaudeMessage
import com.marathon.tracker.data.remote.dto.ClaudeRequest
import com.marathon.tracker.data.workout.WorkoutData
import com.marathon.tracker.domain.model.CoachingReport
import com.marathon.tracker.domain.model.RaceReadiness
import com.marathon.tracker.domain.repository.CoachingRepository
import com.marathon.tracker.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoachingRepositoryImpl @Inject constructor(
    private val claudeApi: ClaudeApi,
    private val dataStore: DataStore<Preferences>,
    private val userPreferencesRepository: UserPreferencesRepository,
) : CoachingRepository {

    private val reportKey = stringPreferencesKey("coaching_report_json")
    private val gson = Gson()

    override fun getLatestReport(): Flow<CoachingReport?> =
        dataStore.data.map { prefs ->
            prefs[reportKey]?.let {
                runCatching { gson.fromJson(it, CoachingReport::class.java) }.getOrNull()
            }
        }

    override fun canGenerateReport(): Flow<Boolean> =
        userPreferencesRepository.getLastReportDateEpochDay().map { lastDay ->
            lastDay == null || lastDay < LocalDate.now().toEpochDay()
        }

    override suspend fun generateReport(weekNumber: Int): Result<CoachingReport> = runCatching {
        val canGenerate = canGenerateReport().first()
        if (!canGenerate) {
            return@runCatching getLatestReport().first()
                ?: error("No cached report available")
        }

        val weekPlan = WorkoutData.getWeekPlan(weekNumber)
        val nextWeekPlan = WorkoutData.getWeekPlan(weekNumber + 1)
        val today = LocalDate.now()
        val nextRace = WorkoutData.getNextRace(today)
        val daysToRace = java.time.temporal.ChronoUnit.DAYS.between(today, nextRace.date).toInt()

        val systemPrompt = buildSystemPrompt()
        val userPrompt = buildUserPrompt(weekNumber, weekPlan, nextWeekPlan, daysToRace, nextRace.name)

        val response = claudeApi.sendMessage(
            ClaudeRequest(
                system = systemPrompt,
                messages = listOf(ClaudeMessage(content = userPrompt)),
            )
        )

        val json = response.content.firstOrNull()?.text ?: error("Empty response from Claude")
        val cleanJson = json.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

        @Suppress("UNCHECKED_CAST")
        val parsed = gson.fromJson(cleanJson, Map::class.java) as Map<String, Any>

        val report = CoachingReport(
            summary = parsed["summary"] as? String ?: "",
            highlights = (parsed["highlights"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            concerns = (parsed["concerns"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            paceAnalysis = parsed["paceAnalysis"] as? String ?: "",
            nextWeekFocus = (parsed["nextWeekFocus"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            adjustments = (parsed["adjustments"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            raceReadiness = parseRaceReadiness(parsed["raceReadiness"]),
            weeklyMotivation = parsed["weeklyMotivation"] as? String ?: "",
            generatedAt = System.currentTimeMillis(),
            weekNumber = weekNumber,
        )

        dataStore.edit { it[reportKey] = gson.toJson(report) }
        userPreferencesRepository.setLastReportDateEpochDay(LocalDate.now().toEpochDay())
        report
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseRaceReadiness(raw: Any?): Map<String, RaceReadiness> {
        if (raw !is Map<*, *>) return emptyMap()
        return (raw as Map<String, Any>).mapValues { (_, v) ->
            if (v is Map<*, *>) {
                val score = ((v["score"] as? Double)?.toInt()) ?: (v["score"] as? Int) ?: 50
                val note = v["note"] as? String ?: ""
                RaceReadiness(score, note)
            } else RaceReadiness(50, "")
        }
    }

    private fun buildSystemPrompt(): String = """
        You are an elite marathon coach with deep expertise in endurance training, lactate threshold work,
        and periodization. You are coaching an athlete with these current PRs: 5K 27:07, 10K 1:00:11,
        HM 2:43:55. Their main issue is an aerobic endurance deficit. The plan focuses on Zone 2 aerobic
        base building, LT work, and long-run progression.

        Race targets: HM Oct 18 2026 sub-2:15, 25K Dec 20 2026 sub-2:45, FM Jan 17 2027 sub-4:30.

        Respond ONLY with valid JSON in the exact schema provided. Be specific, direct, and data-driven.
    """.trimIndent()

    private fun buildUserPrompt(
        weekNumber: Int,
        weekPlan: com.marathon.tracker.domain.model.WeekPlan?,
        nextWeekPlan: com.marathon.tracker.domain.model.WeekPlan?,
        daysToRace: Int,
        raceName: String,
    ): String = """
        Analyse training week $weekNumber of 35 and provide coaching feedback.

        CURRENT WEEK PHASE: ${weekPlan?.phase?.displayName ?: "Unknown"}
        CURRENT WEEK KM TARGET: ${weekPlan?.totalPlannedKm ?: 0} km
        KEY WORKOUT: ${weekPlan?.keyWorkoutDescription ?: "N/A"}

        NEXT WEEK PHASE: ${nextWeekPlan?.phase?.displayName ?: "N/A"}
        NEXT WEEK KM TARGET: ${nextWeekPlan?.totalPlannedKm ?: 0} km

        DAYS TO NEXT RACE: $daysToRace ($raceName)

        Respond with this exact JSON schema:
        {
          "summary": "2-3 sentence overview",
          "highlights": ["achievement 1", "achievement 2"],
          "concerns": ["concern 1", "concern 2"],
          "paceAnalysis": "pace trend analysis string",
          "nextWeekFocus": ["priority 1", "priority 2", "priority 3"],
          "adjustments": ["adjustment suggestion"],
          "raceReadiness": {
            "hm": {"score": 0-100, "note": "string"},
            "race25k": {"score": 0-100, "note": "string"},
            "fm": {"score": 0-100, "note": "string"}
          },
          "weeklyMotivation": "one punchy line"
        }
    """.trimIndent()
}
