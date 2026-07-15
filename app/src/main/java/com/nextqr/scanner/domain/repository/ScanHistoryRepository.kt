package com.nextqr.scanner.domain.repository

import com.nextqr.scanner.domain.model.ScanContentType
import com.nextqr.scanner.domain.model.ScanResult
import kotlinx.coroutines.flow.Flow

/** Persistence of scan/generation history in the encrypted local database. */
interface ScanHistoryRepository {

    fun observeHistory(
        query: String? = null,
        typeFilter: ScanContentType? = null,
        favoritesOnly: Boolean = false,
    ): Flow<List<ScanResult>>

    suspend fun getById(id: Long): ScanResult?

    /** Inserts a scan, returning the assigned id. */
    suspend fun save(result: ScanResult): Long

    suspend fun setFavorite(id: Long, favorite: Boolean)

    suspend fun delete(id: Long)

    /** Wipes all history — used by the GDPR "delete my data" action. */
    suspend fun clearAll()

    /** Number of stored entries; used to enforce the free-tier history cap. */
    fun observeCount(): Flow<Int>
}
