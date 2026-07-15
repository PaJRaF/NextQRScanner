package com.nextqr.scanner.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nextqr.scanner.MainActivity
import com.nextqr.scanner.R

/**
 * Home-screen widget that deep-links straight into the scanner. Tapping it
 * launches [MainActivity] with the quick-scan action.
 */
class QuickScanWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { id ->
            val intent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_QUICK_SCAN
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val views = RemoteViews(context.packageName, R.layout.widget_quick_scan).apply {
                setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            }
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    companion object {
        const val ACTION_QUICK_SCAN = "com.nextqr.scanner.ACTION_QUICK_SCAN"

        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, QuickScanWidgetProvider::class.java),
            )
            QuickScanWidgetProvider().onUpdate(context, manager, ids)
        }
    }
}
