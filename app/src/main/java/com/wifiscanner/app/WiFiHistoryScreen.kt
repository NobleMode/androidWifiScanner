package com.wifiscanner.app

import android.content.Context
import android.net.wifi.WifiManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiFiHistoryScreen(
    onBack: () -> Unit,
    onConnectToNetwork: (WiFiData) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val historyManager = remember(context) { WiFiHistoryManager(context) }
    var savedNetworks by remember { mutableStateOf<List<SavedWiFiNetwork>>(emptyList()) }
    var currentWiFi by remember { mutableStateOf<CurrentWiFiInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    val haptic = rememberHapticFeedback()
    val sounds = rememberSoundEffects()
    
    // Load data asynchronously
    LaunchedEffect(Unit) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val networks = historyManager.getSavedNetworks()
            val wifi = getCurrentWiFiInfo(context)
            withContext(Dispatchers.Main) {
                savedNetworks = networks
                currentWiFi = wifi
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📜 WiFi History") },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.tap()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (savedNetworks.isNotEmpty()) {
                        IconButton(onClick = {
                            haptic.longPress()
                            scope.launch(Dispatchers.IO) {
                                historyManager.clearAll()
                                withContext(Dispatchers.Main) {
                                    savedNetworks = emptyList()
                                }
                            }
                        }) {
                            Text("🗑️", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            currentWiFi?.let { wifi ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📶 Currently Connected",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = wifi.ssid,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = getSignalStrengthIcon(wifi.signalLevel),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${wifi.signalStrength} dBm",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "✅ Active",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = wifi.frequency,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    thickness = 1.dp
                )
                
                Text(
                    text = "Previous Networks",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (savedNetworks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "🤷",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No WiFi history yet!",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Scan some QR codes to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedNetworks, key = { it.ssid + it.timestamp }) { network ->
                        WiFiHistoryCard(
                            network = network,
                            onConnect = {
                                haptic.tap()
                                sounds.playClick()
                                onConnectToNetwork(WiFiData(
                                    ssid = network.ssid,
                                    password = network.password,
                                    encryptionType = network.encryptionType,
                                    isHidden = network.isHidden
                                ))
                            },
                            onToggleFavorite = {
                                haptic.tap()
                                scope.launch(Dispatchers.IO) {
                                    historyManager.toggleFavorite(network.ssid)
                                    val networks = historyManager.getSavedNetworks()
                                    withContext(Dispatchers.Main) {
                                        savedNetworks = networks
                                    }
                                }
                            },
                            onDelete = {
                                haptic.tap()
                                showDeleteDialog = network.ssid
                            }
                        )
                    }
                }
            }
        }
        
        showDeleteDialog?.let { ssid ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Network?") },
                text = { Text("Remove '$ssid' from history?") },
                confirmButton = {
                    TextButton(onClick = {
                        haptic.tap()
                        scope.launch(Dispatchers.IO) {
                            historyManager.deleteNetwork(ssid)
                            val networks = historyManager.getSavedNetworks()
                            withContext(Dispatchers.Main) {
                                savedNetworks = networks
                                showDeleteDialog = null
                            }
                        }
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        haptic.tap()
                        showDeleteDialog = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiFiHistoryCard(
    network: SavedWiFiNetwork,
    onConnect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (network.isFavorite) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = network.ssid,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (network.isHidden) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "👻", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                Row {
                    IconButton(onClick = onToggleFavorite) {
                        Text(
                            text = if (network.isFavorite) "⭐" else "☆",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (network.isFavorite) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Text(text = "🗑️", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "🔒 ${network.encryptionType}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "⏰ ${formatTimestamp(network.timestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onConnect,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🚀 Connect Again")
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

data class CurrentWiFiInfo(
    val ssid: String,
    val signalStrength: Int,
    val signalLevel: Int,
    val frequency: String
)

fun getCurrentWiFiInfo(context: Context): CurrentWiFiInfo? {
    try {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return null
        val wifiInfo = wifiManager.connectionInfo ?: return null
        val ssid = wifiInfo.ssid?.removeSurrounding("\"") ?: return null
        
        if (ssid == "<unknown ssid>" || ssid.isBlank()) return null
        
        val signalStrength = wifiInfo.rssi
        val signalLevel = WifiManager.calculateSignalLevel(signalStrength, 5)
        val frequency = wifiInfo.frequency
        val frequencyBand = when {
            frequency in 2400..2500 -> "2.4 GHz"
            frequency in 5000..5900 -> "5 GHz"
            else -> "${frequency} MHz"
        }
        
        return CurrentWiFiInfo(ssid, signalStrength, signalLevel, frequencyBand)
    } catch (e: Exception) {
        return null
    }
}

fun getSignalStrengthIcon(level: Int): String {
    return when (level) {
        4 -> ""
        3 -> ""
        2 -> ""
        1 -> ""
        else -> ""
    }
}
