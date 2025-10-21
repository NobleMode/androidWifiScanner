package com.wifiscanner.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.wifi.WifiManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateQRScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val historyManager = remember(context) { WiFiHistoryManager(context) }
    var savedNetworks by remember { mutableStateOf<List<SavedWiFiNetwork>>(emptyList()) }
    var currentWiFi by remember { mutableStateOf<CurrentWiFiInfo?>(null) }
    val haptic = rememberHapticFeedback()
    val sounds = rememberSoundEffects()
    
    // Load data asynchronously
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val networks = historyManager.getSavedNetworks()
            val wifi = getCurrentConnectedWiFi(context)
            withContext(Dispatchers.Main) {
                savedNetworks = networks
                currentWiFi = wifi
            }
        }
    }
    
    var inputMode by remember { mutableStateOf("manual") } // "manual", "current", "saved"
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var securityType by remember { mutableStateOf("WPA") }
    var isHidden by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var savedNetworkExpanded by remember { mutableStateOf(false) }
    var selectedSavedNetwork by remember { mutableStateOf<SavedWiFiNetwork?>(null) }
    
    val securityOptions = listOf("WPA", "WEP", "nopass")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎨 Generate WiFi QR") },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.tap()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Share Your WiFi! 📤",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Input Mode Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Network Source:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Current Network Button
                    currentWiFi?.let { wifi ->
                        ElevatedButton(
                            onClick = {
                                haptic.tap()
                                sounds.playClick()
                                inputMode = "current"
                                ssid = wifi.ssid
                                password = "" // Can't retrieve password
                                securityType = "WPA"
                                isHidden = false
                                qrBitmap = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = if (inputMode == "current") 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text("📶 Current Network: ${wifi.ssid}")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Saved Networks Dropdown
                    if (savedNetworks.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = savedNetworkExpanded,
                            onExpandedChange = { 
                                haptic.tap()
                                savedNetworkExpanded = it 
                            }
                        ) {
                            OutlinedTextField(
                                value = if (selectedSavedNetwork != null) 
                                    "💾 ${selectedSavedNetwork!!.ssid}" 
                                else 
                                    "💾 Select Saved Network...",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Saved Networks") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = savedNetworkExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = if (inputMode == "saved") 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = if (inputMode == "saved") 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = savedNetworkExpanded,
                                onDismissRequest = { savedNetworkExpanded = false }
                            ) {
                                savedNetworks.forEach { network ->
                                    DropdownMenuItem(
                                        text = { 
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(network.ssid)
                                                if (network.isFavorite) Text("⭐")
                                            }
                                        },
                                        onClick = {
                                            haptic.tap()
                                            sounds.playClick()
                                            inputMode = "saved"
                                            selectedSavedNetwork = network
                                            ssid = network.ssid
                                            password = network.password
                                            securityType = network.encryptionType
                                            isHidden = network.isHidden
                                            savedNetworkExpanded = false
                                            qrBitmap = null
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Manual Entry Button
                    ElevatedButton(
                        onClick = {
                            haptic.tap()
                            sounds.playClick()
                            inputMode = "manual"
                            ssid = ""
                            password = ""
                            securityType = "WPA"
                            isHidden = false
                            selectedSavedNetwork = null
                            qrBitmap = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = if (inputMode == "manual") 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text("⌨️ Manual Entry")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Show warning for current network (no password)
            if (inputMode == "current") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "⚠️ Note: Android doesn't allow apps to read WiFi passwords. Please enter the password manually below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Network Details Section
            Text(
                text = "Network Details:",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // SSID Input
            OutlinedTextField(
                value = ssid,
                onValueChange = { ssid = it },
                label = { Text("Network Name (SSID)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Security Type Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = when(securityType) {
                        "WPA" -> "🔒 WPA/WPA2"
                        "WEP" -> "🔓 WEP"
                        "nopass" -> "🌐 Open Network"
                        else -> securityType
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Security Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    securityOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { 
                                Text(when(option) {
                                    "WPA" -> "🔒 WPA/WPA2"
                                    "WEP" -> "🔓 WEP"
                                    "nopass" -> "🌐 Open Network"
                                    else -> option
                                })
                            },
                            onClick = {
                                securityType = option
                                expanded = false
                                if (option == "nopass") {
                                    password = ""
                                }
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password Input (only if not open network)
            if (securityType != "nopass") {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Hidden Network Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isHidden,
                    onCheckedChange = { isHidden = it }
                )
                Text("👻 Hidden Network")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Generate Button
            Button(
                onClick = {
                    haptic.success()
                    sounds.playSuccess()
                    if (ssid.isNotEmpty()) {
                        scope.launch {
                            val bitmap = withContext(Dispatchers.Default) {
                                generateWiFiQRCode(ssid, password, securityType, isHidden)
                            }
                            qrBitmap = bitmap
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = ssid.isNotEmpty() && (securityType == "nopass" || password.isNotEmpty())
            ) {
                Text("✨ Generate QR Code")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Display QR Code
            qrBitmap?.let { bitmap ->
                Card(
                    modifier = Modifier
                        .size(300.dp)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "WiFi QR Code",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "📸 Others can scan this to connect!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun generateWiFiQRCode(
    ssid: String,
    password: String,
    security: String,
    hidden: Boolean,
    size: Int = 512
): Bitmap? {
    try {
        // WiFi QR format: WIFI:T:WPA;S:MyNetwork;P:MyPassword;H:false;;
        val wifiString = buildString {
            append("WIFI:")
            append("T:$security;")
            append("S:${escapeSpecialCharacters(ssid)};")
            if (password.isNotEmpty()) {
                append("P:${escapeSpecialCharacters(password)};")
            }
            append("H:$hidden;;")
        }
        
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            wifiString,
            BarcodeFormat.QR_CODE,
            size,
            size
        )
        
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        
        return bitmap
    } catch (e: WriterException) {
        e.printStackTrace()
        return null
    }
}

fun escapeSpecialCharacters(input: String): String {
    return input.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace(":", "\\:")
}

/**
 * Get currently connected WiFi network info
 * Returns null if not connected or can't access info
 * Note: Android doesn't allow apps to read WiFi passwords
 */
fun getCurrentConnectedWiFi(context: Context): CurrentWiFiInfo? {
    try {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            ?: return null
        
        val wifiInfo = wifiManager.connectionInfo ?: return null
        val ssid = wifiInfo.ssid?.removeSurrounding("\"") ?: return null
        
        // Return null if not connected or unknown SSID
        if (ssid == "<unknown ssid>" || ssid.isBlank()) {
            return null
        }
        
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
