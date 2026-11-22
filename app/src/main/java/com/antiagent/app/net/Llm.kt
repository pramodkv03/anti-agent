package com.antiagent.app.net

import com.antiagent.app.data.ChatMessage
import com.antiagent.app.data.LlmModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Minimal streaming client for Gemini and OpenAI-compatible (OpenRouter) endpoints.
 * No third party libraries: plain HttpURLConnection + org.json.
 */
object Llm {

    @Volatile
    private var active: HttpURLConnection? = null

    fun cancel() {
        try {
            active?.disconnect()
        } catch (_: Exception) {
        }
    }

    suspend fun stream(
        model: LlmModel,
        key: String,
        system: String,
        history: List<ChatMessage>,
        temperature: Float,
        onDelta: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        if (model.provider == "gemini") {
            streamGemini(model, key, system, history, temperature, onDelta)
        } else {
            streamOpenRouter(model, key, system, history, temperature, onDelta)
        }
    }

    private fun streamGemini(
        model: LlmModel,
        key: String,
        system: String,
        history: List<ChatMessage>,
        temperature: Float,
        onDelta: (String) -> Unit
    ) {
        val contents = JSONArray()
        for (m in history) {
            val parts = JSONArray()
            if (m.image != null) {
                parts.put(
                    JSONObject().put(
                        "inline_data",
                        JSONObject().put("mime_type", "image/jpeg").put("data", m.image)
                    )
                )
            }
            parts.put(JSONObject().put("text", m.text))
            contents.put(
                JSONObject()
                    .put("role", if (m.role == "user") "user" else "model")
                    .put("parts", parts)
            )
        }

        val body = JSONObject()
            .put("contents", contents)
            .put("generationConfig", JSONObject().put("temperature", temperature.toDouble()))

        if (system.isNotBlank()) {
            body.put(
                "systemInstruction",
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system)))
            )
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/" +
            "${model.id}:streamGenerateContent?alt=sse&key=$key"

        streamSse(
            url = url,
            headers = mapOf("Content-Type" to "application/json"),
            body = body.toString()
        ) { data ->
            val obj = JSONObject(data)
            val cands = obj.optJSONArray("candidates")
            if (cands != null && cands.length() > 0) {
                val parts = cands.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                if (parts != null) {
                    val sb = StringBuilder()
                    for (i in 0 until parts.length()) {
                        val p = parts.getJSONObject(i)
                        if (p.optBoolean("thought", false)) continue
                        if (p.has("text")) sb.append(p.optString("text", ""))
                    }
                    if (sb.isNotEmpty()) onDelta(sb.toString())
                }
            } else {
                val err = obj.optJSONObject("error")
                if (err != null) throw RuntimeException(err.optString("message", "Gemini error"))
            }
        }
    }

    private fun streamOpenRouter(
        model: LlmModel,
        key: String,
        system: String,
        history: List<ChatMessage>,
        temperature: Float,
        onDelta: (String) -> Unit
    ) {
        val messages = JSONArray()
        if (system.isNotBlank()) {
            messages.put(JSONObject().put("role", "system").put("content", system))
        }
        for (m in history) {
            val mo = JSONObject().put("role", if (m.role == "user") "user" else "assistant")
            if (m.image != null) {
                val content = JSONArray()
                    .put(JSONObject().put("type", "text").put("text", m.text))
                    .put(
                        JSONObject().put("type", "image_url").put(
                            "image_url",
                            JSONObject().put("url", "data:image/jpeg;base64,${m.image}")
                        )
                    )
                mo.put("content", content)
            } else {
                mo.put("content", m.text)
            }
            messages.put(mo)
        }

        val body = JSONObject()
            .put("model", model.id)
            .put("stream", true)
            .put("messages", messages)
            .put("temperature", temperature.toDouble())

        streamSse(
            url = "https://openrouter.ai/api/v1/chat/completions",
            headers = mapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer $key",
                "HTTP-Referer" to "https://github.com/antiagent",
                "X-Title" to "Anti Agent"
            ),
            body = body.toString()
        ) { data ->
            val obj = JSONObject(data)
            val err = obj.optJSONObject("error")
            if (err != null) throw RuntimeException(err.optString("message", "OpenRouter error"))
            val choices = obj.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val delta = choices.getJSONObject(0).optJSONObject("delta")
                val text = delta?.optString("content", "") ?: ""
                if (text.isNotEmpty()) onDelta(text)
            }
        }
    }

    private fun streamSse(
        url: String,
        headers: Map<String, String>,
        body: String,
        onData: (String) -> Unit
    ) {
        val conn = URL(url).openConnection() as HttpURLConnection
        active = conn
        try {
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.connectTimeout = 30_000
            conn.readTimeout = 180_000
            conn.setRequestProperty("Accept", "text/event-stream")
            headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }

            val bytes = body.toByteArray(Charsets.UTF_8)
            conn.setFixedLengthStreamingMode(bytes.size)
            conn.outputStream.use { it.write(bytes) }

            val code = conn.responseCode
            if (code !in 200..299) {
                val errText = try {
                    conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                } catch (_: Exception) {
                    ""
                }
                throw RuntimeException("HTTP $code ${errText.take(500)}")
            }

            conn.inputStream.bufferedReader().use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    if (line.startsWith("data:")) {
                        val data = line.removePrefix("data:").trim()
                        if (data.isNotEmpty() && data != "[DONE]") onData(data)
                    }
                    line = reader.readLine()
                }
            }
        } finally {
            active = null
            try {
                conn.disconnect()
            } catch (_: Exception) {
            }
        }
    }
}
