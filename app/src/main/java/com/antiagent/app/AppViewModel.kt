package com.antiagent.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.antiagent.app.data.ChatMessage
import com.antiagent.app.data.Conversation
import com.antiagent.app.data.JsonIo
import com.antiagent.app.data.LlmModel
import com.antiagent.app.data.Models
import com.antiagent.app.data.NoteItem
import com.antiagent.app.data.Prefs
import com.antiagent.app.net.Llm
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)

    val conversations = mutableStateListOf<Conversation>()
    val notes = mutableStateListOf<NoteItem>()

    var currentId by mutableStateOf("")
    var screen by mutableStateOf("chat")
    var listVersion by mutableStateOf(0)

    var sending by mutableStateOf(false)
    var streamingText by mutableStateOf("")
    var error by mutableStateOf<String?>(null)
    var draft by mutableStateOf("")
    var pendingImage by mutableStateOf<String?>(null)
    var lastAnswer by mutableStateOf("")

    var geminiKey by mutableStateOf(prefs.geminiKey)
    var openRouterKey by mutableStateOf(prefs.openRouterKey)
    var modelId by mutableStateOf(prefs.modelId)
    var systemPrompt by mutableStateOf(prefs.systemPrompt)
    var temperature by mutableStateOf(prefs.temperature)
    var autoSpeak by mutableStateOf(prefs.autoSpeak)
    var theme by mutableStateOf(prefs.theme)

    private var job: Job? = null
    private var stopRequested = false

    init {
        conversations.addAll(JsonIo.conversationsFromJson(prefs.conversationsJson))
        notes.addAll(JsonIo.notesFromJson(prefs.notesJson))

        val saved = prefs.activeConversation
        val found = conversations.firstOrNull { it.id == saved }
        if (found != null) {
            currentId = found.id
        } else if (conversations.isNotEmpty()) {
            currentId = conversations.maxByOrNull { it.updatedAt }!!.id
        } else {
            currentId = newChat().id
        }
    }

    val activeModel: LlmModel get() = Models.find(modelId) ?: Models.gemini.first()

    fun current(): Conversation {
        conversations.firstOrNull { it.id == currentId }?.let { return it }
        val c = newChat()
        currentId = c.id
        return c
    }

    fun newChat(): Conversation {
        val c = Conversation(UUID.randomUUID().toString(), "Naya chat")
        conversations.add(c)
        currentId = c.id
        listVersion++
        save()
        return c
    }

    fun openChat(id: String) {
        currentId = id
        screen = "chat"
        save()
    }

    fun deleteChat(id: String) {
        conversations.removeAll { it.id == id }
        if (currentId == id) {
            currentId = conversations.maxByOrNull { it.updatedAt }?.id ?: newChat().id
        }
        listVersion++
        save()
    }

    fun stop() {
        stopRequested = true
        Llm.cancel()
        job?.cancel()
    }

    fun send() {
        if (sending) return
        val text = draft.trim()
        val image = pendingImage
        if (text.isEmpty() && image == null) return

        val model = activeModel
        val key = if (model.provider == "gemini") geminiKey.trim() else openRouterKey.trim()
        if (key.isEmpty()) {
            error = if (model.provider == "gemini") {
                "Gemini API key nahi hai. Settings me free key daalo (1 minute ka kaam)."
            } else {
                "OpenRouter API key nahi hai. Settings me free key daalo."
            }
            return
        }

        val conv = current()
        conv.messages.add(ChatMessage("user", text.ifBlank { "Is photo me kya hai?" }, image))
        if (conv.messages.count { it.role == "user" } == 1) {
            conv.title = text.ifBlank { "Photo" }.take(48).replace("\n", " ")
        }
        conv.updatedAt = System.currentTimeMillis()

        val history = conv.messages.toList()
        draft = ""
        pendingImage = null
        error = null
        sending = true
        streamingText = ""
        stopRequested = false
        listVersion++
        save()

        job = viewModelScope.launch {
            try {
                Llm.stream(model, key, systemPrompt, history, temperature) { delta ->
                    streamingText += delta
                }
                commitAnswer(conv)
            } catch (ce: CancellationException) {
                commitAnswer(conv)
                throw ce
            } catch (e: Exception) {
                commitAnswer(conv)
                if (!stopRequested) error = readableError(e)
            } finally {
                sending = false
                streamingText = ""
                conv.updatedAt = System.currentTimeMillis()
                listVersion++
                save()
            }
        }
    }

    private fun commitAnswer(conv: Conversation) {
        val answer = streamingText.trim()
        if (answer.isNotEmpty()) {
            conv.messages.add(ChatMessage("assistant", answer))
            lastAnswer = answer
        }
    }

    private fun readableError(e: Exception): String {
        val m = e.message ?: "Network error"
        return when {
            m.contains("API_KEY_INVALID") || m.contains("API key not valid") ->
                "Key galat hai. Settings me key dobara paste karo."
            m.contains("HTTP 401") || m.contains("HTTP 403") ->
                "Key galat ya expire hai. Settings me key check karo."
            m.contains("HTTP 402") -> "Is model ka free quota khatam hai. Doosra model try karo."
            m.contains("HTTP 429") -> "Free limit khatam. Thodi der baad ya doosra model try karo."
            m.contains("HTTP 404") -> "Model nahi mila. Doosra model choose karo."
            m.contains("HTTP 5") -> "Server busy hai. Thodi der baad try karo."
            else -> m.take(400)
        }
    }

    // ---- settings ----

    fun setGeminiKey(v: String) { geminiKey = v; prefs.geminiKey = v }
    fun setOpenRouterKey(v: String) { openRouterKey = v; prefs.openRouterKey = v }
    fun setModel(id: String) { modelId = id; prefs.modelId = id }
    fun setSystemPrompt(v: String) { systemPrompt = v; prefs.systemPrompt = v }
    fun setTemperature(v: Float) { temperature = v; prefs.temperature = v }
    fun setAutoSpeak(v: Boolean) { autoSpeak = v; prefs.autoSpeak = v }
    fun setTheme(v: String) { theme = v; prefs.theme = v }

    // ---- notes ----

    fun addNote(text: String) {
        val t = text.trim()
        if (t.isEmpty()) return
        notes.add(NoteItem(UUID.randomUUID().toString(), t))
        save()
    }

    fun toggleNote(id: String) {
        val idx = notes.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val n = notes[idx]
            notes[idx] = n.copy(done = !n.done)
            save()
        }
    }

    fun deleteNote(id: String) {
        notes.removeAll { it.id == id }
        save()
    }

    // ---- persistence ----

    fun save() {
        prefs.conversationsJson = JsonIo.conversationsToJson(conversations)
        prefs.notesJson = JsonIo.notesToJson(notes)
        prefs.activeConversation = currentId
    }
}
