package com.nextqr.scanner.data.scanner

import com.google.mlkit.vision.barcode.common.Barcode
import com.nextqr.scanner.domain.model.BarcodeType

/** Maps ML Kit format constants to our domain [BarcodeType]. */
internal fun Int.toBarcodeType(): BarcodeType = when (this) {
    Barcode.FORMAT_QR_CODE -> BarcodeType.QR_CODE
    Barcode.FORMAT_CODE_128 -> BarcodeType.CODE_128
    Barcode.FORMAT_CODE_39 -> BarcodeType.CODE_39
    Barcode.FORMAT_EAN_13 -> BarcodeType.EAN_13
    Barcode.FORMAT_EAN_8 -> BarcodeType.EAN_8
    Barcode.FORMAT_UPC_A -> BarcodeType.UPC_A
    Barcode.FORMAT_UPC_E -> BarcodeType.UPC_E
    Barcode.FORMAT_PDF417 -> BarcodeType.PDF417
    Barcode.FORMAT_AZTEC -> BarcodeType.AZTEC
    Barcode.FORMAT_DATA_MATRIX -> BarcodeType.DATA_MATRIX
    else -> BarcodeType.UNKNOWN
}

/** The full set of formats we ask ML Kit to detect. */
internal val SUPPORTED_ML_KIT_FORMATS = intArrayOf(
    Barcode.FORMAT_QR_CODE,
    Barcode.FORMAT_CODE_128,
    Barcode.FORMAT_CODE_39,
    Barcode.FORMAT_EAN_13,
    Barcode.FORMAT_EAN_8,
    Barcode.FORMAT_UPC_A,
    Barcode.FORMAT_UPC_E,
    Barcode.FORMAT_PDF417,
    Barcode.FORMAT_AZTEC,
    Barcode.FORMAT_DATA_MATRIX,
)
