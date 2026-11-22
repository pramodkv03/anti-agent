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
    var updatedAt: Long = System.currentTimeMillis()
)

data class NoteItem(
    val id: String,
    val text: String,
    val done: Boolean = false
)

data class LlmModel(
    val id: String,
    val label: String,
    val provider: String        // "gemini" | "openrouter"
) {
    val providerLabel: String get() = if (provider == "gemini") "Gemini" else "OpenRouter"
}

object Models {
    val defaults = listOf(
        LlmModel("gemini-2.5-flash", "Gemini 2.5 Flash", "gemini"),
        LlmModel("gemini-2.5-flash-lite", "Gemini 2.5 Flash Lite", "gemini"),
        LlmModel("gemini-2.0-flash", "Gemini 2.0 Flash", "gemini"),
        LlmModel("gemini-2.5-pro", "Gemini 2.5 Pro", "gemini"),
        LlmModel("deepseek/deepseek-chat-v3-0324:free", "DeepSeek V3 (free)", "openrouter"),
        LlmModel("meta-llama/llama-3.3-70b-instruct:free", "Llama 3.3 70B (free)", "openrouter"),
        LlmModel("qwen/qwen3-235b-a22b:free", "Qwen3 235B (free)", "openrouter"),
        LlmModel("google/gemma-3-27b-it:free", "Gemma 3 27B (free)", "openrouter"),
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
