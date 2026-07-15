package com.nextqr.scanner.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scans",
    indices = [
        Index("timestamp"),
        Index("content_type"),
        Index("is_favorite"),
    ],
)
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "raw_value") val rawValue: String,
    @ColumnInfo(name = "barcode_type") val barcodeType: String,
    @ColumnInfo(name = "content_type") val contentType: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "is_generated") val isGenerated: Boolean = false,
    @ColumnInfo(name = "security_verdict") val securityVerdict: String? = null,
)
