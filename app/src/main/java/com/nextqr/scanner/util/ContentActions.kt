package com.nextqr.scanner.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.widget.Toast
import androidx.core.net.toUri
import com.nextqr.scanner.domain.model.ParsedContent

/**
 * Executes the "smart action" for a parsed payload, always as an explicit user
 * gesture. Links are launched via [Intent.ACTION_VIEW] only after the user has
 * seen the safety verdict; [allowDangerous] must be true to launch a URL the
 * security layer flagged as dangerous.
 *
 * Wi-Fi/contact/calendar actions are delegated to the system settings/apps so
 * the app itself never holds contacts/location permissions.
 */
object ContentActions {

    fun perform(context: Context, content: ParsedContent, allowDangerous: Boolean) {
        when (content) {
            is ParsedContent.Url -> openUrl(context, content.url, allowDangerous)
            is ParsedContent.PlainText -> copyToClipboard(context, content.rawValue)
            is ParsedContent.Email -> startIntent(context, emailIntent(content))
            is ParsedContent.Sms -> startIntent(context, smsIntent(content))
            is ParsedContent.Phone ->
                startIntent(context, Intent(Intent.ACTION_DIAL, "tel:${content.number}".toUri()))
            is ParsedContent.Geo -> openUrl(context, content.rawValue, allowDangerous = true)
            is ParsedContent.Contact -> startIntent(context, contactIntent(content))
            is ParsedContent.CalendarEvent -> startIntent(context, eventIntent(content))
            is ParsedContent.Wifi -> openWifiSettings(context)
            is ParsedContent.Crypto -> openUrl(context, content.rawValue, allowDangerous = true)
            is ParsedContent.Payment -> copyToClipboard(context, content.iban ?: content.rawValue)
        }
    }

    private fun openUrl(context: Context, url: String, allowDangerous: Boolean) {
        if (!allowDangerous && !url.startsWith("https://") && !url.startsWith("http://")) {
            // Only http(s) is auto-launchable from the safe path.
            return
        }
        startIntent(context, Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    private fun emailIntent(content: ParsedContent.Email) =
        Intent(Intent.ACTION_SENDTO, "mailto:${content.address}".toUri()).apply {
            content.subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
            content.body?.let { putExtra(Intent.EXTRA_TEXT, it) }
        }

    private fun smsIntent(content: ParsedContent.Sms) =
        Intent(Intent.ACTION_SENDTO, "smsto:${content.number}".toUri()).apply {
            content.message?.let { putExtra("sms_body", it) }
        }

    private fun contactIntent(content: ParsedContent.Contact) =
        Intent(Intent.ACTION_INSERT).apply {
            type = ContactsContract.Contacts.CONTENT_TYPE
            content.name?.let { putExtra(ContactsContract.Intents.Insert.NAME, it) }
            content.phones.firstOrNull()?.let { putExtra(ContactsContract.Intents.Insert.PHONE, it) }
            content.emails.firstOrNull()?.let { putExtra(ContactsContract.Intents.Insert.EMAIL, it) }
            content.organization?.let { putExtra(ContactsContract.Intents.Insert.COMPANY, it) }
        }

    private fun eventIntent(content: ParsedContent.CalendarEvent) =
        Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            content.title?.let { putExtra(CalendarContract.Events.TITLE, it) }
            content.location?.let { putExtra(CalendarContract.Events.EVENT_LOCATION, it) }
            content.description?.let { putExtra(CalendarContract.Events.DESCRIPTION, it) }
        }

    private fun openWifiSettings(context: Context) {
        // On modern Android, programmatic connect needs a system panel; we hand
        // the user to Wi-Fi settings rather than requesting location permission.
        startIntent(context, Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
    }

    private fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("NextQR", text))
        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }

    private fun startIntent(context: Context, intent: Intent) {
        try {
            context.startActivity(intent.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No app can handle this", Toast.LENGTH_SHORT).show()
        }
    }
}
