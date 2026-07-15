package com.nextqr.scanner.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request/response DTOs for the Google Safe Browsing v4 `threatMatches:find`
 * endpoint. See https://developers.google.com/safe-browsing/v4.
 */
@Serializable
data class ThreatMatchesRequest(
    val client: ClientInfo,
    val threatInfo: ThreatInfo,
)

@Serializable
data class ClientInfo(
    val clientId: String,
    val clientVersion: String,
)

@Serializable
data class ThreatInfo(
    val threatTypes: List<String>,
    val platformTypes: List<String>,
    val threatEntryTypes: List<String>,
    val threatEntries: List<ThreatEntry>,
)

@Serializable
data class ThreatEntry(val url: String)

@Serializable
data class ThreatMatchesResponse(
    val matches: List<ThreatMatch>? = null,
)

@Serializable
data class ThreatMatch(
    @SerialName("threatType") val threatType: String,
    @SerialName("platformType") val platformType: String? = null,
)
