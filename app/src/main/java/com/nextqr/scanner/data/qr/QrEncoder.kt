package com.nextqr.scanner.data.qr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.scale
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.nextqr.scanner.domain.model.ErrorCorrection
import com.nextqr.scanner.domain.model.ModuleShape
import com.nextqr.scanner.domain.model.QrGenerationOptions
import javax.inject.Inject
import javax.inject.Singleton
import android.graphics.BitmapFactory

/**
 * Renders [QrGenerationOptions] into a [Bitmap] using ZXing for the module
 * matrix and a manual draw pass for colour, module shape and centre logo.
 *
 * A logo overlay is capped at ~22% of the code area and only combined with
 * [ErrorCorrection.HIGH]/[ErrorCorrection.QUARTILE] callers so the code stays
 * scannable (enforced in the generator ViewModel).
 */
@Singleton
class QrEncoder @Inject constructor() {

    fun encode(options: QrGenerationOptions): Bitmap {
        require(options.content.isNotBlank()) { "QR content must not be blank" }

        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to options.errorCorrection.toZxing(),
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8",
        )
        val matrix = QRCodeWriter().encode(
            options.content,
            BarcodeFormat.QR_CODE,
            options.sizePx,
            options.sizePx,
            hints,
        )

        val size = options.sizePx
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(options.backgroundColor)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = options.foregroundColor }

        // Determine module pixel size from the matrix dimensions.
        val matrixWidth = matrix.width
        val cell = size.toFloat() / matrixWidth

        for (x in 0 until matrixWidth) {
            for (y in 0 until matrix.height) {
                if (!matrix.get(x, y)) continue
                val left = x * cell
                val top = y * cell
                when (options.moduleShape) {
                    ModuleShape.SQUARE ->
                        canvas.drawRect(left, top, left + cell, top + cell, paint)
                    ModuleShape.ROUNDED ->
                        canvas.drawRoundRect(
                            RectF(left, top, left + cell, top + cell),
                            cell * 0.3f, cell * 0.3f, paint,
                        )
                    ModuleShape.DOT ->
                        canvas.drawCircle(
                            left + cell / 2f, top + cell / 2f, cell * 0.42f, paint,
                        )
                }
            }
        }

        options.logoPngBytes?.let { drawLogo(canvas, bitmap, it, size) }
        return bitmap
    }

    private fun drawLogo(canvas: Canvas, base: Bitmap, logoBytes: ByteArray, size: Int) {
        val logo = BitmapFactory.decodeByteArray(logoBytes, 0, logoBytes.size) ?: return
        val logoSize = (size * 0.22f).toInt()
        val scaled = logo.scale(logoSize, logoSize)

        val left = (size - logoSize) / 2f
        val top = (size - logoSize) / 2f
        val pad = logoSize * 0.12f

        // Punch a clean quiet-zone behind the logo so it doesn't corrupt modules.
        val clear = Paint().apply {
            color = Color.WHITE
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        }
        canvas.drawRoundRect(
            RectF(left - pad, top - pad, left + logoSize + pad, top + logoSize + pad),
            pad, pad, clear,
        )
        canvas.drawBitmap(scaled, null, Rect(left.toInt(), top.toInt(), (left + logoSize).toInt(), (top + logoSize).toInt()), null)
    }

    private fun ErrorCorrection.toZxing(): ErrorCorrectionLevel = when (this) {
        ErrorCorrection.LOW -> ErrorCorrectionLevel.L
        ErrorCorrection.MEDIUM -> ErrorCorrectionLevel.M
        ErrorCorrection.QUARTILE -> ErrorCorrectionLevel.Q
        ErrorCorrection.HIGH -> ErrorCorrectionLevel.H
    }
}
