package com.nextqr.scanner.domain.model

/**
 * A single scan (or generated code) as persisted in history.
 *
 * @property id stable primary key (0 = not yet persisted).
 * @property rawValue exact decoded string.
 * @property barcodeType symbology it was decoded from / generated as.
 * @property contentType semantic classification of [rawValue].
 * @property timestamp epoch millis of the scan.
 * @property isFavorite user-pinned flag.
 * @property isGenerated true if this entry was created by the QR generator.
 * @property securityVerdict cached URL safety verdict (only for URL content).
 */
data class ScanResult(
    val id: Long = 0,
    val rawValue: String,
    val barcodeType: BarcodeType,
    val contentType: ScanContentType,
    val timestamp: Long,
    val isFavorite: Boolean = false,
    val isGenerated: Boolean = false,
    val securityVerdict: UrlSafetyVerdict? = null,
)
