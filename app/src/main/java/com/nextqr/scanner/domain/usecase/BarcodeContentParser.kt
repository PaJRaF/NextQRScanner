package com.nextqr.scanner.domain.usecase

import com.nextqr.scanner.domain.model.ParsedContent
import javax.inject.Inject

/**
 * Pure-Kotlin parser that turns a raw decoded string into a structured
 * [ParsedContent]. Deliberately free of Android dependencies so it can be
 * covered exhaustively by fast JVM unit tests.
 *
 * The ordering of checks matters: more specific prefixes are matched before the
 * generic URL/text fallbacks.
 */
class BarcodeContentParser @Inject constructor() {

    fun parse(raw: String): ParsedContent {
        val trimmed = raw.trim()
        val lower = trimmed.lowercase()

        return when {
            lower.startsWith("wifi:") -> parseWifi(trimmed)
            lower.startsWith("begin:vcard") -> parseVCard(trimmed)
            lower.startsWith("mecard:") -> parseMeCard(trimmed)
            lower.startsWith("begin:vevent") ||
                lower.startsWith("begin:vcalendar") -> parseEvent(trimmed)
            lower.startsWith("mailto:") -> parseMailto(trimmed)
            lower.startsWith("matmsg:") -> parseMatMsg(trimmed)
            lower.startsWith("smsto:") || lower.startsWith("sms:") -> parseSms(trimmed)
            lower.startsWith("tel:") -> ParsedContent.Phone(trimmed, trimmed.substring(4))
            lower.startsWith("geo:") -> parseGeo(trimmed)
            isEpcPayment(trimmed) -> parsePayment(trimmed)
            CRYPTO_SCHEMES.any { lower.startsWith("$it:") } -> parseCrypto(trimmed)
            isPlainEmail(trimmed) -> ParsedContent.Email(trimmed, trimmed, null, null)
            isUrl(trimmed) -> ParsedContent.Url(trimmed, normalizeUrl(trimmed))
            else -> ParsedContent.PlainText(trimmed)
        }
    }

    // --- Wi-Fi: WIFI:T:WPA;S:ssid;P:password;H:true; ---
    private fun parseWifi(raw: String): ParsedContent {
        val fields = parseSemicolonFields(raw.removePrefix("WIFI:").removePrefix("wifi:"))
        val enc = when (fields["T"]?.uppercase()) {
            "WPA", "WPA2", "WPA3" -> ParsedContent.Wifi.Encryption.WPA
            "WEP" -> ParsedContent.Wifi.Encryption.WEP
            else -> ParsedContent.Wifi.Encryption.OPEN
        }
        return ParsedContent.Wifi(
            rawValue = raw,
            ssid = fields["S"].orEmpty(),
            password = fields["P"],
            encryption = enc,
            hidden = fields["H"].equals("true", ignoreCase = true),
        )
    }

    private fun parseVCard(raw: String): ParsedContent {
        val lines = raw.lines()
        fun value(prefix: String) = lines
            .firstOrNull { it.uppercase().startsWith(prefix) }
            ?.substringAfter(':')?.trim()
        val phones = lines.filter { it.uppercase().startsWith("TEL") }
            .map { it.substringAfter(':').trim() }
        val emails = lines.filter { it.uppercase().startsWith("EMAIL") }
            .map { it.substringAfter(':').trim() }
        return ParsedContent.Contact(
            rawValue = raw,
            name = value("FN") ?: value("N"),
            organization = value("ORG"),
            phones = phones,
            emails = emails,
            url = value("URL"),
            address = value("ADR")?.replace(";", " ")?.trim(),
        )
    }

    // MECARD:N:name;TEL:123;EMAIL:a@b.com;;
    private fun parseMeCard(raw: String): ParsedContent {
        val fields = parseSemicolonFields(raw.removePrefix("MECARD:").removePrefix("mecard:"))
        return ParsedContent.Contact(
            rawValue = raw,
            name = fields["N"],
            organization = fields["ORG"],
            phones = listOfNotNull(fields["TEL"]),
            emails = listOfNotNull(fields["EMAIL"]),
            url = fields["URL"],
            address = fields["ADR"],
        )
    }

    private fun parseEvent(raw: String): ParsedContent {
        fun field(name: String) = Regex("$name[:;][^\\r\\n]*", RegexOption.IGNORE_CASE)
            .find(raw)?.value?.substringAfter(':')?.trim()
        return ParsedContent.CalendarEvent(
            rawValue = raw,
            title = field("SUMMARY"),
            location = field("LOCATION"),
            start = field("DTSTART"),
            end = field("DTEND"),
            description = field("DESCRIPTION"),
        )
    }

    private fun parseMailto(raw: String): ParsedContent {
        val withoutScheme = raw.removePrefix("mailto:").removePrefix("MAILTO:")
        val address = withoutScheme.substringBefore('?')
        val params = parseQuery(withoutScheme.substringAfter('?', ""))
        return ParsedContent.Email(raw, address, params["subject"], params["body"])
    }

    // MATMSG:TO:a@b.com;SUB:subject;BODY:body;;
    private fun parseMatMsg(raw: String): ParsedContent {
        val fields = parseSemicolonFields(raw.removePrefix("MATMSG:").removePrefix("matmsg:"))
        return ParsedContent.Email(raw, fields["TO"].orEmpty(), fields["SUB"], fields["BODY"])
    }

    private fun parseSms(raw: String): ParsedContent {
        val body = raw.substringAfter(':')
        val number = body.substringBefore(':').substringBefore('?')
        val message = if (body.contains(':')) body.substringAfter(':', "").ifEmpty { null } else null
        return ParsedContent.Sms(raw, number, message)
    }

    // geo:lat,lng?q=label
    private fun parseGeo(raw: String): ParsedContent {
        val coords = raw.removePrefix("geo:").substringBefore('?')
        val lat = coords.substringBefore(',').toDoubleOrNull() ?: 0.0
        val lng = coords.substringAfter(',').substringBefore(',').toDoubleOrNull() ?: 0.0
        val label = parseQuery(raw.substringAfter('?', ""))["q"]
        return ParsedContent.Geo(raw, lat, lng, label)
    }

    private fun parseCrypto(raw: String): ParsedContent {
        val scheme = raw.substringBefore(':')
        val rest = raw.substringAfter(':')
        val address = rest.substringBefore('?')
        val amount = parseQuery(rest.substringAfter('?', ""))["amount"]
        return ParsedContent.Crypto(raw, scheme, address, amount)
    }

    // EPC/SEPA "Girocode": BCD\n<ver>\n...\nBIC\nName\nIBAN\nEURamount\n...
    private fun parsePayment(raw: String): ParsedContent {
        val lines = raw.lines()
        val name = lines.getOrNull(5)?.trim()
        val iban = lines.getOrNull(6)?.trim()
        val amount = lines.getOrNull(7)?.trim()?.removePrefix("EUR")
        val title = lines.getOrNull(9)?.trim()
        return ParsedContent.Payment(raw, name, iban, amount?.ifBlank { null }, title?.ifBlank { null })
    }

    private fun isEpcPayment(raw: String): Boolean =
        raw.lineSequence().firstOrNull()?.trim().equals("BCD", ignoreCase = true)

    // --- helpers ---

    private fun parseSemicolonFields(body: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        // Split on ';' but respect '\;' escaping used by the Wi-Fi spec.
        val tokens = body.split(Regex("(?<!\\\\);"))
        for (token in tokens) {
            if (!token.contains(':')) continue
            val key = token.substringBefore(':').trim().uppercase()
            val value = token.substringAfter(':').replace("\\;", ";").replace("\\:", ":")
            if (key.isNotEmpty()) result[key] = value
        }
        return result
    }

    private fun parseQuery(query: String): Map<String, String> =
        query.split('&').filter { it.contains('=') }.associate {
            it.substringBefore('=') to decode(it.substringAfter('='))
        }

    private fun decode(s: String): String =
        s.replace('+', ' ').replace(Regex("%([0-9A-Fa-f]{2})")) { m ->
            m.groupValues[1].toInt(16).toChar().toString()
        }

    private fun isPlainEmail(s: String): Boolean =
        EMAIL_REGEX.matches(s)

    private fun isUrl(s: String): Boolean {
        if (s.contains(' ') || s.contains('\n')) return false
        if (URL_SCHEME_REGEX.containsMatchIn(s)) return true
        // Bare domain like example.com/path
        return DOMAIN_REGEX.matches(s.substringBefore('/'))
    }

    private fun normalizeUrl(s: String): String =
        if (URL_SCHEME_REGEX.containsMatchIn(s)) s else "https://$s"

    private companion object {
        val CRYPTO_SCHEMES = listOf(
            "bitcoin", "ethereum", "litecoin", "bitcoincash",
            "dogecoin", "monero", "ripple",
        )
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        val URL_SCHEME_REGEX = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://")
        val DOMAIN_REGEX = Regex("^([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$")
    }
}
