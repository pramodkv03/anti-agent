package com.antiagent.app.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class ChatMessage(
    val role: String,          // "user" | "assistant"
    val text: String,
    val image: String? = null  // base64 jpeg
)

class Conversation(
    val id: String,
    var title: String,
    val messages: SnapshotStateList<ChatMessage> = mutableStateListOf(),
    var updatedAt: Long = System.currentTimeMillis(),
    var projectId: String = "chat"
)

data class NoteItem(
    val id: String,
    val text: String,
    val done: Boolean = false
)

data class LlmModel(
    val id: String,
    val label: String,
    val provider: String,        // "gemini" | "openrouter" | ...
    val context: Int = 0,
    val vision: Boolean = false,
    val tools: Boolean = false
) {
    val providerLabel: String get() = provider.replaceFirstChar { it.uppercase() }

    val tags: String
        get() = buildList {
            if (vision) add("Vision")
            if (tools) add("Tools")
            if (context >= 1000) add("${context / 1000}k ctx")
        }.joinToString(" · ")
}

object Models {
    val defaults = listOf(
        LlmModel("gemini-3.5-flash", "Gemini 3.5 Flash", "gemini", 1_048_576, true, true),
        LlmModel("gemini-3.5-flash-lite", "Gemini 3.5 Flash-Lite", "gemini", 1_048_576, true, true),
        LlmModel("gemini-flash-lite-latest", "Gemini Flash-Lite (latest)", "gemini", 1_048_576, true, true),
        LlmModel("gemini-2.5-pro", "Gemini 2.5 Pro (older keys)", "gemini", 1_048_576, true, true),
        LlmModel("deepseek/deepseek-chat-v3-0324:free", "DeepSeek V3 (free)", "openrouter"),
        LlmModel("meta-llama/llama-3.3-70b-instruct:free", "Llama 3.3 70B (free)", "openrouter"),
        LlmModel("google/gemma-3-27b-it:free", "Gemma 3 27B (free)", "openrouter", vision = true),
        LlmModel("mistralai/mistral-small-3.2-24b-instruct:free", "Mistral Small 3.2 (free)", "openrouter")
    )

    fun find(id: String, list: List<LlmModel>): LlmModel? =
        list.firstOrNull { it.id == id } ?: defaults.firstOrNull { it.id == id }
}

data class Persona(val id: String, val prompt: String)

object Personas {
    val all = listOf(
        Persona("general", ""),
        Persona(
            "coder",
            "You are an expert software engineer. Give correct, runnable code, keep explanations short, " +
                "and point out pitfalls. Use markdown code blocks with the language name."
        ),
        Persona(
            "writer",
            "You are a skilled writer and editor. Write clear, engaging copy, improve the user's text, " +
                "and offer a couple of variants when useful."
        ),
        Persona(
            "teacher",
            "You are a patient teacher. Explain step by step with simple examples, and end with one small " +
                "practice question."
        ),
        Persona(
            "translator",
            "You are a professional translator. Translate accurately, keep the tone and formatting, and do " +
                "not add explanations unless asked."
        )
    )

    fun find(id: String): Persona = all.firstOrNull { it.id == id } ?: all.first()
}

/** A workspace: its own chats, model and behaviour. */
data class Project(
    val id: String,
    val name: String,
    val modelId: String,
    val persona: String = "general",
    val webSearch: Boolean = false,
    val color: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

object Projects {
    const val ALL = "all"

    val defaults = listOf(
        Project("chat", "Chat", "gemini-3.5-flash", "general", false, 0),
        Project("coding", "Coding", "gemini-3.5-flash", "coder", false, 1),
        Project("writing", "Writing", "gemini-3.5-flash", "writer", false, 2),
        Project("study", "Study", "gemini-3.5-flash", "teacher", false, 3),
        Project("research", "Research", "gemini-3.5-flash", "general", true, 4),
        Project("translate", "Translate", "gemini-3.5-flash", "translator", false, 5)
    )
}

data class McpServer(
    val id: String,
    val name: String,
    val url: String,
    val enabled: Boolean = true
)

data class UsageRow(
    val key: String,
    val provider: String,
    var requests: Long = 0,
    var inputTokens: Long = 0,
    var outputTokens: Long = 0,
    var day: String = ""
)
