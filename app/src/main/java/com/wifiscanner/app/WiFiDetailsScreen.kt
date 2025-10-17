package com.wifiscanner.app

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WiFiDetailsScreen(
    wifiData: WiFiData,
    onConnect: (WiFiData) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📶",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.size(72.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Found Your WiFi! 🎯",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                DetailRow("Network Name (SSID)", wifiData.ssid)
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        DetailRow("Security Type", wifiData.encryptionType)
                    }
                    if (wifiData.encryptionType != "nopass") {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secured",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                if (wifiData.password.isNotEmpty()) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    DetailRow("Password", wifiData.password)
                }
                
                if (wifiData.isHidden) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    DetailRow("Hidden Network", "Yes")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { onConnect(wifiData) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("🚀 Let's Connect!")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("❌ Nah, Maybe Later")
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
