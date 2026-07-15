package com.nextqr.scanner.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Saves a QR bitmap to the public Pictures collection via MediaStore. On
 * Android 10+ this needs no storage permission (scoped storage); on older
 * devices the manifest declares no legacy write permission, so we still use the
 * app-scoped MediaStore insert which the platform allows.
 */
@Composable
fun rememberSaveQrToGallery(): (Bitmap) -> Unit {
    val context = LocalContext.current
    return { bitmap -> saveBitmap(context, bitmap) }
}

private fun saveBitmap(context: Context, bitmap: Bitmap) {
    val filename = "NextQR_${System.currentTimeMillis()}.png"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/NextQR",
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    if (uri == null) {
        Toast.makeText(context, "Save failed", Toast.LENGTH_SHORT).show()
        return
    }
    resolver.openOutputStream(uri)?.use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
    }
    Toast.makeText(context, "Saved to gallery", Toast.LENGTH_SHORT).show()
}
