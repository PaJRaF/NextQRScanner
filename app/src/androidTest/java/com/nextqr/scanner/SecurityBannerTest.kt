package com.nextqr.scanner

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.nextqr.scanner.domain.model.UrlSafety
import com.nextqr.scanner.domain.model.UrlSafetyVerdict
import com.nextqr.scanner.domain.model.UrlWarning
import com.nextqr.scanner.presentation.components.SecurityBanner
import com.nextqr.scanner.presentation.theme.NextQrTheme
import org.junit.Rule
import org.junit.Test

class SecurityBannerTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dangerousVerdict_showsBlockedMessageAndWarnings() {
        composeRule.setContent {
            NextQrTheme {
                SecurityBanner(
                    safety = UrlSafety(
                        url = "http://bit.ly/evil",
                        verdict = UrlSafetyVerdict.DANGEROUS,
                        warnings = listOf(UrlWarning.SHORTENED_LINK, UrlWarning.NON_HTTPS),
                    ),
                )
            }
        }

        composeRule.onNodeWithText("Dangerous link blocked").assertIsDisplayed()
    }
}
