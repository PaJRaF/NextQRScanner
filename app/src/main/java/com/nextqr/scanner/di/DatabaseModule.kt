package com.nextqr.scanner.di

import android.content.Context
import androidx.room.Room
import com.nextqr.scanner.data.local.ScanDatabase
import com.nextqr.scanner.data.local.dao.ScanDao
import com.nextqr.scanner.data.security.DatabaseKeyProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideScanDatabase(
        @ApplicationContext context: Context,
        keyProvider: DatabaseKeyProvider,
    ): ScanDatabase {
        // SQLCipher native library loader; safe to call multiple times.
        System.loadLibrary("sqlcipher")
        val factory = SupportOpenHelperFactory(keyProvider.getOrCreatePassphrase())
        @Suppress("DEPRECATION")
        return Room.databaseBuilder(context, ScanDatabase::class.java, ScanDatabase.NAME)
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideScanDao(database: ScanDatabase): ScanDao = database.scanDao()
}
