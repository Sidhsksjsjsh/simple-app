package com.nexus.ai.utils

import android.net.wifi.ScanResult

enum class WifiSecurityType { OPEN, WEP, WPA, WPA2, WPA3, WPA3_SAE, UNKNOWN }

enum class SignalQuality { EXCELLENT, GOOD, FAIR, POOR, VERY_POOR }

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val security: WifiSecurityType,
    val signalQuality: SignalQuality,
    val isOpen: Boolean,
    val isWeak: Boolean,
    val isLikelyCaptivePortal: Boolean = false
) {
    /**
     * Alias untuk `rssi` supaya kompatibel dengan kode yang pakai `level`.
     * (Beberapa bagian kode memakai `net.level`, beberapa pakai `net.rssi`.)
     */
    val level: Int get() = rssi

    /** Band: 2.4 GHz atau 5 GHz, bergantung pada frekuensi. */
    val band: String
        get() = when {
            frequency in 2400..2500 -> "2.4 GHz"
            frequency in 4900..5900 -> "5 GHz"
            frequency in 5925..7125 -> "6 GHz"
            else -> "Unknown"
        }

    /** Label security untuk ditampilkan. */
    val securityLabel: String
        get() = when (security) {
            WifiSecurityType.OPEN -> "🔓 Terbuka"
            WifiSecurityType.WEP -> "🔒 WEP"
            WifiSecurityType.WPA -> "🔒 WPA"
            WifiSecurityType.WPA2 -> "🔒 WPA2"
            WifiSecurityType.WPA3 -> "🔒 WPA3"
            WifiSecurityType.WPA3_SAE -> "🔒 WPA3-SAE"
            WifiSecurityType.UNKNOWN -> "❓ Unknown"
        }

    /** Deskripsi kualitas sinyal untuk ditampilkan. */
    val qualityLabel: String
        get() = when (signalQuality) {
            SignalQuality.EXCELLENT -> "Excellent"
            SignalQuality.GOOD -> "Good"
            SignalQuality.FAIR -> "Fair"
            SignalQuality.POOR -> "Poor"
            SignalQuality.VERY_POOR -> "Very Poor"
        }

    companion object {
        fun fromScanResult(result: ScanResult): WifiNetwork {
            val security = WifiHelper.getSecurityType(result)
            val quality = WifiHelper.getSignalQuality(result.level)
            return WifiNetwork(
                ssid = result.SSID.ifBlank { "(hidden)" },
                bssid = result.BSSID ?: "",
                rssi = result.level,
                frequency = result.frequency,
                security = security,
                signalQuality = quality,
                isOpen = security == WifiSecurityType.OPEN,
                isWeak = result.level < -75
            )
        }
    }
}