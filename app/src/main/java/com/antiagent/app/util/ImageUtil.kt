package com.antiagent.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.max

object ImageUtil {

    fun encode(ctx: Context, uri: Uri, maxSide: Int = 1024): String? {
        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            ctx.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            var sample = 1
            while (max(bounds.outWidth, bounds.outHeight) / sample > maxSide * 2) {
                sample *= 2
            }

            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            var bmp = ctx.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            } ?: return null

            val scale = maxSide.toFloat() / max(bmp.width, bmp.height)
            if (scale < 1f) {
                val w = (bmp.width * scale).toInt().coerceAtLeast(1)
                val h = (bmp.height * scale).toInt().coerceAtLeast(1)
                bmp = Bitmap.createScaledBitmap(bmp, w, h, true)
            }

            val out = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, out)
            return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        } catch (_: Exception) {
            return null
        }
    }

    fun decode(b64: String): Bitmap? {
        return try {
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }
}
