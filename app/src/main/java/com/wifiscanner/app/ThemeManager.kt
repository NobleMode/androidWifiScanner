package com.wifiscanner.app

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    private val _isDarkMode = mutableStateOf(prefs.getBoolean("dark_mode", false))
    val isDarkMode: State<Boolean> = _isDarkMode
    
    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
        prefs.edit().putBoolean("dark_mode", _isDarkMode.value).apply()
    }
    
    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }
}
