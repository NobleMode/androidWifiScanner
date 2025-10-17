package com.wifiscanner.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.wifiscanner.app.ui.theme.WiFiQRScannerTheme

class MainActivity : ComponentActivity() {
    private lateinit var themeManager: ThemeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeManager = ThemeManager(this)
        
        setContent {
            val isDarkMode = themeManager.isDarkMode.value
            
            WiFiQRScannerTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WiFiScannerApp(
                        themeManager = themeManager
                    )
                }
            }
        }
    }
}
