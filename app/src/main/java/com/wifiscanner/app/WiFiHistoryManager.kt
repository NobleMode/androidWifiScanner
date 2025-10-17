package com.wifiscanner.app

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class SavedWiFiNetwork(
    val ssid: String,
    val password: String,
    val encryptionType: String,
    val isHidden: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

class WiFiHistoryManager(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("wifi_history", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    fun saveNetwork(wifiData: WiFiData) {
        val networks = getSavedNetworks().toMutableList()
        
        // Remove if already exists (update it)
        networks.removeAll { it.ssid == wifiData.ssid }
        
        // Add at the beginning
        networks.add(0, SavedWiFiNetwork(
            ssid = wifiData.ssid,
            password = wifiData.password,
            encryptionType = wifiData.encryptionType,
            isHidden = wifiData.isHidden,
            timestamp = System.currentTimeMillis()
        ))
        
        // Keep only last 50 networks
        if (networks.size > 50) {
            networks.subList(50, networks.size).clear()
        }
        
        val json = gson.toJson(networks)
        prefs.edit().putString("networks", json).apply()
    }
    
    fun getSavedNetworks(): List<SavedWiFiNetwork> {
        val json = prefs.getString("networks", null) ?: return emptyList()
        val type = object : TypeToken<List<SavedWiFiNetwork>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun toggleFavorite(ssid: String) {
        val networks = getSavedNetworks().toMutableList()
        val index = networks.indexOfFirst { it.ssid == ssid }
        if (index != -1) {
            networks[index] = networks[index].copy(isFavorite = !networks[index].isFavorite)
            val json = gson.toJson(networks)
            prefs.edit().putString("networks", json).apply()
        }
    }
    
    fun deleteNetwork(ssid: String) {
        val networks = getSavedNetworks().toMutableList()
        networks.removeAll { it.ssid == ssid }
        val json = gson.toJson(networks)
        prefs.edit().putString("networks", json).apply()
    }
    
    fun clearAll() {
        prefs.edit().remove("networks").apply()
    }
}
