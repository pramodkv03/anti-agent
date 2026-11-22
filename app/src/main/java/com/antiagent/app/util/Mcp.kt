package com.antiagent.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

/** Minimal MCP client: sends a JSON-RPC initialize to a server URL and reports the reply. */
object Mcp {

    suspend fun ping(serverUrl: String): String = withContext(Dispatchers.IO) {
        val url = URL(serverUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 8000
        conn.readTimeout = 12000
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json, text/event-stream")
        conn.doOutput = true
        val body = JSONObject()
            .put("jsonrpc", "2.0")
            .put("id", 1)
            .put("method", "initialize")
            .put(
                "params",
                JSONObject()
                    .put("protocolVersion", "2024-11-05")
                    .put("capabilities", JSONObject())
                    .put(
                        "clientInfo",
                        JSONObject().put("name", "AntiAgent").put("version", "1.1")
                    )
            ).toString()
        try {
            val out: OutputStream = conn.outputStream
            out.write(body.toByteArray(Charsets.UTF_8))
            out.flush()
            out.close()
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use(BufferedReader::readText) ?: ""
            val pretty = try {
                val obj = JSONObject(text)
                val result = obj.optJSONObject("result")
                val name = result?.optJSONObject("serverInfo")?.optString("name", "")
                if (!name.isNullOrBlank()) "Connected to $name" else text.take(300)
            } catch (_: Exception) {
                text.take(300)
            }
            if (code in 200..299) pretty else "HTTP $code: $pretty"
        } finally {
            conn.disconnect()
        }
    }
}
