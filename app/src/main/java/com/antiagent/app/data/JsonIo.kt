package com.antiagent.app.data

import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject

object JsonIo {

    fun conversationsToJson(list: List<Conversation>): String {
        val arr = JSONArray()
        for (c in list) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("title", c.title)
            obj.put("updatedAt", c.updatedAt)
            obj.put("projectId", c.projectId)
            val msgs = JSONArray()
            for (m in c.messages) {
                val mo = JSONObject()
                mo.put("role", m.role)
                mo.put("text", m.text)
                if (m.image != null) mo.put("image", m.image)
                msgs.put(mo)
            }
            obj.put("messages", msgs)
            arr.put(obj)
        }
        return arr.toString()
    }

    fun conversationsFromJson(raw: String): MutableList<Conversation> {
        val out = mutableStateListOf<Conversation>()
        if (raw.isBlank()) return out
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val id = o.optString("id", "")
                if (id.isBlank()) continue
                val conv = Conversation(
                    id = id,
                    title = o.optString("title", "Chat"),
                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                    projectId = o.optString("projectId", "chat")
                )
                val msgs = o.optJSONArray("messages")
                if (msgs != null) {
                    for (j in 0 until msgs.length()) {
                        val mo = msgs.getJSONObject(j)
                        conv.messages.add(
                            ChatMessage(
                                role = mo.optString("role", "assistant"),
                                text = mo.optString("text", ""),
                                image = if (mo.has("image")) mo.optString("image", "") else null
                            )
                        )
                    }
                }
                out.add(conv)
            }
        } catch (_: Exception) {
        }
        return out
    }

    fun notesToJson(list: List<NoteItem>): String {
        val arr = JSONArray()
        for (n in list) {
            arr.put(
                JSONObject()
                    .put("id", n.id)
                    .put("text", n.text)
                    .put("done", n.done)
            )
        }
        return arr.toString()
    }

    fun notesFromJson(raw: String): MutableList<NoteItem> {
        val out = mutableStateListOf<NoteItem>()
        if (raw.isBlank()) return out
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(
                    NoteItem(
                        id = o.optString("id", ""),
                        text = o.optString("text", ""),
                        done = o.optBoolean("done", false)
                    )
                )
            }
        } catch (_: Exception) {
        }
        return out
    }

    fun modelsToJson(list: List<LlmModel>): String {
        val arr = JSONArray()
        for (m in list) {
            arr.put(
                JSONObject()
                    .put("id", m.id)
                    .put("label", m.label)
                    .put("provider", m.provider)
                    .put("context", m.context)
                    .put("vision", m.vision)
                    .put("tools", m.tools)
            )
        }
        return arr.toString()
    }

    fun modelsFromJson(raw: String): List<LlmModel> {
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            val out = ArrayList<LlmModel>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val id = o.optString("id", "")
                if (id.isBlank()) continue
                out.add(
                    LlmModel(
                        id = id,
                        label = o.optString("label", id),
                        provider = o.optString("provider", "gemini"),
                        context = o.optInt("context", 0),
                        vision = o.optBoolean("vision", false),
                        tools = o.optBoolean("tools", false)
                    )
                )
            }
            out
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun projectsToJson(list: List<Project>): String {
        val arr = JSONArray()
        for (p in list) {
            arr.put(
                JSONObject()
                    .put("id", p.id)
                    .put("name", p.name)
                    .put("modelId", p.modelId)
                    .put("persona", p.persona)
                    .put("webSearch", p.webSearch)
                    .put("color", p.color)
                    .put("createdAt", p.createdAt)
            )
        }
        return arr.toString()
    }

    fun projectsFromJson(raw: String): MutableList<Project> {
        val out = mutableStateListOf<Project>()
        if (raw.isBlank()) return out
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val id = o.optString("id", "")
                if (id.isBlank()) continue
                out.add(
                    Project(
                        id = id,
                        name = o.optString("name", "Project"),
                        modelId = o.optString("modelId", "gemini-2.5-flash"),
                        persona = o.optString("persona", "general"),
                        webSearch = o.optBoolean("webSearch", false),
                        color = o.optInt("color", 0),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {
        }
        return out
    }

    fun mcpToJson(list: List<McpServer>): String {
        val arr = JSONArray()
        for (m in list) {
            arr.put(JSONObject().put("id", m.id).put("name", m.name).put("url", m.url).put("enabled", m.enabled))
        }
        return arr.toString()
    }

    fun mcpFromJson(raw: String): MutableList<McpServer> {
        val out = ArrayList<McpServer>()
        if (raw.isBlank()) return out
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val url = o.optString("url", "")
                if (url.isBlank()) continue
                out.add(
                    McpServer(
                        id = o.optString("id", url),
                        name = o.optString("name", "Server"),
                        url = url,
                        enabled = o.optBoolean("enabled", true)
                    )
                )
            }
        } catch (_: Exception) {
        }
        return out
    }

    fun usageToJson(list: List<UsageRow>): String {
        val arr = JSONArray()
        for (u in list) {
            arr.put(
                JSONObject()
                    .put("key", u.key)
                    .put("provider", u.provider)
                    .put("requests", u.requests)
                    .put("inputTokens", u.inputTokens)
                    .put("outputTokens", u.outputTokens)
                    .put("day", u.day)
            )
        }
        return arr.toString()
    }

    fun usageFromJson(raw: String): MutableList<UsageRow> {
        val out = ArrayList<UsageRow>()
        if (raw.isBlank()) return out
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val key = o.optString("key", "")
                if (key.isBlank()) continue
                out.add(
                    UsageRow(
                        key = key,
                        provider = o.optString("provider", ""),
                        requests = o.optLong("requests", 0),
                        inputTokens = o.optLong("inputTokens", 0),
                        outputTokens = o.optLong("outputTokens", 0),
                        day = o.optString("day", "")
                    )
                )
            }
        } catch (_: Exception) {
        }
        return out
    }
}
