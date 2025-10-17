package com.wifiscanner.app

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
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
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateQRScreen(
    onBack: () -> Unit
) {
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var securityType by remember { mutableStateOf("WPA") }
    var isHidden by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var expanded by remember { mutableStateOf(false) }
    
    val securityOptions = listOf("WPA", "WEP", "nopass")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎨 Generate WiFi QR") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                    if (ssid.isNotEmpty()) {
                        qrBitmap = generateWiFiQRCode(ssid, password, securityType, isHidden)
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
