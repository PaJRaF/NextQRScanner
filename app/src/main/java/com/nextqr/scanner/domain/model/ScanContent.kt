package com.nextqr.scanner.domain.model

/** High-level semantic category of a scanned/entered payload. */
enum class ScanContentType {
    URL,
    TEXT,
    WIFI,
    CONTACT,
    EMAIL,
    SMS,
    PHONE,
    CALENDAR_EVENT,
    GEO,
    CRYPTO,
    PAYMENT,
}

/**
 * Structured, parsed representation of a payload. A [ScanContentType] alone is
 * not enough for the detail screen — each variant exposes the fields the UI and
 * the "smart action" (open, connect, add contact…) need.
 */
sealed interface ParsedContent {
    val type: ScanContentType
    val rawValue: String

    data class Url(
        override val rawValue: String,
        val url: String,
    ) : ParsedContent {
        override val type = ScanContentType.URL
    }

    data class PlainText(
        override val rawValue: String,
    ) : ParsedContent {
        override val type = ScanContentType.TEXT
    }

    data class Wifi(
        override val rawValue: String,
        val ssid: String,
        val password: String?,
        val encryption: Encryption,
        val hidden: Boolean,
    ) : ParsedContent {
        override val type = ScanContentType.WIFI

        enum class Encryption { WPA, WEP, OPEN }
    }

    data class Contact(
        override val rawValue: String,
        val name: String?,
        val organization: String?,
        val phones: List<String>,
        val emails: List<String>,
        val url: String?,
        val address: String?,
    ) : ParsedContent {
        override val type = ScanContentType.CONTACT
    }

    data class Email(
        override val rawValue: String,
        val address: String,
        val subject: String?,
        val body: String?,
    ) : ParsedContent {
        override val type = ScanContentType.EMAIL
    }

    data class Sms(
        override val rawValue: String,
        val number: String,
        val message: String?,
    ) : ParsedContent {
        override val type = ScanContentType.SMS
    }

    data class Phone(
        override val rawValue: String,
        val number: String,
    ) : ParsedContent {
        override val type = ScanContentType.PHONE
    }

    data class CalendarEvent(
        override val rawValue: String,
        val title: String?,
        val location: String?,
        val start: String?,
        val end: String?,
        val description: String?,
    ) : ParsedContent {
        override val type = ScanContentType.CALENDAR_EVENT
    }

    data class Geo(
        override val rawValue: String,
        val latitude: Double,
        val longitude: Double,
        val label: String?,
    ) : ParsedContent {
        override val type = ScanContentType.GEO
    }

    /** BIP-21 style crypto URIs, e.g. bitcoin:, ethereum:, litecoin:. */
    data class Crypto(
        override val rawValue: String,
        val scheme: String,
        val address: String,
        val amount: String?,
    ) : ParsedContent {
        override val type = ScanContentType.CRYPTO
    }

    /**
     * Payment payloads. Covers EPC/SEPA credit-transfer QR ("Girocode") and the
     * Polish "2 przelewy" / recipient-transfer format. Handled read-only: we
     * present the details and hand off to a banking app, never move money
     * ourselves.
     */
    data class Payment(
        override val rawValue: String,
        val recipientName: String?,
        val iban: String?,
        val amount: String?,
        val title: String?,
    ) : ParsedContent {
        override val type = ScanContentType.PAYMENT
    }
}
