package com.antiagent.app.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object FileUtil {

    /** Returns (name, text) or (name, null) when the file is too large, or null on failure. */
    fun readText(ctx: Context, uri: Uri, maxBytes: Int = 200_000): Pair<String, String?>? {
        return try {
            var name = "file"
            ctx.contentResolver.query(uri, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && c.moveToFirst()) {
                    name = c.getString(idx) ?: "file"
                }
            }
            val bytes = ctx.contentResolver.openInputStream(uri)?.use { stream ->
                val buffer = ByteArray(8192)
                val out = java.io.ByteArrayOutputStream()
                var total = 0
                while (true) {
                    val n = stream.read(buffer)
                    if (n <= 0) break
                    total += n
                    if (total > maxBytes) return Pair(name, null)
                    out.write(buffer, 0, n)
                }
                out.toByteArray()
            } ?: return null
            Pair(name, String(bytes, Charsets.UTF_8))
        } catch (_: Exception) {
            null
        }
    }
}
