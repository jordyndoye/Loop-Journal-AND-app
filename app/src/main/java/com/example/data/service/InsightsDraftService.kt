package com.example.data.service

import android.util.Log
import com.example.BuildConfig
import com.example.domain.model.BreakpointChain
import com.example.domain.model.DayBlock
import com.example.domain.model.DayRecord
import com.example.domain.model.ExperimentSuggestion
import com.example.domain.model.TapeSynthesis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Service to execute the silent insights draft call using Gemini model `gemini-3.8-flash`.
 *
 * Rules:
 * - Model: gemini-3.8-flash.
 * - The key stays in the build config, not in a screen and not in the prompt.
 * - Send only that user's stored days: date, block, planned time, outcome, cause, woke ratings, before-sleep ratings.
 * - Ask for JSON only, three fields: playback, chain, experiment. Omit a field if the days do not support it.
 * - Possible, not proven. No percentage, no chart, no diagnosis, no invented days.
 * - If the call fails, times out, or the phone is offline, keep the last note and do not crash.
 * - Never write model output back into the day log.
 */
object InsightsDraftService {

  private const val TAG = "InsightsDraftService"
  private const val MODEL_NAME = "gemini-3.8-flash"
  private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

  private fun safeLog(msg: String, throwable: Throwable? = null) {
    try {
      if (throwable != null) {
        Log.w(TAG, msg, throwable)
      } else {
        Log.i(TAG, msg)
      }
    } catch (_: Throwable) {}
  }

  private val httpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .build()
  }

  data class StoredDayData(
    val dateIso: String,
    val blocks: List<DayBlock>,
    val record: DayRecord?
  )

  data class DraftResult(
    val rawJson: String,
    val synthesis: TapeSynthesis,
    val suggestedExperiment: ExperimentSuggestion?
  )

  /**
   * Generates a silent insights draft if API key is present and network succeeds.
   * If the call fails, times out, or the phone is offline, returns null without throwing.
   */
  suspend fun generateSilentDraft(
    days: List<StoredDayData>
  ): DraftResult? = withContext(Dispatchers.IO) {
    try {
      val apiKey = try {
        BuildConfig.GEMINI_API_KEY
      } catch (_: Throwable) {
        ""
      }

      if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
        safeLog("Gemini API key is not configured; keeping existing draft.")
        return@withContext null
      }

      // Format strictly only the user's stored days:
      // date, block, planned time, outcome, cause, woke ratings, before-sleep ratings
      val storedDaysArray = JSONArray()
      for (day in days) {
        val dayObj = JSONObject()
        dayObj.put("date", day.dateIso)

        val blocksArray = JSONArray()
        for (b in day.blocks) {
          val bObj = JSONObject()
          bObj.put("block", b.title)
          bObj.put("planned_time", b.plannedTime)
          bObj.put("outcome", b.status.name)
          if (!b.cause.isNullOrBlank()) {
            bObj.put("cause", b.cause)
          }
          blocksArray.put(bObj)
        }
        dayObj.put("blocks", blocksArray)

        val wokeObj = JSONObject()
        day.record?.wakeEnergy?.let { wokeObj.put("energy", it) }
        day.record?.wakeMood?.let { wokeObj.put("mood", it) }
        day.record?.wakeStress?.let { wokeObj.put("stress", it) }
        if (wokeObj.length() > 0) {
          dayObj.put("woke_ratings", wokeObj)
        }

        val sleepObj = JSONObject()
        day.record?.sleepEnergy?.let { sleepObj.put("energy", it) }
        day.record?.sleepMood?.let { sleepObj.put("mood", it) }
        day.record?.sleepStress?.let { sleepObj.put("stress", it) }
        if (sleepObj.length() > 0) {
          dayObj.put("before_sleep_ratings", sleepObj)
        }

        storedDaysArray.put(dayObj)
      }

      // Construct prompt text. The key is NEVER included in this prompt.
      val promptText = buildString {
        appendLine("Analyze the following stored daily routine records.")
        appendLine()
        appendLine("CRITICAL INSTRUCTIONS:")
        appendLine("- Return a single valid JSON object containing up to three fields: \"playback\", \"chain\", \"experiment\".")
        appendLine("- Omit a field (or leave it null) if the recorded days do not support it.")
        appendLine("- Possible, not proven. Frame observations mechanically (triggers, mechanisms, downstream impacts).")
        appendLine("- No percentage, no chart, no diagnosis, no invented days.")
        appendLine("- Never diagnose medical or psychological conditions.")
        appendLine("- Never fabricate or extrapolate days not present in the user records.")
        appendLine()
        appendLine("Expected JSON Schema:")
        appendLine("{")
        appendLine("  \"playback\": \"Observational summary of what occurred across the captured days (String)\",")
        appendLine("  \"chain\": [")
        appendLine("    {")
        appendLine("      \"title\": \"Name of observed friction pattern (String)\",")
        appendLine("      \"trigger\": \"What initiated the friction (String)\",")
        appendLine("      \"mechanism\": \"How the drift unfolded (String)\",")
        appendLine("      \"downstreamImpact\": \"Consequence on subsequent blocks (String)\",")
        appendLine("      \"frequencyNote\": \"Context or frequency across the days (String)\"")
        appendLine("    }")
        appendLine("  ],")
        appendLine("  \"experiment\": {")
        appendLine("    \"title\": \"One small intervention title (String)\",")
        appendLine("    \"hypothesis\": \"Hypothesis for the intervention (String)\",")
        appendLine("    \"singleIntervention\": \"Single variable modification (String)\",")
        appendLine("    \"metricToWatch\": \"Observation metric to watch (String)\"")
        appendLine("  }")
        appendLine("}")
        appendLine()
        appendLine("Stored days data:")
        appendLine(storedDaysArray.toString(2))
      }

      val requestJson = JSONObject()
      val contentsArray = JSONArray()
      val contentObj = JSONObject()
      val partsArray = JSONArray()
      val partObj = JSONObject()
      partObj.put("text", promptText)
      partsArray.put(partObj)
      contentObj.put("parts", partsArray)
      contentsArray.put(contentObj)
      requestJson.put("contents", contentsArray)

      val generationConfig = JSONObject()
      generationConfig.put("responseMimeType", "application/json")
      generationConfig.put("temperature", 0.2)
      requestJson.put("generationConfig", generationConfig)

      val url = "$BASE_URL?key=$apiKey"
      val body = requestJson.toString().toRequestBody("application/json".toMediaType())
      val request = Request.Builder()
        .url(url)
        .post(body)
        .build()

      val response = httpClient.newCall(request).execute()
      if (!response.isSuccessful) {
        safeLog("Gemini API returned error code ${response.code}: ${response.message}")
        return@withContext null
      }

      val responseBodyString = response.body?.string() ?: return@withContext null
      val responseJson = JSONObject(responseBodyString)
      val candidates = responseJson.optJSONArray("candidates") ?: return@withContext null
      if (candidates.length() == 0) return@withContext null

      val firstCandidate = candidates.getJSONObject(0)
      val content = firstCandidate.optJSONObject("content") ?: return@withContext null
      val parts = content.optJSONArray("parts") ?: return@withContext null
      if (parts.length() == 0) return@withContext null

      var textOutput = parts.getJSONObject(0).optString("text", "").trim()
      if (textOutput.startsWith("```json")) {
        textOutput = textOutput.removePrefix("```json").trim()
      }
      if (textOutput.startsWith("```")) {
        textOutput = textOutput.removePrefix("```").trim()
      }
      if (textOutput.endsWith("```")) {
        textOutput = textOutput.removeSuffix("```").trim()
      }

      if (textOutput.isBlank()) return@withContext null

      val draftObj = JSONObject(textOutput)

      val playbackNote = draftObj.optString("playback", "").ifBlank {
        "Daily routine records observed across ${days.size} captured days."
      }

      val chainsList = mutableListOf<BreakpointChain>()
      val chainJson = draftObj.opt("chain")
      if (chainJson is JSONArray) {
        for (i in 0 until chainJson.length()) {
          val cObj = chainJson.optJSONObject(i) ?: continue
          chainsList.add(
            BreakpointChain(
              id = "chain_${i + 1}",
              title = cObj.optString("title", "Observed Friction Chain"),
              trigger = cObj.optString("trigger", "Friction trigger"),
              mechanism = cObj.optString("mechanism", "Mechanical progression"),
              downstreamImpact = cObj.optString("downstreamImpact", "Downstream delay"),
              frequencyNote = cObj.optString("frequencyNote", "Observed across captured days")
            )
          )
        }
      } else if (chainJson is JSONObject) {
        chainsList.add(
          BreakpointChain(
            id = "chain_1",
            title = chainJson.optString("title", "Observed Friction Chain"),
            trigger = chainJson.optString("trigger", "Friction trigger"),
            mechanism = chainJson.optString("mechanism", "Mechanical progression"),
            downstreamImpact = chainJson.optString("downstreamImpact", "Downstream delay"),
            frequencyNote = chainJson.optString("frequencyNote", "Observed across captured days")
          )
        )
      }

      var suggestion: ExperimentSuggestion? = null
      val expObj = draftObj.optJSONObject("experiment")
      if (expObj != null) {
        val title = expObj.optString("title", "").trim()
        val hypothesis = expObj.optString("hypothesis", "").trim()
        val singleIntervention = expObj.optString("singleIntervention", "").trim()
        val metricToWatch = expObj.optString("metricToWatch", "").trim()

        if (title.isNotBlank() && singleIntervention.isNotBlank()) {
          suggestion = ExperimentSuggestion(
            id = "sug_${System.currentTimeMillis()}",
            number = 1,
            title = title,
            hypothesis = hypothesis.ifBlank { "Testing single variable change to stabilize routine fidelity." },
            singleIntervention = singleIntervention,
            metricToWatch = metricToWatch.ifBlank { "Routine on-time completion" },
            targetDurationDays = 7
          )
        }
      }

      val synthesis = TapeSynthesis(
        playbackNote = playbackNote,
        daysAnalyzed = days.size,
        capturedDaysCount = days.size,
        totalBreakpointsCount = chainsList.size,
        chains = chainsList,
        oneSuggestedExperiment = suggestion
      )

      DraftResult(
        rawJson = textOutput,
        synthesis = synthesis,
        suggestedExperiment = suggestion
      )
    } catch (t: Throwable) {
      safeLog("Silent insights call failed or offline; keeping last note", t)
      null
    }
  }
}
