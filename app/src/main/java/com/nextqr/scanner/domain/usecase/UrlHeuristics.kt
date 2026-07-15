package com.nextqr.scanner.domain.usecase

import com.nextqr.scanner.domain.model.UrlWarning
import javax.inject.Inject

/**
 * Local, offline heuristics that flag suspicious URL patterns without any
 * network call. Used both as a first line of defence and as the sole signal
 * when the remote reputation service is unreachable or disabled.
 *
 * Pure Kotlin → fully unit-testable.
 */
class UrlHeuristics @Inject constructor() {

    fun analyze(url: String): List<UrlWarning> {
        val warnings = mutableListOf<UrlWarning>()
        val lower = url.lowercase()

        if (!lower.startsWith("https://")) warnings += UrlWarning.NON_HTTPS

        val host = extractHost(url) ?: return warnings

        if (url.substringAfter("://", "").substringBefore('/').contains('@')) {
            warnings += UrlWarning.EMBEDDED_CREDENTIALS
        }
        if (IP_HOST_REGEX.matches(host)) warnings += UrlWarning.IP_ADDRESS_HOST
        if (host.contains("xn--")) warnings += UrlWarning.PUNYCODE_HOST

        val labels = host.split('.')
        if (labels.size > 4) warnings += UrlWarning.EXCESSIVE_SUBDOMAINS

        val tld = labels.lastOrNull()?.lowercase()
        if (tld != null && tld in SUSPICIOUS_TLDS) warnings += UrlWarning.UNUSUAL_TLD

        val registrable = labels.takeLast(2).firstOrNull()?.lowercase()
        if (registrable != null && registrable in SHORTENER_DOMAINS) {
            warnings += UrlWarning.SHORTENED_LINK
        }

        if (containsLookalikeChars(host)) warnings += UrlWarning.LOOKALIKE_CHARACTERS

        // Heuristic for QRLjacking: a redirect param pointing at a login page.
        if (LOGIN_REDIRECT_REGEX.containsMatchIn(lower)) {
            warnings += UrlWarning.QRLJACKING_LOGIN_REDIRECT
        }

        return warnings
    }

    private fun extractHost(url: String): String? {
        val afterScheme = url.substringAfter("://", url)
        val authority = afterScheme.substringBefore('/').substringBefore('?')
        val hostPart = authority.substringAfterLast('@').substringBefore(':')
        return hostPart.ifBlank { null }
    }

    /** Mixed-script hosts (e.g. Latin + Cyrillic) are a classic spoofing sign. */
    private fun containsLookalikeChars(host: String): Boolean {
        val hasLatin = host.any { it in 'a'..'z' || it in 'A'..'Z' }
        val hasCyrillic = host.any { it in 'Ѐ'..'ӿ' }
        val hasGreek = host.any { it in 'Ͱ'..'Ͽ' }
        return hasLatin && (hasCyrillic || hasGreek)
    }

    private companion object {
        val IP_HOST_REGEX = Regex("^(\\d{1,3}\\.){3}\\d{1,3}$")
        val LOGIN_REDIRECT_REGEX =
            Regex("(redirect|return|next|url|continue)=[^&]*(login|signin|auth|account)")
        val SHORTENER_DOMAINS = setOf(
            "bit", "tinyurl", "t", "goo", "ow", "buff", "is", "cutt",
            "rebrand", "shorturl", "rb", "trib", "lnkd",
        )
        val SUSPICIOUS_TLDS = setOf(
            "zip", "mov", "xyz", "top", "click", "country", "gq", "cf",
            "tk", "ml", "ga", "work", "loan", "review", "kim", "men",
        )
    }
}
