package com.nextqr.scanner.presentation.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.nextqr.scanner.R
import com.nextqr.scanner.domain.model.ParsedContent
import com.nextqr.scanner.domain.model.UrlSafetyVerdict
import com.nextqr.scanner.presentation.components.SecurityBanner
import com.nextqr.scanner.util.ContentActions

/**
 * Bottom sheet shown after a scan. For URLs it always renders the full address
 * and the [SecurityBanner] verdict; the link is never opened automatically — the
 * user must tap "open" and, for dangerous verdicts, confirm again.
 */
@Composable
fun ScanResultSheet(
    result: ScanUiResult,
    onOpenDetail: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = contentTypeLabel(result.parsed),
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = result.rawValue,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )

        result.urlSafety?.let { safety ->
            SecurityBanner(safety = safety)
        }

        val isDangerous = result.urlSafety?.verdict == UrlSafetyVerdict.DANGEROUS

        Button(
            onClick = { ContentActions.perform(context, result.parsed, allowDangerous = false) },
            enabled = !isDangerous,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(smartActionLabel(result.parsed)))
        }

        if (isDangerous) {
            OutlinedButton(
                onClick = { ContentActions.perform(context, result.parsed, allowDangerous = true) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_open_anyway))
            }
        }

        OutlinedButton(onClick = onOpenDetail, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_view_details))
        }
    }
}

private fun contentTypeLabel(parsed: ParsedContent): String = when (parsed) {
    is ParsedContent.Url -> "URL"
    is ParsedContent.Wifi -> "Wi-Fi"
    is ParsedContent.Contact -> "Contact"
    is ParsedContent.Email -> "Email"
    is ParsedContent.Sms -> "SMS"
    is ParsedContent.Phone -> "Phone"
    is ParsedContent.CalendarEvent -> "Event"
    is ParsedContent.Geo -> "Location"
    is ParsedContent.Crypto -> "Crypto"
    is ParsedContent.Payment -> "Payment"
    is ParsedContent.PlainText -> "Text"
}

private fun smartActionLabel(parsed: ParsedContent): Int = when (parsed) {
    is ParsedContent.Url -> R.string.action_open_link
    is ParsedContent.Wifi -> R.string.action_connect_wifi
    is ParsedContent.Contact -> R.string.action_add_contact
    is ParsedContent.Email -> R.string.action_send_email
    is ParsedContent.Sms -> R.string.action_send_sms
    is ParsedContent.Phone -> R.string.action_call
    is ParsedContent.CalendarEvent -> R.string.action_add_event
    is ParsedContent.Geo -> R.string.action_open_map
    is ParsedContent.Crypto -> R.string.action_open_wallet
    is ParsedContent.Payment -> R.string.action_open_payment
    is ParsedContent.PlainText -> R.string.action_copy
}
