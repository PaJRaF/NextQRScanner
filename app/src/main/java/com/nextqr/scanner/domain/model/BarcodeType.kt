package com.nextqr.scanner.domain.model

/**
 * Supported symbologies. Mirrors the subset of ML Kit formats we advertise to
 * users and configure the scanner for. Keeping this in the domain layer means
 * ViewModels and use cases never depend on ML Kit constants directly.
 */
enum class BarcodeType {
    QR_CODE,
    CODE_128,
    CODE_39,
    EAN_13,
    EAN_8,
    UPC_A,
    UPC_E,
    PDF417,
    AZTEC,
    DATA_MATRIX,
    UNKNOWN;

    /** True for 2D symbologies that can carry rich structured payloads. */
    val is2d: Boolean
        get() = this == QR_CODE || this == PDF417 || this == AZTEC || this == DATA_MATRIX
}
