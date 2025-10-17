package com.wifiscanner.app

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualWiFiEntryScreen(
    onBack: () -> Unit,
    onConnect: (WiFiData, Boolean) -> Unit
) {
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var securityType by remember { mutableStateOf("WPA/WPA2") }
    var isHidden by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var permanentConnection by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }

    val securityOptions = listOf("WPA/WPA2", "WEP", "Open Network")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⌨️ Manual WiFi Entry") },
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
                text = "🔑",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enter WiFi Details",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Network Name
            OutlinedTextField(
                value = ssid,
                onValueChange = { ssid = it },
                label = { Text("Network Name (SSID)") },
                placeholder = { Text("MyWiFiNetwork") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Security Type Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = securityType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Security Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    securityOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                securityType = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password (if not open network)
            if (securityType != "Open Network") {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("••••••••") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(
                                text = if (passwordVisible) "👁️" else "🙈",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Hidden Network Checkbox
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                            text = "👻 Hidden Network",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Network doesn't broadcast SSID",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Checkbox(
                        checked = isHidden,
                        onCheckedChange = { isHidden = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Permanent Connection Checkbox
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
                            text = "💾 Save Permanently",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = if (permanentConnection) "Network will be saved" else "Temporary connection",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Checkbox(
                        checked = permanentConnection,
                        onCheckedChange = { permanentConnection = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Connect Button
            Button(
                onClick = {
                    if (ssid.isNotBlank()) {
                        val wifiData = WiFiData(
                            ssid = ssid,
                            password = if (securityType == "Open Network") "" else password,
                            encryptionType = securityType,
                            isHidden = isHidden
                        )
                        onConnect(wifiData, permanentConnection)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = ssid.isNotBlank() && (securityType == "Open Network" || password.isNotBlank())
            ) {
                Text("🚀 Connect to Network")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "💡 Tip: Make sure you have the correct network name and password",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
