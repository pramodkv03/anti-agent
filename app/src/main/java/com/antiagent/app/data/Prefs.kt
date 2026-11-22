package com.antiagent.app.data

import android.content.Context

class Prefs(ctx: Context) {
    private val sp = ctx.applicationContext.getSharedPreferences("anti_agent_prefs", Context.MODE_PRIVATE)

    var geminiKey: String
        get() = sp.getString("gemini_key", "") ?: ""
        set(value) = sp.edit().putString("gemini_key", value).apply()

    var openRouterKey: String
        get() = sp.getString("openrouter_key", "") ?: ""
        set(value) = sp.edit().putString("openrouter_key", value).apply()

    var modelId: String
        get() = sp.getString("model_id", "gemini-2.5-flash") ?: "gemini-2.5-flash"
        set(value) = sp.edit().putString("model_id", value).apply()

    var systemPrompt: String
        get() = sp.getString("system_prompt", DEFAULT_SYSTEM_PROMPT) ?: DEFAULT_SYSTEM_PROMPT
        set(value) = sp.edit().putString("system_prompt", value).apply()

    var temperature: Float
        get() = sp.getFloat("temperature", 0.8f)
        set(value) = sp.edit().putFloat("temperature", value).apply()

    var autoSpeak: Boolean
        get() = sp.getBoolean("auto_speak", false)
        set(value) = sp.edit().putBoolean("auto_speak", value).apply()

    var theme: String
        get() = sp.getString("theme", "midnight") ?: "midnight"
        set(value) = sp.edit().putString("theme", value).apply()

    var language: String
        get() = sp.getString("language", "en") ?: "en"
        set(value) = sp.edit().putString("language", value).apply()

    var replyLang: String
        get() = sp.getString("reply_lang", "auto") ?: "auto"
        set(value) = sp.edit().putString("reply_lang", value).apply()

    var persona: String
        get() = sp.getString("persona", "general") ?: "general"
        set(value) = sp.edit().putString("persona", value).apply()

    var webSearch: Boolean
        get() = sp.getBoolean("web_search", false)
        set(value) = sp.edit().putBoolean("web_search", value).apply()

    var conversationsJson: String
        get() = sp.getString("conversations", "") ?: ""
        set(value) = sp.edit().putString("conversations", value).apply()

    var notesJson: String
        get() = sp.getString("notes", "") ?: ""
        set(value) = sp.edit().putString("notes", value).apply()

    var activeConversation: String
        get() = sp.getString("active_conversation", "") ?: ""
        set(value) = sp.edit().putString("active_conversation", value).apply()

    var modelsJson: String
        get() = sp.getString("models", "") ?: ""
        set(value) = sp.edit().putString("models", value).apply()

    companion object {
        const val DEFAULT_SYSTEM_PROMPT =
            "You are Anti Agent, a helpful AI assistant on the user's phone. " +
                "Be accurate and practical. Reply in the same language the user writes in. " +
                "Use markdown lists and code blocks when they help."
    }
}
