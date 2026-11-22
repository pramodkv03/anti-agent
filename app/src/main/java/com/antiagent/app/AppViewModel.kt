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
import com.antiagent.app.data.Personas
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

    var models by mutableStateOf<List<LlmModel>>(emptyList())

    var currentId by mutableStateOf("")
    var screen by mutableStateOf("chat")
    var listVersion by mutableStateOf(0)

    var sending by mutableStateOf(false)
    var streamingText by mutableStateOf("")
    var error by mutableStateOf<String?>(null)
    var draft by mutableStateOf("")
    var pendingImage by mutableStateOf<String?>(null)
    var pendingFileName by mutableStateOf<String?>(null)
    var pendingFileText by mutableStateOf<String?>(null)
    var lastAnswer by mutableStateOf("")
    var searchQuery by mutableStateOf("")

    /** null, "no_key_gemini", "no_key_openrouter", "empty", "failed" */
    var chatErrorKind by mutableStateOf<String?>(null)
    var chatErrorDetail by mutableStateOf<String?>(null)

    var geminiKey by mutableStateOf(prefs.geminiKey)
    var openRouterKey by mutableStateOf(prefs.openRouterKey)
    var modelId by mutableStateOf(prefs.modelId)
    var systemPrompt by mutableStateOf(prefs.systemPrompt)
    var temperature by mutableStateOf(prefs.temperature)
    var autoSpeak by mutableStateOf(prefs.autoSpeak)
    var theme by mutableStateOf(prefs.theme)
    var language by mutableStateOf(prefs.language)
    var replyLang by mutableStateOf(prefs.replyLang)
    var persona by mutableStateOf(prefs.persona)
    var webSearch by mutableStateOf(prefs.webSearch)

    var testingProvider by mutableStateOf("")
    var statusGemini by mutableStateOf("")
    var statusOpenRouter by mutableStateOf("")
    var modelsVersion by mutableStateOf(0)

    private var job: Job? = null
    private var stopRequested = false

    init {
        conversations.addAll(JsonIo.conversationsFromJson(prefs.conversationsJson))
        notes.addAll(JsonIo.notesFromJson(prefs.notesJson))
        models = JsonIo.modelsFromJson(prefs.modelsJson).ifEmpty { Models.defaults }
        if (Models.find(modelId, models) == null) {
            modelId = models.first().id
            prefs.modelId = modelId
        }

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

    val activeModel: LlmModel
        get() = Models.find(modelId, models) ?: models.firstOrNull() ?: Models.defaults.first()

    // ------------------------------------------------------------------ chats

    fun current(): Conversation {
        conversations.firstOrNull { it.id == currentId }?.let { return it }
        val c = newChat()
        currentId = c.id
        return c
    }

    fun newChat(): Conversation {
        val c = Conversation(UUID.randomUUID().toString(), "New chat")
        conversations.add(c)
        currentId = c.id
        chatErrorKind = null
        chatErrorDetail = null
        listVersion++
        save()
        return c
    }

    fun openChat(id: String) {
        currentId = id
        screen = "chat"
        chatErrorKind = null
        chatErrorDetail = null
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

    fun clearAllChats() {
        conversations.clear()
        currentId = ""
        listVersion++
        newChat()
        save()
    }

    fun send() {
        if (sending) return
        ensureUsableModel()
        val image = pendingImage
        val fileText = pendingFileText
        var text = draft.trim()
        if (text.isEmpty() && image == null && fileText == null) return

        if (fileText != null) {
            val name = pendingFileName ?: "file"
            text = (if (text.isEmpty()) "" else text + "\n\n") + "[$name]\n```\n$fileText\n```"
        }

        val conv = current()
        val userText = text.ifBlank { "Describe this image." }
        conv.messages.add(ChatMessage("user", userText, image))
        if (conv.messages.count { it.role == "user" } == 1) {
            conv.title = text.ifBlank { "Photo" }.take(48).replace("\n", " ")
        }
        conv.updatedAt = System.currentTimeMillis()

        draft = ""
        pendingImage = null
        pendingFileName = null
        pendingFileText = null
        listVersion++
        save()

        startCompletion(conv)
    }

    fun retry() {
        if (sending) return
        ensureUsableModel()
        val conv = current()
        while (conv.messages.isNotEmpty() && conv.messages.last().role == "assistant") {
            conv.messages.removeAt(conv.messages.lastIndex)
        }
        if (conv.messages.isNotEmpty()) startCompletion(conv)
    }

    fun regenerate() {
        if (sending) return
        val conv = current()
        while (conv.messages.isNotEmpty() && conv.messages.last().role == "assistant") {
            conv.messages.removeAt(conv.messages.lastIndex)
        }
        if (conv.messages.isNotEmpty()) startCompletion(conv)
    }

    fun stop() {
        stopRequested = true
        Llm.cancel()
        job?.cancel()
    }

    private fun startCompletion(conv: Conversation) {
        val model = activeModel
        val key = if (model.provider == "gemini") geminiKey.trim() else openRouterKey.trim()

        chatErrorKind = null
        chatErrorDetail = null

        if (key.isEmpty()) {
            chatErrorKind = if (model.provider == "gemini") "no_key_gemini" else "no_key_openrouter"
            return
        }

        sending = true
        streamingText = ""
        stopRequested = false
        val system = buildSystemPrompt()
        val history = conv.messages.takeLast(30)
        val web = webSearch && model.provider == "gemini"

        job = viewModelScope.launch {
            var finished = false
            try {
                Llm.stream(model, key, system, history, temperature, web) { delta ->
                    streamingText += delta
                }
                finished = true
            } catch (e: CancellationException) {
                // stopped by the user
            } catch (e: Exception) {
                if (!stopRequested) {
                    chatErrorKind = "failed"
                    chatErrorDetail = e.message ?: "unknown error"
                }
            } finally {
                val answer = streamingText.trim()
                if (answer.isNotEmpty()) {
                    conv.messages.add(ChatMessage("assistant", answer))
                    lastAnswer = answer
                } else if (finished && chatErrorKind == null) {
                    chatErrorKind = "empty"
                }
                sending = false
                streamingText = ""
                conv.updatedAt = System.currentTimeMillis()
                listVersion++
                save()
            }
        }
    }

    private fun buildSystemPrompt(): String {
        val parts = mutableListOf<String>()
        val personaPrompt = Personas.find(persona).prompt
        if (personaPrompt.isNotBlank()) parts.add(personaPrompt)
        when (replyLang) {
            "en" -> parts.add("Always reply in English.")
            "hi" -> parts.add("Always reply in Hindi (Devanagari script).")
            "hinglish" -> parts.add("Always reply in Hinglish: Hindi written in Roman letters.")
            else -> parts.add("Reply in the same language the user writes in.")
        }
        if (systemPrompt.isNotBlank()) parts.add(systemPrompt)
        return parts.joinToString("\n\n")
    }

    // --------------------------------------------------------------- providers

    /**
     * If the selected provider has no key but the other one does, switch to a
     * model that can actually answer. This removes the most common "it does not
     * work even after adding the API key" confusion.
     */
    fun ensureUsableModel() {
        val m = activeModel
        val key = if (m.provider == "gemini") geminiKey.trim() else openRouterKey.trim()
        if (key.isNotEmpty()) return
        val otherKey = if (m.provider == "gemini") openRouterKey.trim() else geminiKey.trim()
        if (otherKey.isEmpty()) return
        val alt = models.firstOrNull { it.provider != m.provider } ?: return
        selectModel(alt.id)
    }

    fun connect(provider: String) {
        val key = if (provider == "gemini") geminiKey.trim() else openRouterKey.trim()
        if (key.isEmpty()) {
            if (provider == "gemini") statusGemini = "PASTE_KEY" else statusOpenRouter = "PASTE_KEY"
            return
        }
        testingProvider = provider
        if (provider == "gemini") statusGemini = "TESTING" else statusOpenRouter = "TESTING"

        viewModelScope.launch {
            try {
                val fetched = if (provider == "gemini") {
                    Llm.fetchGeminiModels(key)
                } else {
                    Llm.fetchOpenRouterModels(key)
                }
                if (fetched.isEmpty()) throw RuntimeException("No models returned for this key")

                val others = models.filter { it.provider != provider }
                models = if (provider == "gemini") fetched + others else others + fetched
                prefs.modelsJson = JsonIo.modelsToJson(models)
                if (Models.find(modelId, models) == null) {
                    modelId = models.first().id
                    prefs.modelId = modelId
                }
                modelsVersion++
                val msg = "CONNECTED:${fetched.size}"
                if (provider == "gemini") statusGemini = msg else statusOpenRouter = msg
            } catch (e: Exception) {
                val msg = "ERROR:" + (e.message ?: "failed")
                if (provider == "gemini") statusGemini = msg else statusOpenRouter = msg
            } finally {
                testingProvider = ""
            }
        }
    }

    /** Sends one tiny real message so the user can see whether the key works. */
    fun testProvider(provider: String) {
        val key = if (provider == "gemini") geminiKey.trim() else openRouterKey.trim()
        val setStatus: (String) -> Unit = {
            if (provider == "gemini") statusGemini = it else statusOpenRouter = it
        }
        if (key.isEmpty()) {
            setStatus("PASTE_KEY")
            return
        }
        testingProvider = provider
        setStatus("TESTING")

        viewModelScope.launch {
            try {
                val model = models.firstOrNull { it.provider == provider }
                    ?: Models.defaults.firstOrNull { it.provider == provider }
                    ?: throw RuntimeException("No model for this provider")
                Llm.testChat(model, key)
                setStatus("OK:${model.label}")
            } catch (e: Exception) {
                setStatus("ERROR:" + (e.message ?: "failed"))
            } finally {
                testingProvider = ""
            }
        }
    }

    fun clearKey(provider: String) {
        if (provider == "gemini") {
            geminiKey = ""
            prefs.geminiKey = ""
            statusGemini = ""
        } else {
            openRouterKey = ""
            prefs.openRouterKey = ""
            statusOpenRouter = ""
        }
    }

    fun diagnosticsText(): String = Llm.lastDebug

    // ---------------------------------------------------------------- settings

    fun changeGeminiKey(v: String) {
        geminiKey = v
        prefs.geminiKey = v
        statusGemini = ""
    }

    fun changeOpenRouterKey(v: String) {
        openRouterKey = v
        prefs.openRouterKey = v
        statusOpenRouter = ""
    }

    fun selectModel(id: String) {
        modelId = id
        prefs.modelId = id
        chatErrorKind = null
        chatErrorDetail = null
    }

    fun changeSystemPrompt(v: String) {
        systemPrompt = v
        prefs.systemPrompt = v
    }

    fun changeTemperature(v: Float) {
        temperature = v
        prefs.temperature = v
    }

    fun changeAutoSpeak(v: Boolean) {
        autoSpeak = v
        prefs.autoSpeak = v
    }

    fun changeTheme(v: String) {
        theme = v
        prefs.theme = v
    }

    fun changeLanguage(v: String) {
        language = v
        prefs.language = v
    }

    fun changeReplyLang(v: String) {
        replyLang = v
        prefs.replyLang = v
    }

    fun selectPersona(id: String) {
        persona = id
        prefs.persona = id
    }

    fun changeWebSearch(v: Boolean) {
        webSearch = v
        prefs.webSearch = v
    }

    fun attachFile(name: String, content: String) {
        pendingFileName = name
        pendingFileText = content.take(200_000)
    }

    fun clearAttachment() {
        pendingImage = null
        pendingFileName = null
        pendingFileText = null
    }

    // ------------------------------------------------------------------- notes

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

    // ------------------------------------------------------------- persistence

    fun save() {
        prefs.conversationsJson = JsonIo.conversationsToJson(conversations)
        prefs.notesJson = JsonIo.notesToJson(notes)
        prefs.activeConversation = currentId
        prefs.modelsJson = JsonIo.modelsToJson(models)
    }
}
