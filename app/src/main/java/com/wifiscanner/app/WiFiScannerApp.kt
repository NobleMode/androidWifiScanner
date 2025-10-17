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
fun WiFiScannerApp() {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    
    var scannedWiFiData by remember { mutableStateOf<WiFiData?>(null) }
    var showScanner by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    when {
        scannedWiFiData != null -> {
            WiFiDetailsScreen(
                wifiData = scannedWiFiData!!,
                onConnect = { data ->
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
                cameraPermissionGranted = cameraPermissionState.status.isGranted,
                locationPermissionGranted = locationPermissionState.status.isGranted
            )
        }
    }
}

@Composable
fun MainScreen(
    onScanClick: () -> Unit,
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
