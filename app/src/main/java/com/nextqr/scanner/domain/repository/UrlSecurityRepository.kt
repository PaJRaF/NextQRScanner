package com.nextqr.scanner.domain.repository

import com.nextqr.scanner.domain.model.UrlSafety

/** Verifies the reputation/safety of a URL before it is opened. */
interface UrlSecurityRepository {
    /**
     * Runs local heuristics and (if enabled and reachable) a remote reputation
     * lookup. Never throws for network problems — returns an UNKNOWN verdict
     * with whatever heuristic warnings were found.
     */
    suspend fun checkUrl(url: String): UrlSafety
}
