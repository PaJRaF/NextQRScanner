package com.nextqr.scanner.data.repository

import com.nextqr.scanner.data.local.dao.ScanDao
import com.nextqr.scanner.data.local.entity.ScanEntity
import com.nextqr.scanner.domain.model.BarcodeType
import com.nextqr.scanner.domain.model.ScanContentType
import com.nextqr.scanner.domain.model.ScanResult
import com.nextqr.scanner.domain.model.UrlSafetyVerdict
import com.nextqr.scanner.domain.repository.ScanHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanHistoryRepositoryImpl @Inject constructor(
    private val dao: ScanDao,
) : ScanHistoryRepository {

    override fun observeHistory(
        query: String?,
        typeFilter: ScanContentType?,
        favoritesOnly: Boolean,
    ): Flow<List<ScanResult>> =
        dao.observe(query?.ifBlank { null }, typeFilter?.name, favoritesOnly)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): ScanResult? = dao.getById(id)?.toDomain()

    override suspend fun save(result: ScanResult): Long = dao.insert(result.toEntity())

    override suspend fun setFavorite(id: Long, favorite: Boolean) =
        dao.setFavorite(id, favorite)

    override suspend fun delete(id: Long) = dao.delete(id)

    override suspend fun clearAll() = dao.clearAll()

    override fun observeCount(): Flow<Int> = dao.observeCount()

    // --- mappers ---

    private fun ScanEntity.toDomain() = ScanResult(
        id = id,
        rawValue = rawValue,
        barcodeType = barcodeType.toEnum(BarcodeType.UNKNOWN),
        contentType = contentType.toEnum(ScanContentType.TEXT),
        timestamp = timestamp,
        isFavorite = isFavorite,
        isGenerated = isGenerated,
        securityVerdict = securityVerdict?.toEnumOrNull<UrlSafetyVerdict>(),
    )

    private fun ScanResult.toEntity() = ScanEntity(
        id = id,
        rawValue = rawValue,
        barcodeType = barcodeType.name,
        contentType = contentType.name,
        timestamp = timestamp,
        isFavorite = isFavorite,
        isGenerated = isGenerated,
        securityVerdict = securityVerdict?.name,
    )

    private inline fun <reified T : Enum<T>> String.toEnum(fallback: T): T =
        runCatching { enumValueOf<T>(this) }.getOrDefault(fallback)

    private inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? =
        runCatching { enumValueOf<T>(this) }.getOrNull()
}
