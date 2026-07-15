package com.nextqr.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nextqr.scanner.data.local.entity.ScanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {

    /**
     * Filtered, reactive history query. Null filters are ignored via the
     * `(:param IS NULL OR …)` idiom so a single query covers every combination
     * of search text / type / favourites.
     */
    @Query(
        """
        SELECT * FROM scans
        WHERE (:query IS NULL OR raw_value LIKE '%' || :query || '%')
          AND (:contentType IS NULL OR content_type = :contentType)
          AND (:favoritesOnly = 0 OR is_favorite = 1)
        ORDER BY timestamp DESC
        """,
    )
    fun observe(
        query: String?,
        contentType: String?,
        favoritesOnly: Boolean,
    ): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE id = :id")
    suspend fun getById(id: Long): ScanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ScanEntity): Long

    @Query("UPDATE scans SET is_favorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("DELETE FROM scans WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM scans")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM scans")
    fun observeCount(): Flow<Int>
}
