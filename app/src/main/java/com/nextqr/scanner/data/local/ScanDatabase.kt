package com.nextqr.scanner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nextqr.scanner.data.local.dao.ScanDao
import com.nextqr.scanner.data.local.entity.ScanEntity

@Database(
    entities = [ScanEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class ScanDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao

    companion object {
        const val NAME = "nextqr-scans.db"
    }
}
