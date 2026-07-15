package com.nextqr.scanner.di

import com.nextqr.scanner.data.billing.BillingRepositoryImpl
import com.nextqr.scanner.data.repository.ScanHistoryRepositoryImpl
import com.nextqr.scanner.data.repository.SettingsRepositoryImpl
import com.nextqr.scanner.data.repository.UrlSecurityRepositoryImpl
import com.nextqr.scanner.domain.repository.BillingRepository
import com.nextqr.scanner.domain.repository.ScanHistoryRepository
import com.nextqr.scanner.domain.repository.SettingsRepository
import com.nextqr.scanner.domain.repository.UrlSecurityRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindScanHistoryRepository(
        impl: ScanHistoryRepositoryImpl,
    ): ScanHistoryRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl,
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindUrlSecurityRepository(
        impl: UrlSecurityRepositoryImpl,
    ): UrlSecurityRepository

    @Binds
    @Singleton
    abstract fun bindBillingRepository(
        impl: BillingRepositoryImpl,
    ): BillingRepository
}
