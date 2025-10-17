package com.wifiscanner.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

/**
 * Connection tester to verify internet connectivity
 */
class ConnectionTester(private val context: Context) {
    
    /**
     * Test if we have working internet connection
     * Returns ConnectionStatus with details
     */
    suspend fun testConnection(): ConnectionStatus = withContext(Dispatchers.IO) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        
        if (activeNetwork == null) {
            return@withContext ConnectionStatus(
                isConnected = false,
                hasInternet = false,
                message = "❌ No network connection"
            )
        }
        
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        if (capabilities == null) {
            return@withContext ConnectionStatus(
                isConnected = false,
                hasInternet = false,
                message = "❌ Cannot check network capabilities"
            )
        }
        
        val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        if (!hasWifi) {
            return@withContext ConnectionStatus(
                isConnected = true,
                hasInternet = false,
                message = "⚠️ Connected but not via WiFi"
            )
        }
        
        // Test actual internet connectivity
        val hasInternet = pingTest()
        
        return@withContext if (hasInternet) {
            ConnectionStatus(
                isConnected = true,
                hasInternet = true,
                message = "✅ Connected & Internet Working!",
                pingMs = getPingTime()
            )
        } else {
            ConnectionStatus(
                isConnected = true,
                hasInternet = false,
                message = "⚠️ Connected but no internet"
            )
        }
    }
    
    /**
     * Quick ping test to Google DNS
     */
    private suspend fun pingTest(): Boolean = withContext(Dispatchers.IO) {
        try {
            val address = InetAddress.getByName("8.8.8.8")
            return@withContext address.isReachable(3000) // 3 second timeout
        } catch (e: Exception) {
            // Try HTTP fallback
            return@withContext httpTest()
        }
    }
    
    /**
     * HTTP connectivity test as fallback
     */
    private suspend fun httpTest(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.google.com")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.requestMethod = "HEAD"
            connection.connect()
            val responseCode = connection.responseCode
            connection.disconnect()
            return@withContext responseCode == 200 || responseCode == 204 || responseCode == 301 || responseCode == 302
        } catch (e: Exception) {
            return@withContext false
        }
    }
    
    /**
     * Measure ping time in milliseconds
     */
    private suspend fun getPingTime(): Long = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val address = InetAddress.getByName("8.8.8.8")
            address.isReachable(3000)
            val endTime = System.currentTimeMillis()
            return@withContext endTime - startTime
        } catch (e: Exception) {
            return@withContext -1
        }
    }
}

/**
 * Connection status result
 */
data class ConnectionStatus(
    val isConnected: Boolean,
    val hasInternet: Boolean,
    val message: String,
    val pingMs: Long = -1
)
