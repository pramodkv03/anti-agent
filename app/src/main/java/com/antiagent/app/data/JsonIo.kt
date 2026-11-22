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
                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
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
            // corrupted storage: start clean instead of crashing
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
}
