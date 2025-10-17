package com.wifiscanner.app

data class WiFiData(
    val ssid: String,
    val password: String,
    val encryptionType: String,
    val isHidden: Boolean = false
)

fun parseWiFiQR(qrContent: String): WiFiData? {
    // WiFi QR format: WIFI:T:WPA;S:MyNetwork;P:MyPassword;H:false;;
    if (!qrContent.startsWith("WIFI:")) return null
    
    val parts = qrContent.substring(5).split(";")
    var ssid = ""
    var password = ""
    var encryptionType = ""
    var isHidden = false
    
    for (part in parts) {
        when {
            part.startsWith("T:") -> encryptionType = part.substring(2)
            part.startsWith("S:") -> ssid = part.substring(2)
            part.startsWith("P:") -> password = part.substring(2)
            part.startsWith("H:") -> isHidden = part.substring(2).equals("true", ignoreCase = true)
        }
    }
    
    return if (ssid.isNotEmpty()) {
        WiFiData(
            ssid = ssid,
            password = password,
            encryptionType = when (encryptionType.uppercase()) {
                "WPA" -> "WPA/WPA2"
                "WEP" -> "WEP"
                "nopass" -> "Open Network"
                else -> encryptionType.ifEmpty { "Unknown" }
            },
            isHidden = isHidden
        )
    } else {
        null
    }
}
