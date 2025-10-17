package com.wifiscanner.app

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.WifiNetworkSuggestion
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Build
import android.widget.Toast

fun connectToWiFi(context: Context, wifiData: WiFiData, isPermanent: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android 10+ - Use WifiNetworkSpecifier (temporary connection)
        // Note: Android 10+ doesn't allow apps to add permanent WiFi networks via API
        connectToWiFiModern(context, wifiData, isPermanent)
    } else {
        // Android 9 and below - Use WifiConfiguration (can be permanent)
        connectToWiFiLegacy(context, wifiData, isPermanent)
    }
}

@androidx.annotation.RequiresApi(Build.VERSION_CODES.Q)
private fun connectToWiFiModern(context: Context, wifiData: WiFiData, isPermanent: Boolean) {
    // Android 10+ uses WifiNetworkSuggestion API
    // This suggests a network to the system, and the user can approve it
    
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    
    val suggestionBuilder = WifiNetworkSuggestion.Builder()
        .setSsid(wifiData.ssid)
    
    when (wifiData.encryptionType) {
        "WPA/WPA2", "WPA" -> suggestionBuilder.setWpa2Passphrase(wifiData.password)
        "WEP" -> suggestionBuilder.setWpa2Passphrase(wifiData.password) // WEP deprecated, try WPA2
        "Open Network", "nopass" -> {} // No password needed
    }
    
    if (wifiData.isHidden) {
        suggestionBuilder.setIsHiddenSsid(true)
    }
    
    // Set priority (higher priority networks are preferred)
    suggestionBuilder.setPriority(1)
    
    val suggestion = suggestionBuilder.build()
    val suggestionsList = listOf(suggestion)
    
    val status = wifiManager.addNetworkSuggestions(suggestionsList)
    
    when (status) {
        WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS -> {
            Toast.makeText(
                context,
                "✅ Network suggested to system!\n" +
                "Swipe down notification panel to connect.\n" +
                "System will ask for approval.",
                Toast.LENGTH_LONG
            ).show()
        }
        WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE -> {
            Toast.makeText(
                context,
                "Network already suggested. Check WiFi settings to connect.",
                Toast.LENGTH_LONG
            ).show()
        }
        WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_APP_DISALLOWED -> {
            Toast.makeText(
                context,
                "❌ App not allowed to suggest networks.\n" +
                "Please add network manually in Settings.",
                Toast.LENGTH_LONG
            ).show()
        }
        else -> {
            Toast.makeText(
                context,
                "⚠️ Could not suggest network.\n" +
                "Please add manually:\nSettings → WiFi → + Add Network\n" +
                "SSID: ${wifiData.ssid}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

@Suppress("DEPRECATION")
private fun connectToWiFiLegacy(context: Context, wifiData: WiFiData, isPermanent: Boolean) {
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    
    val wifiConfig = WifiConfiguration().apply {
        SSID = "\"${wifiData.ssid}\""
        
        when (wifiData.encryptionType) {
            "WPA/WPA2" -> {
                preSharedKey = "\"${wifiData.password}\""
                allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            }
            "WEP" -> {
                wepKeys[0] = "\"${wifiData.password}\""
                wepTxKeyIndex = 0
                allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
            }
            "Open Network" -> {
                allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            }
        }
        
        hiddenSSID = wifiData.isHidden
    }
    
    val networkId = wifiManager.addNetwork(wifiConfig)
    if (networkId != -1) {
        wifiManager.disconnect()
        wifiManager.enableNetwork(networkId, true)
        wifiManager.reconnect()
        
        val connectionType = if (isPermanent) "permanently saved" else "temporary connection"
        Toast.makeText(context, "⏳ Connecting to ${wifiData.ssid} ($connectionType)...", Toast.LENGTH_SHORT).show()
        
        // Note: On Android 9 and below, the network is always saved to the system
        // There's no built-in way to make it truly temporary
        if (!isPermanent) {
            Toast.makeText(
                context,
                "ℹ️ Network saved. Remove manually from WiFi settings if needed.",
                Toast.LENGTH_LONG
            ).show()
        }
    } else {
        Toast.makeText(context, "😓 Hmm, couldn't add that network. Try again?", Toast.LENGTH_SHORT).show()
    }
}
