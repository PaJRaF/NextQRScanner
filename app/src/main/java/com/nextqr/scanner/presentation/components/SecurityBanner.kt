package com.nextqr.scanner.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nextqr.scanner.R
import com.nextqr.scanner.domain.model.UrlSafety
import com.nextqr.scanner.domain.model.UrlSafetyVerdict
import com.nextqr.scanner.domain.model.UrlWarning
import com.nextqr.scanner.presentation.theme.DangerRed
import com.nextqr.scanner.presentation.theme.SafeGreen
import com.nextqr.scanner.presentation.theme.WarnAmber

/**
 * Renders the URL safety verdict and any heuristic warnings. Purely
 * informational — it does not open anything, matching the "no auto-open" rule.
 */
@Composable
fun SecurityBanner(safety: UrlSafety, modifier: Modifier = Modifier) {
    val (icon, color, titleRes) = when (safety.verdict) {
        UrlSafetyVerdict.SAFE ->
            Triple(Icons.Filled.CheckCircle, SafeGreen, R.string.security_safe)
        UrlSafetyVerdict.SUSPICIOUS ->
            Triple(Icons.Filled.Warning, WarnAmber, R.string.security_suspicious)
        UrlSafetyVerdict.DANGEROUS ->
            Triple(Icons.Filled.Dangerous, DangerRed, R.string.security_dangerous)
        UrlSafetyVerdict.UNKNOWN ->
            Triple(Icons.Filled.HelpOutline, WarnAmber, R.string.security_unknown)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
    ) {
        Column(Modifier.padding(12.dp)) {
            ListItem(
                headlineContent = { Text(stringResource(titleRes)) },
                leadingContent = { Icon(icon, contentDescription = null, tint = color) },
                colors = androidx.compose.material3.ListItemDefaults.colors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                ),
            )
            safety.warnings.forEach { warning ->
                Text(
                    text = "• " + stringResource(warning.labelRes()),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
        }
    }
}

private fun UrlWarning.labelRes(): Int = when (this) {
    UrlWarning.SHORTENED_LINK -> R.string.warn_shortened
    UrlWarning.IP_ADDRESS_HOST -> R.string.warn_ip_host
    UrlWarning.PUNYCODE_HOST -> R.string.warn_punycode
    UrlWarning.UNUSUAL_TLD -> R.string.warn_unusual_tld
    UrlWarning.NON_HTTPS -> R.string.warn_non_https
    UrlWarning.EMBEDDED_CREDENTIALS -> R.string.warn_credentials
    UrlWarning.EXCESSIVE_SUBDOMAINS -> R.string.warn_subdomains
    UrlWarning.LOOKALIKE_CHARACTERS -> R.string.warn_lookalike
    UrlWarning.QRLJACKING_LOGIN_REDIRECT -> R.string.warn_qrljacking
}
