package com.example.vetbook.presentation.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

fun compressImageForAvatar(
    context: Context,
    uri: Uri,
    maxSizePx: Int = 512,
    quality: Int = 80
): ByteArray? {
    return try {
        val input = context.contentResolver.openInputStream(uri) ?: return null
        val original = BitmapFactory.decodeStream(input) ?: return null
        input.close()

        val width = original.width
        val height = original.height
        if (width <= 0 || height <= 0) return null

        val scale = (maxSizePx.toFloat() / maxOf(width, height)).coerceAtMost(1f)
        val targetWidth = (width * scale).toInt()
        val targetHeight = (height * scale).toInt()
        val scaledBitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(original, targetWidth, targetHeight, true)
        } else {
            original
        }

        val stream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        if (scaledBitmap != original) {
            scaledBitmap.recycle()
        }
        original.recycle()
        stream.toByteArray()
    } catch (_: Exception) {
        null
    }
}

