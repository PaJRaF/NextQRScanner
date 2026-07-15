package com.nextqr.scanner.data.remote

import com.nextqr.scanner.data.remote.dto.ThreatMatchesRequest
import com.nextqr.scanner.data.remote.dto.ThreatMatchesResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface SafeBrowsingApi {
    /**
     * The API key is passed as a query param per Google's spec. Requests are
     * only ever made over HTTPS (enforced by the Network Security Config).
     */
    @POST("v4/threatMatches:find")
    suspend fun findThreatMatches(
        @Query("key") apiKey: String,
        @Body request: ThreatMatchesRequest,
    ): ThreatMatchesResponse
}
