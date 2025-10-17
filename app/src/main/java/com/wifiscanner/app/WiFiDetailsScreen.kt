package com.wifiscanner.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun WiFiDetailsScreen(
    wifiData: WiFiData,
    onConnect: (WiFiData, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var permanentConnection by remember { mutableStateOf(true) }
    var showConnectionTest by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf<ConnectionStatus?>(null) }
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()
    val sounds = rememberSoundEffects()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Found Your WiFi! ",
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

                    Column {
                        Text(
                            text = "Password",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (passwordVisible) wifiData.password else "",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(onClick = {
                                passwordVisible = !passwordVisible
                                haptic.tap()
                                sounds.playClick()
                            }) {
                                Text(
                                    text = if (passwordVisible) "👁️" else "🙈",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }

                            IconButton(onClick = {
                                copyToClipboard(context, wifiData.password)
                                haptic.success()
                                sounds.playSuccess()
                            }) {
                                Text(
                                    text = "📋",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                }

                if (wifiData.isHidden) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    DetailRow("Hidden Network", "Yes")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Share WiFi Details Button
        OutlinedButton(
            onClick = {
                shareWiFiDetails(context, wifiData)
                haptic.tap()
                sounds.playClick()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📤 Share WiFi Details")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Permanent connection checkbox
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "💾 Save Network Permanently",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = if (permanentConnection) "Network will be saved to device" else "Temporary connection only",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                Checkbox(
                    checked = permanentConnection,
                    onCheckedChange = {
                        permanentConnection = it
                        haptic.tap()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                haptic.success()
                sounds.playSuccess()
                onConnect(wifiData, permanentConnection)
                // Auto-test connection after connecting
                showConnectionTest = true
                scope.launch {
                    kotlinx.coroutines.delay(3000) // Wait 3 seconds for connection
                    val tester = ConnectionTester(context)
                    connectionStatus = tester.testConnection()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(" Let's Connect!")
        }

        // Connection test result
        if (showConnectionTest) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        connectionStatus == null -> MaterialTheme.colorScheme.surfaceVariant
                        connectionStatus?.hasInternet == true -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (connectionStatus == null) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("🧪 Testing connection...")
                    } else {
                        Text(
                            text = connectionStatus?.message ?: "",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (connectionStatus?.pingMs != null && connectionStatus?.pingMs!! > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${connectionStatus?.pingMs}ms)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                haptic.tap()
                onCancel()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(" Nah, Maybe Later")
        }
    }
}

fun shareWiFiDetails(context: Context, wifiData: WiFiData) {
    val shareText = buildString {
        appendLine("📶 WiFi Network Details")
        appendLine()
        appendLine("Network Name: ${wifiData.ssid}")
        appendLine("Password: ${wifiData.password}")
        appendLine("Security: ${wifiData.encryptionType}")
        if (wifiData.isHidden) {
            appendLine("Hidden Network: Yes")
        }
        appendLine()
        appendLine("Shared from WiFi QR Scanner")
    }
    
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    
    context.startActivity(Intent.createChooser(shareIntent, "Share WiFi Details"))
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("WiFi Password", text)
    clipboard.setPrimaryClip(clip)
    android.widget.Toast.makeText(context, " Password copied!", android.widget.Toast.LENGTH_SHORT).show()
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
