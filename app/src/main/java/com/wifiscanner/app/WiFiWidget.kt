package com.wifiscanner.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.widget.RemoteViews

/**
 * WiFi Scanner Home Screen Widget
 * Shows current WiFi status and provides quick scan button
 */
class WiFiWidget : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update each widget instance
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Get current WiFi info
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val wifiInfo = wifiManager.connectionInfo
    val ssid = wifiInfo.ssid.removeSurrounding("\"")
    val signalStrength = WifiManager.calculateSignalLevel(wifiInfo.rssi, 5)
    
    val widgetText = if (ssid != "<unknown ssid>") {
        "📶 $ssid\n${"▰".repeat(signalStrength)}${"▱".repeat(5 - signalStrength)}"
    } else {
        "📵 Not Connected"
    }
    
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.wifi_widget)
    views.setTextViewText(R.id.widget_text, widgetText)
    
    // Create intent to launch app
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    } else {
        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    views.setOnClickPendingIntent(R.id.widget_scan_button, pendingIntent)
    
    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
