package com.wifiscanner.app

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WiFiScannerApp(themeManager: ThemeManager) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    
    var scannedWiFiData by remember { mutableStateOf<WiFiData?>(null) }
    var showScanner by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showGenerateQR by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val historyManager = remember { WiFiHistoryManager(context) }

    when {
        showHistory -> {
            WiFiHistoryScreen(
                onBack = { showHistory = false },
                onConnectToNetwork = { wifiData ->
                    connectToWiFi(context, wifiData)
                    showHistory = false
                }
            )
        }
        showGenerateQR -> {
            GenerateQRScreen(
                onBack = { showGenerateQR = false }
            )
        }
        scannedWiFiData != null -> {
            WiFiDetailsScreen(
                wifiData = scannedWiFiData!!,
                onConnect = { data ->
                    historyManager.saveNetwork(data)
                    connectToWiFi(context, data)
                    scannedWiFiData = null
                    showScanner = false
                },
                onCancel = {
                    scannedWiFiData = null
                    showScanner = false
                }
            )
        }
        showScanner && cameraPermissionState.status.isGranted -> {
            QRScannerScreen(
                onQRCodeScanned = { qrContent ->
                    parseWiFiQR(qrContent)?.let { wifiData ->
                        scannedWiFiData = wifiData
                    }
                },
                onClose = {
                    showScanner = false
                }
            )
        }
        else -> {
            MainScreen(
                onScanClick = {
                    if (cameraPermissionState.status.isGranted && locationPermissionState.status.isGranted) {
                        showScanner = true
                    } else {
                        if (!cameraPermissionState.status.isGranted) {
                            cameraPermissionState.launchPermissionRequest()
                        }
                        if (!locationPermissionState.status.isGranted) {
                            locationPermissionState.launchPermissionRequest()
                        }
                    }
                },
                onHistoryClick = { showHistory = true },
                onGenerateQRClick = { showGenerateQR = true },
                onToggleTheme = { themeManager.toggleTheme() },
                isDarkMode = themeManager.isDarkMode.value,
                cameraPermissionGranted = cameraPermissionState.status.isGranted,
                locationPermissionGranted = locationPermissionState.status.isGranted
            )
        }
    }
}

@Composable
fun MainScreen(
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onGenerateQRClick: () -> Unit,
    onToggleTheme: () -> Unit,
    isDarkMode: Boolean,
    cameraPermissionGranted: Boolean,
    locationPermissionGranted: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Theme toggle at top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onToggleTheme) {
                Text(
                    text = if (isDarkMode) "☀️" else "🌙",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        
        Text(
            text = "📱 WiFi QR Scanner",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Scan. Connect. Done. 💨",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onScanClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("📸 Scan That QR!")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onHistoryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("📜 View History")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onGenerateQRClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("🎨 Generate QR Code")
        }
        
        if (!cameraPermissionGranted || !locationPermissionGranted) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "⚠️ Psst... I need camera & location permissions to work my magic!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
