package com.nextqr.scanner.domain.model

/**
 * Result of the multi-layer URL safety check performed before any link from a
 * scanned code is opened. The user always sees the full URL and this verdict
 * first — links are never auto-opened.
 */
data class UrlSafety(
    val url: String,
    val verdict: UrlSafetyVerdict,
    /** Human-readable heuristic warnings (shortened link, IP host, punycode…). */
    val warnings: List<UrlWarning>,
    /** Threat categories reported by the remote reputation service, if any. */
    val remoteThreats: List<String> = emptyList(),
)

enum class UrlSafetyVerdict {
    /** No signal of danger from heuristics or reputation lookup. */
    SAFE,

    /** Heuristics flagged suspicious patterns; proceed with caution. */
    SUSPICIOUS,

    /** Reputation service classified the URL as malware/phishing/unwanted. */
    DANGEROUS,

    /** Could not reach the reputation service; only heuristics were applied. */
    UNKNOWN,
}

enum class UrlWarning {
    SHORTENED_LINK,
    IP_ADDRESS_HOST,
    PUNYCODE_HOST,
    UNUSUAL_TLD,
    NON_HTTPS,
    EMBEDDED_CREDENTIALS,
    EXCESSIVE_SUBDOMAINS,
    LOOKALIKE_CHARACTERS,
    QRLJACKING_LOGIN_REDIRECT,
}
