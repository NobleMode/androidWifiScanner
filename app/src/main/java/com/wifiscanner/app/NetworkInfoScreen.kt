package com.wifiscanner.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.net.NetworkInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkInfoScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var networkInfo: NetworkInfoData? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        val info = withContext(Dispatchers.IO) {
            getNetworkInfo(context)
        }
        networkInfo = info
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 Network Info") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            networkInfo?.let { info ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (info.isConnected) {
                        // WiFi Name
                        InfoCard(
                            title = "📶 Network Name",
                            value = info.ssid,
                            subtitle = "SSID"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Connection Details
                        InfoCard(
                            title = "🔒 Security",
                            value = info.security,
                            subtitle = "Encryption Type"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // IP Address
                        InfoCard(
                            title = "🌐 IP Address",
                            value = info.ipAddress,
                            subtitle = "Local Network Address"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // MAC Address
                        InfoCard(
                            title = "🔖 MAC Address",
                            value = info.macAddress,
                            subtitle = "Hardware Address"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Signal Strength
                        InfoCard(
                            title = "📡 Signal Strength",
                            value = "${info.signalStrength} dBm (${info.signalLevel}/4)",
                            subtitle = "RSSI Level"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Frequency
                        if (info.frequency > 0) {
                            val band = if (info.frequency > 5000) "5 GHz" else "2.4 GHz"
                            InfoCard(
                                title = "📻 Frequency",
                                value = "${info.frequency} MHz",
                                subtitle = "$band Band"
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Link Speed
                        if (info.linkSpeed > 0) {
                            InfoCard(
                                title = "⚡ Link Speed",
                                value = "${info.linkSpeed} Mbps",
                                subtitle = "Connection Speed"
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Gateway
                        if (info.gateway.isNotEmpty()) {
                            InfoCard(
                                title = "🚪 Gateway",
                                value = info.gateway,
                                subtitle = "Router IP Address"
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // DNS
                        if (info.dns.isNotEmpty()) {
                            InfoCard(
                                title = "🗂️ DNS Server",
                                value = info.dns,
                                subtitle = "Domain Name System"
                            )
                        }

                    } else {
                        // Not connected
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = "❌ Not Connected",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Connect to a WiFi network to see network information",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    value: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

data class NetworkInfoData(
    val isConnected: Boolean = false,
    val ssid: String = "Not connected",
    val security: String = "Unknown",
    val ipAddress: String = "0.0.0.0",
    val macAddress: String = "00:00:00:00:00:00",
    val signalStrength: Int = 0,
    val signalLevel: Int = 0,
    val frequency: Int = 0,
    val linkSpeed: Int = 0,
    val gateway: String = "",
    val dns: String = ""
)

@Suppress("DEPRECATION")
fun getNetworkInfo(context: Context): NetworkInfoData {
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Check if connected to WiFi
    val network = connectivityManager.activeNetwork ?: return NetworkInfoData()
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkInfoData()
    
    if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
        return NetworkInfoData()
    }

    val wifiInfo: WifiInfo = wifiManager.connectionInfo

    // SSID
    val ssid = wifiInfo.ssid.replace("\"", "")

    // Security (approximate)
    val security = "WPA/WPA2" // Can't easily determine without scanning

    // IP Address
    val ipAddress = intToIp(wifiInfo.ipAddress)

    // MAC Address
    val macAddress = getMacAddress() ?: "02:00:00:00:00:00"

    // Signal Strength
    val rssi = wifiInfo.rssi
    val level = WifiManager.calculateSignalLevel(rssi, 5)

    // Frequency (API 21+)
    val frequency = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        wifiInfo.frequency
    } else {
        0
    }

    // Link Speed
    val linkSpeed = wifiInfo.linkSpeed

    // Gateway
    val dhcpInfo = wifiManager.dhcpInfo
    val gateway = intToIp(dhcpInfo.gateway)

    // DNS
    val dns = intToIp(dhcpInfo.dns1)

    return NetworkInfoData(
        isConnected = true,
        ssid = ssid,
        security = security,
        ipAddress = ipAddress,
        macAddress = macAddress,
        signalStrength = rssi,
        signalLevel = level,
        frequency = frequency,
        linkSpeed = linkSpeed,
        gateway = gateway,
        dns = dns
    )
}

fun intToIp(ip: Int): String {
    return String.format(
        "%d.%d.%d.%d",
        ip and 0xff,
        ip shr 8 and 0xff,
        ip shr 16 and 0xff,
        ip shr 24 and 0xff
    )
}

fun getMacAddress(): String? {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            if (networkInterface.name.equals("wlan0", ignoreCase = true)) {
                val mac = networkInterface.hardwareAddress ?: return null
                val buf = StringBuilder()
                for (byte in mac) {
                    buf.append(String.format("%02X:", byte))
                }
                if (buf.isNotEmpty()) {
                    buf.deleteCharAt(buf.length - 1)
                }
                return buf.toString()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}
