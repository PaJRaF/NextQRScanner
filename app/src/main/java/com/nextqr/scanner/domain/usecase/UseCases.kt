package com.nextqr.scanner.domain.usecase

import com.nextqr.scanner.domain.model.BarcodeType
import com.nextqr.scanner.domain.model.ParsedContent
import com.nextqr.scanner.domain.model.ScanResult
import com.nextqr.scanner.domain.model.UrlSafety
import com.nextqr.scanner.domain.repository.ScanHistoryRepository
import com.nextqr.scanner.domain.repository.SettingsRepository
import com.nextqr.scanner.domain.repository.UrlSecurityRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Classifies a raw decoded string into structured content. */
class ClassifyContentUseCase @Inject constructor(
    private val parser: BarcodeContentParser,
) {
    operator fun invoke(raw: String): ParsedContent = parser.parse(raw)
}

/**
 * Verifies a URL's safety, honouring the user's "check URL safety" preference.
 * When disabled, only offline heuristics are applied (still never auto-opening).
 */
class VerifyUrlUseCase @Inject constructor(
    private val securityRepository: UrlSecurityRepository,
    private val heuristics: UrlHeuristics,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(url: String): UrlSafety {
        val checkRemote = settingsRepository.settings.first().checkUrlSafety
        return if (checkRemote) {
            securityRepository.checkUrl(url)
        } else {
            UrlSafety(
                url = url,
                verdict = com.nextqr.scanner.domain.model.UrlSafetyVerdict.UNKNOWN,
                warnings = heuristics.analyze(url),
            )
        }
    }
}

/**
 * Persists a scan into history if history is enabled. Classifies the content to
 * store the correct semantic type. Returns the saved id, or null if history is
 * disabled by the user.
 */
class SaveScanUseCase @Inject constructor(
    private val historyRepository: ScanHistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val classify: ClassifyContentUseCase,
) {
    suspend operator fun invoke(
        rawValue: String,
        barcodeType: BarcodeType,
        isGenerated: Boolean = false,
    ): Long? {
        if (!settingsRepository.settings.first().historyEnabled) return null
        val parsed = classify(rawValue)
        return historyRepository.save(
            ScanResult(
                rawValue = rawValue,
                barcodeType = barcodeType,
                contentType = parsed.type,
                timestamp = System.currentTimeMillis(),
                isGenerated = isGenerated,
            ),
        )
    }
}
