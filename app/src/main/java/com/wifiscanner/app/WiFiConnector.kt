package com.wifiscanner.app

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Build
import android.widget.Toast

fun connectToWiFi(context: Context, wifiData: WiFiData) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android 10+ - Use WifiNetworkSpecifier
        connectToWiFiModern(context, wifiData)
    } else {
        // Android 9 and below - Use WifiConfiguration
        connectToWiFiLegacy(context, wifiData)
    }
}

@androidx.annotation.RequiresApi(Build.VERSION_CODES.Q)
private fun connectToWiFiModern(context: Context, wifiData: WiFiData) {
    val specifierBuilder = WifiNetworkSpecifier.Builder()
        .setSsid(wifiData.ssid)
    
    when (wifiData.encryptionType) {
        "WPA/WPA2" -> specifierBuilder.setWpa2Passphrase(wifiData.password)
        "WEP" -> specifierBuilder.setWpa2Passphrase(wifiData.password) // WEP not directly supported
        "Open Network" -> {} // No password needed
    }
    
    if (wifiData.isHidden) {
        specifierBuilder.setIsHiddenSsid(true)
    }
    
    val networkRequest = NetworkRequest.Builder()
        .addTransportType(android.net.NetworkCapabilities.TRANSPORT_WIFI)
        .setNetworkSpecifier(specifierBuilder.build())
        .build()
    
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: android.net.Network) {
            super.onAvailable(network)
            Toast.makeText(context, "🎉 Boom! Connected to ${wifiData.ssid}", Toast.LENGTH_SHORT).show()
        }
        
        override fun onUnavailable() {
            super.onUnavailable()
            Toast.makeText(context, "😅 Oops! Couldn't connect to ${wifiData.ssid}", Toast.LENGTH_SHORT).show()
        }
    }
    
    connectivityManager.requestNetwork(networkRequest, callback)
    Toast.makeText(context, "⏳ Connecting to ${wifiData.ssid}...", Toast.LENGTH_SHORT).show()
}

@Suppress("DEPRECATION")
private fun connectToWiFiLegacy(context: Context, wifiData: WiFiData) {
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
        Toast.makeText(context, "⏳ Connecting to ${wifiData.ssid}...", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "😓 Hmm, couldn't add that network. Try again?", Toast.LENGTH_SHORT).show()
    }
}
