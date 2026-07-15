package com.nextqr.scanner.data.repository

import android.util.Log
import com.nextqr.scanner.BuildConfig
import com.nextqr.scanner.data.remote.SafeBrowsingApi
import com.nextqr.scanner.data.remote.dto.ClientInfo
import com.nextqr.scanner.data.remote.dto.ThreatEntry
import com.nextqr.scanner.data.remote.dto.ThreatInfo
import com.nextqr.scanner.data.remote.dto.ThreatMatchesRequest
import com.nextqr.scanner.domain.model.UrlSafety
import com.nextqr.scanner.domain.model.UrlSafetyVerdict
import com.nextqr.scanner.domain.repository.UrlSecurityRepository
import com.nextqr.scanner.domain.usecase.UrlHeuristics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Two-layer URL verification:
 *  1. Offline heuristics ([UrlHeuristics]) — always run, never fail.
 *  2. Google Safe Browsing reputation lookup — best-effort; network/quota
 *     failures degrade gracefully to a heuristic-only verdict.
 *
 * The final verdict is the strongest signal from either layer. A URL is never
 * opened automatically regardless of verdict — the UI always shows this result
 * and the full address first.
 */
@Singleton
class UrlSecurityRepositoryImpl @Inject constructor(
    private val api: SafeBrowsingApi,
    private val heuristics: UrlHeuristics,
) : UrlSecurityRepository {

    override suspend fun checkUrl(url: String): UrlSafety {
        val warnings = heuristics.analyze(url)
        val heuristicVerdict =
            if (warnings.isEmpty()) UrlSafetyVerdict.SAFE else UrlSafetyVerdict.SUSPICIOUS

        val apiKey = BuildConfig.SAFE_BROWSING_API_KEY
        if (apiKey.isBlank()) {
            // No key configured → heuristics only.
            return UrlSafety(url, heuristicVerdict, warnings)
        }

        val remoteThreats = queryRemote(url)
            ?: // Network/parse failure → heuristics, but mark as UNKNOWN so the
                // UI can communicate that the reputation check didn't complete.
                return UrlSafety(
                    url = url,
                    verdict = if (warnings.isEmpty()) UrlSafetyVerdict.UNKNOWN else heuristicVerdict,
                    warnings = warnings,
                )

        val verdict = when {
            remoteThreats.isNotEmpty() -> UrlSafetyVerdict.DANGEROUS
            else -> heuristicVerdict
        }
        return UrlSafety(url, verdict, warnings, remoteThreats)
    }

    /** Returns the matched threat-type list, or null on any failure. */
    private suspend fun queryRemote(url: String): List<String>? = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.findThreatMatches(
                apiKey = BuildConfig.SAFE_BROWSING_API_KEY,
                request = ThreatMatchesRequest(
                    client = ClientInfo("com.nextqr.scanner", BuildConfig.VERSION_NAME),
                    threatInfo = ThreatInfo(
                        threatTypes = THREAT_TYPES,
                        platformTypes = listOf("ANY_PLATFORM"),
                        threatEntryTypes = listOf("URL"),
                        threatEntries = listOf(ThreatEntry(url)),
                    ),
                ),
            )
            response.matches.orEmpty().map { it.threatType }
        }.getOrElse {
            Log.w(TAG, "Safe Browsing lookup failed: ${it.javaClass.simpleName}")
            null
        }
    }

    private companion object {
        const val TAG = "UrlSecurity"
        val THREAT_TYPES = listOf(
            "MALWARE",
            "SOCIAL_ENGINEERING",
            "UNWANTED_SOFTWARE",
            "POTENTIALLY_HARMFUL_APPLICATION",
        )
    }
}
