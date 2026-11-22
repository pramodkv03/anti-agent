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
 * Streaming client for Gemini and OpenAI-compatible (OpenRouter) endpoints.
 * Plain HttpURLConnection + org.json, no third party libraries.
 *
 * If a provider streams nothing (proxy buffers SSE, unusual gateway) the same
 * answer is fetched once more without streaming, so the chat still works.
 */
object Llm {

    @Volatile
    private var active: HttpURLConnection? = null

    /** Human readable info about the last request; shown in Settings → Diagnostics. */
    @Volatile
    var lastDebug: String = ""

    fun cancel() {
        try {
            active?.disconnect()
        } catch (_: Exception) {
        }
    }

    // ---------------------------------------------------------------- streaming

    suspend fun stream(
        model: LlmModel,
        key: String,
        system: String,
        history: List<ChatMessage>,
        temperature: Float,
        webSearch: Boolean,
        onDelta: (String) -> Unit,
        onUsage: (Int, Int) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        var got = false
        val cb: (String) -> Unit = { delta ->
            if (delta.isNotEmpty()) {
                got = true
                onDelta(delta)
            }
        }
        if (model.provider == "gemini") {
            streamGemini(model, key, system, history, temperature, webSearch, cb, onUsage)
            if (!got) {
                val text = generateGemini(model, key, system, history, temperature, webSearch, onUsage = onUsage)
                if (text.isNotBlank()) cb(text)
            }
        } else {
            streamOpenRouter(model, key, system, history, temperature, cb, onUsage)
            if (!got) {
                val text = generateOpenRouter(model, key, system, history, temperature, onUsage = onUsage)
                if (text.isNotBlank()) cb(text)
            }
        }
    }

    // ------------------------------------------------------------------ testing

    /** One small request used by Settings → Test. Throws with a readable reason. */
    suspend fun testChat(model: LlmModel, key: String): String = withContext(Dispatchers.IO) {
        val history = listOf(ChatMessage("user", "Reply with exactly: OK"))
        val text = if (model.provider == "gemini") {
            generateGemini(model, key, "", history, 0.1f, false, maxTokens = 24)
        } else {
            generateOpenRouter(model, key, "", history, 0.1f, maxTokens = 24)
        }
        if (text.isBlank()) throw RuntimeException("The answer was empty")
        text
    }

    /** One-shot completion used by tools (browser automation, terminal AI, MCP). */
    suspend fun generate(
        model: LlmModel,
        key: String,
        system: String,
        prompt: String
    ): String = withContext(Dispatchers.IO) {
        val history = listOf(ChatMessage("user", prompt))
        if (model.provider == "gemini") {
            generateGemini(model, key, system, history, 0.1f, false)
        } else {
            generateOpenRouter(model, key, system, history, 0.1f)
        }
    }

    // -------------------------------------------------------------- Gemini core

    private fun geminiBody(
        system: String,
        history: List<ChatMessage>,
        temperature: Float,
        webSearch: Boolean,
        maxTokens: Int?
    ): JSONObject {
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
            if (m.text.isNotBlank()) parts.put(JSONObject().put("text", m.text))
            contents.put(
                JSONObject()
                    .put("role", if (m.role == "user") "user" else "model")
                    .put("parts", parts)
            )
        }

        val gen = JSONObject().put("temperature", temperature.toDouble())
        if (maxTokens != null) gen.put("maxOutputTokens", maxTokens)

        val body = JSONObject()
            .put("contents", contents)
            .put("generationConfig", gen)

        if (system.isNotBlank()) {
            body.put(
                "systemInstruction",
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system)))
            )
        }
        if (webSearch) {
            body.put("tools", JSONArray().put(JSONObject().put("google_search", JSONObject())))
        }
        return body
    }

    private fun geminiHeaders(key: String) = mapOf(
        "Content-Type" to "application/json",
        "x-goog-api-key" to key
    )

    private fun geminiText(root: JSONObject): String {
        val err = root.optJSONObject("error")
        if (err != null) throw RuntimeException(err.optString("message", "Gemini error"))

        val cands = root.optJSONArray("candidates") ?: return ""
        if (cands.length() == 0) return ""
        val parts = cands.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts") ?: return ""
        val sb = StringBuilder()
        for (i in 0 until parts.length()) {
            val p = parts.getJSONObject(i)
            if (p.optBoolean("thought", false)) continue
            if (p.has("text")) sb.append(p.optString("text", ""))
        }
        return sb.toString()
    }

    private fun streamGemini(
        model: LlmModel,
        key: String,
        system: String,
        history: List<ChatMessage>,
        temperature: Float,
        webSearch: Boolean,
        onDelta: (String) -> Unit,
        onUsage: (Int, Int) -> Unit
    ) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/" +
            "${model.id}:streamGenerateContent?alt=sse"
        streamSse(
            url = url,
            headers = geminiHeaders(key),
            body = geminiBody(system, history, temperature, webSearch, null).toString(),
            label = model.id
        ) { data ->
            val obj = JSONObject(data)
            val text = geminiText(obj)
            if (text.isNotEmpty()) onDelta(text)
            reportGeminiUsage(obj, onUsage)
        }
    }

    private fun generateGemini(
        model: LlmModel,
        key: String,
        system: String,
        history: List<ChatMessage>,
        temperature: Float,
        webSearch: Boolean,
        maxTokens: Int? = null,
        onUsage: (Int, Int) -> Unit = { _, _ -> }
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/" +
            "${model.id}:generateContent"
        val raw = httpPostJson(
            url = url,
            headers = geminiHeaders(key),
            body = geminiBody(system, history, temperature, webSearch, maxTokens).toString(),
            label = model.id
        )
        val obj = JSONObject(raw)
        reportGeminiUsage(obj, onUsage)
        return geminiText(obj)
    }

    private fun reportGeminiUsage(root: JSONObject, onUsage: (Int, Int) -> Unit) {
        val u = root.optJSONObject("usageMetadata") ?: return
        val input = u.optInt("promptTokenCount", 0)
        val output = u.optInt("candidatesTokenCount", 0) + u.optInt("thoughtsTokenCount", 0)
        if (input + output > 0) onUsage(input, output)
    }

    // ---------------------------------------------------------- OpenRouter core

    private fun orMessages(system: String, history: List<ChatMessage>): JSONArray {
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
        return messages
    }

    private fun orHeaders(key: String) = mapOf(
        "Content-Type" to "application/json",
        "Authorization" to "Bearer $key",
        "HTTP-Referer" to "https://github.com/pramodkv03/anti-agent",
        "X-Title" to "Anti Agent"
    )

    private fun orText(root: JSONObject): String {
        val err = root.optJSONObject("error")
        if (err != null) throw RuntimeException(err.optString("message", "OpenRouter error"))
        val choices = root.optJSONArray("choices") ?: return ""
        if (choices.length() == 0) return ""
        val msg = choices.getJSONObject(0).optJSONObject("message") ?: return ""
        return msg.optString("content", "")
    }

    private fun streamOpenRouter(
        model: LlmModel,
        key: String,
        system: String,
        history: List<ChatMessage>,
        temperature: Float,
        onDelta: (String) -> Unit,
        onUsage: (Int, Int) -> Unit
    ) {
        val body = JSONObject()
            .put("model", model.id)
            .put("stream", true)
            .put("stream_options", JSONObject().put("include_usage", true))
            .put("messages", orMessages(system, history))
            .put("temperature", temperature.toDouble())

        streamSse(
            url = "https://openrouter.ai/api/v1/chat/completions",
            headers = orHeaders(key),
            body = body.toString(),
            label = model.id
        ) { data ->
            val obj = JSONObject(data)
            val err = obj.optJSONObject("error")
            if (err != null) throw RuntimeException(err.optString("message", "OpenRouter error"))

            reportOpenRouterUsage(obj, onUsage)
            val choices = obj.optJSONArray("choices") ?: return@streamSse
            if (choices.length() == 0) return@streamSse
            val delta = choices.getJSONObject(0).optJSONObject("delta") ?: return@streamSse
            if (delta.isNull("content")) return@streamSse
            val text = delta.optString("content", "")
            if (text.isNotEmpty()) onDelta(text)
        }
    }

    private fun generateOpenRouter(
        model: LlmModel,
        key: String,
        system: String,
        history: List<ChatMessage>,
        temperature: Float,
        maxTokens: Int? = null,
        onUsage: (Int, Int) -> Unit = { _, _ -> }
    ): String {
        val body = JSONObject()
            .put("model", model.id)
            .put("stream", false)
            .put("messages", orMessages(system, history))
            .put("temperature", temperature.toDouble())
        if (maxTokens != null) body.put("max_tokens", maxTokens)

        val raw = httpPostJson(
            url = "https://openrouter.ai/api/v1/chat/completions",
            headers = orHeaders(key),
            body = body.toString(),
            label = model.id
        )
        val obj = JSONObject(raw)
        reportOpenRouterUsage(obj, onUsage)
        return orText(obj)
    }

    private fun reportOpenRouterUsage(root: JSONObject, onUsage: (Int, Int) -> Unit) {
        val u = root.optJSONObject("usage") ?: return
        val input = u.optInt("prompt_tokens", 0)
        val output = u.optInt("completion_tokens", 0)
        if (input + output > 0) onUsage(input, output)
    }

    // ------------------------------------------------------------- SSE plumbing

    private fun streamSse(
        url: String,
        headers: Map<String, String>,
        body: String,
        label: String,
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
                val raw = readStreamText(conn.errorStream)
                lastDebug = "$label → HTTP $code\n${raw.take(1200)}"
                throw RuntimeException("HTTP $code • ${extractError(raw)}")
            }
            lastDebug = "$label → HTTP $code (streaming)"

            conn.inputStream.bufferedReader().use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    val trimmed = line.trim()
                    if (trimmed.startsWith("data:")) {
                        val data = trimmed.removePrefix("data:").trim()
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

    private fun httpPostJson(
        url: String,
        headers: Map<String, String>,
        body: String,
        label: String
    ): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        active = conn
        try {
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.connectTimeout = 30_000
            conn.readTimeout = 120_000
            conn.setRequestProperty("Accept", "application/json")
            headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }

            val bytes = body.toByteArray(Charsets.UTF_8)
            conn.setFixedLengthStreamingMode(bytes.size)
            conn.outputStream.use { it.write(bytes) }

            val code = conn.responseCode
            val isOk = code in 200..299
            val raw = readStreamText(if (isOk) conn.inputStream else conn.errorStream)
            lastDebug = "$label → HTTP $code\n${raw.take(1200)}"
            if (!isOk) throw RuntimeException("HTTP $code • ${extractError(raw)}")
            return raw
        } finally {
            active = null
            try {
                conn.disconnect()
            } catch (_: Exception) {
            }
        }
    }

    // ------------------------------------------------------------------ listing

    suspend fun fetchGeminiModels(key: String): List<LlmModel> = withContext(Dispatchers.IO) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models?pageSize=200"
        val raw = httpGet(url, mapOf("x-goog-api-key" to key), "Gemini models")
        val root = JSONObject(raw)
        val arr = root.optJSONArray("models") ?: return@withContext emptyList()
        val out = ArrayList<LlmModel>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val name = o.optString("name", "").removePrefix("models/")
            if (name.isBlank()) continue
            val methods = o.optJSONArray("supportedGenerationMethods")
            var canGenerate = false
            if (methods != null) {
                for (j in 0 until methods.length()) {
                    if (methods.optString(j) == "generateContent") canGenerate = true
                }
            }
            if (!canGenerate) continue
            val lower = name.lowercase()
            if (lower.contains("embedding") || lower.contains("aqa") || lower.contains("imagen") ||
                lower.contains("veo") || lower.contains("tts") || lower.contains("live") ||
                lower.contains("native-audio")
            ) continue
            val label = o.optString("displayName", "").ifBlank { name }
            val context = o.optInt("inputTokenLimit", 0)
            val vision = lower.contains("flash") || lower.contains("pro") || lower.contains("gemma") ||
                lower.contains("vision")
            val tools = !lower.contains("lite")
            out.add(LlmModel(name, label, "gemini", context, vision, tools))
        }
        out
    }

    suspend fun fetchOpenRouterModels(key: String): List<LlmModel> = withContext(Dispatchers.IO) {
        // 1) validate the key (clear error if it is wrong)
        httpGet(
            "https://openrouter.ai/api/v1/key",
            mapOf("Authorization" to "Bearer $key"),
            "OpenRouter key"
        )

        // 2) list models, keep the free ones
        val raw = httpGet(
            "https://openrouter.ai/api/v1/models",
            emptyMap(),
            "OpenRouter models"
        )
        val root = JSONObject(raw)
        val arr = root.optJSONArray("data") ?: return@withContext emptyList()
        val out = ArrayList<LlmModel>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val id = o.optString("id", "")
            if (id.isBlank()) continue
            val pricing = o.optJSONObject("pricing")
            val prompt = pricing?.optString("prompt", "1") ?: "1"
            val completion = pricing?.optString("completion", "1") ?: "1"
            val isFree = prompt.toDoubleOrNull() == 0.0 && completion.toDoubleOrNull() == 0.0
            if (!isFree) continue
            val label = o.optString("name", "").ifBlank { id }
            val lower = id.lowercase()
            val context = o.optInt("context_length", 0)
            val vision = lower.contains("gemma-3") || lower.contains("vision") ||
                lower.contains("vl") || lower.contains("llava") || lower.contains("pixtral")
            var tools = false
            val params = o.optJSONArray("supported_parameters")
            if (params != null) {
                for (j in 0 until params.length()) {
                    if (params.optString(j) == "tools") tools = true
                }
            }
            out.add(LlmModel(id, label, "openrouter", context, vision, tools))
            if (out.size >= 400) break
        }
        out
    }

    /** OpenRouter returns the key's own usage and limit (dollars). */
    suspend fun fetchOpenRouterKeyInfo(key: String): String = withContext(Dispatchers.IO) {
        httpGet(
            "https://openrouter.ai/api/v1/key",
            mapOf("Authorization" to "Bearer $key"),
            "OpenRouter key info"
        )
    }

    private fun httpGet(url: String, headers: Map<String, String>, label: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        active = conn
        try {
            conn.requestMethod = "GET"
            conn.connectTimeout = 30_000
            conn.readTimeout = 45_000
            conn.setRequestProperty("Accept", "application/json")
            headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
            val code = conn.responseCode
            val isOk = code in 200..299
            val raw = readStreamText(if (isOk) conn.inputStream else conn.errorStream)
            lastDebug = "$label → HTTP $code\n${raw.take(1200)}"
            if (!isOk) throw RuntimeException("HTTP $code • ${extractError(raw)}")
            return raw
        } finally {
            active = null
            try {
                conn.disconnect()
            } catch (_: Exception) {
            }
        }
    }

    private fun readStreamText(stream: java.io.InputStream?): String {
        if (stream == null) return ""
        return try {
            stream.bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            ""
        }
    }

    /** Turns provider error JSON into one short sentence the user can act on. */
    private fun extractError(raw: String): String {
        if (raw.isBlank()) return "empty error response — check your internet"
        val lower = raw.lowercase()
        val friendly = when {
            lower.contains("429") || lower.contains("rate-limit") ||
                lower.contains("rate limit") || lower.contains("rpm_limit_exceeded") ||
                lower.contains("too many requests") ->
                "That free model is rate-limited right now — retry in a minute or pick another model"
            lower.contains("api_key_invalid") || lower.contains("api key not valid") ->
                "This API key is not valid"
            lower.contains("permission_denied") || lower.contains("key was disabled") ->
                "This key is blocked or has no permission"
            lower.contains("resource_exhausted") || lower.contains("quota") ->
                "Free limit reached — wait a bit or pick another model"
            lower.contains("no endpoints found") || lower.contains("not a valid model") ->
                "This model is not available for your key"
            lower.contains("location is not supported") ->
                "Gemini API does not work in this region"
            lower.contains("credit") || lower.contains("payment") ->
                "This model needs credit — pick a :free model"
            else -> null
        }

        val detail = try {
            val obj = JSONObject(raw)
            val err = obj.optJSONObject("error")
            val msg = err?.optString("message", "") ?: ""
            if (msg.isNotBlank()) msg else raw.take(300)
        } catch (_: Exception) {
            raw.take(300)
        }

        return if (friendly != null) "$friendly — ${detail.take(220)}" else detail
    }
}
