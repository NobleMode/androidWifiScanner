package com.wifiscanner.app

import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiFiHistoryScreen(
    onBack: () -> Unit,
    onConnectToNetwork: (WiFiData) -> Unit
) {
    val context = LocalContext.current
    val historyManager = remember { WiFiHistoryManager(context) }
    var savedNetworks by remember { mutableStateOf(historyManager.getSavedNetworks()) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📜 WiFi History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (savedNetworks.isNotEmpty()) {
                        IconButton(onClick = {
                            historyManager.clearAll()
                            savedNetworks = emptyList()
                        }) {
                            Text("🗑️", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (savedNetworks.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedNetworks, key = { it.ssid + it.timestamp }) { network ->
                    WiFiHistoryCard(
                        network = network,
                        onConnect = {
                            onConnectToNetwork(WiFiData(
                                ssid = network.ssid,
                                password = network.password,
                                encryptionType = network.encryptionType,
                                isHidden = network.isHidden
                            ))
                        },
                        onToggleFavorite = {
                            historyManager.toggleFavorite(network.ssid)
                            savedNetworks = historyManager.getSavedNetworks()
                        },
                        onDelete = {
                            showDeleteDialog = network.ssid
                        }
                    )
                }
            }
        }
        
        // Delete confirmation dialog
        showDeleteDialog?.let { ssid ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Network?") },
                text = { Text("Remove '$ssid' from history?") },
                confirmButton = {
                    TextButton(onClick = {
                        historyManager.deleteNetwork(ssid)
                        savedNetworks = historyManager.getSavedNetworks()
                        showDeleteDialog = null
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
                        Text(
                            text = "👻",
                            style = MaterialTheme.typography.bodySmall
                        )
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
                        Text(
                            text = "🗑️",
                            style = MaterialTheme.typography.titleLarge
                        )
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
