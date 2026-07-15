package com.nextqr.scanner.domain.model

/** Error-correction level exposed by the generator UI. */
enum class ErrorCorrection { LOW, MEDIUM, QUARTILE, HIGH }

/** Module (dot) rendering style for stylised QR codes (premium feature). */
enum class ModuleShape { SQUARE, ROUNDED, DOT }

/**
 * All parameters used to render a QR code. Colour customisation, logo overlay
 * and non-square module shapes are premium features; the free tier is limited
 * to a plain black-on-white code (enforced in the ViewModel, not here).
 *
 * @property foregroundColor ARGB int.
 * @property backgroundColor ARGB int.
 * @property logoPngBytes optional centre logo (PNG), overlaid after rendering.
 * @property sizePx target bitmap size in pixels.
 */
data class QrGenerationOptions(
    val content: String,
    val foregroundColor: Int = 0xFF000000.toInt(),
    val backgroundColor: Int = 0xFFFFFFFF.toInt(),
    val errorCorrection: ErrorCorrection = ErrorCorrection.MEDIUM,
    val moduleShape: ModuleShape = ModuleShape.SQUARE,
    val logoPngBytes: ByteArray? = null,
    val sizePx: Int = 1024,
) {
    // Explicit equals/hashCode because of the ByteArray field.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QrGenerationOptions) return false
        return content == other.content &&
            foregroundColor == other.foregroundColor &&
            backgroundColor == other.backgroundColor &&
            errorCorrection == other.errorCorrection &&
            moduleShape == other.moduleShape &&
            sizePx == other.sizePx &&
            (logoPngBytes?.contentEquals(other.logoPngBytes) ?: (other.logoPngBytes == null))
    }

    override fun hashCode(): Int {
        var result = content.hashCode()
        result = 31 * result + foregroundColor
        result = 31 * result + backgroundColor
        result = 31 * result + errorCorrection.hashCode()
        result = 31 * result + moduleShape.hashCode()
        result = 31 * result + sizePx
        result = 31 * result + (logoPngBytes?.contentHashCode() ?: 0)
        return result
    }
}
