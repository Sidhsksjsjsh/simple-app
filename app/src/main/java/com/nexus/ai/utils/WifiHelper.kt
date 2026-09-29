package com.nexus.ai.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume

class WifiHelper(private val context: Context) {

    private val wifiManager: WifiManager = context.applicationContext
        .getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val connectivityManager: ConnectivityManager = context.applicationContext
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var scanReceiver: BroadcastReceiver? = null

    // ============================================================
    // BAGIAN 1: SCANNING — callback
    // ============================================================

    fun startScan(
        onResult: (List<WifiNetwork>) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        stopScan()

        scanReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val success = intent.getBooleanExtra(
                    WifiManager.EXTRA_RESULTS_UPDATED, false
                )
                if (success) {
                    onResult(parseScanResults(wifiManager.scanResults))
                } else {
                    val cached = wifiManager.scanResults
                    if (cached.isNotEmpty()) onResult(parseScanResults(cached))
                    else onError("Scan gagal dan tidak ada cache")
                }
            }
        }

        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                scanReceiver, filter, Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(scanReceiver, filter)
        }

        if (!wifiManager.startScan()) {
            onResult(parseScanResults(wifiManager.scanResults))
        }
    }

    fun stopScan() {
        scanReceiver?.let {
            runCatching { context.unregisterReceiver(it) }
            scanReceiver = null
        }
    }

    private fun parseScanResults(results: List<ScanResult>): List<WifiNetwork> {
        return results
            .filter { it.SSID.isNotBlank() || it.BSSID.isNotBlank() }
            .map { WifiNetwork.fromScanResult(it) }
            .distinctBy { it.bssid }
            .sortedByDescending { it.rssi }
    }

    // ============================================================
    // BAGIAN 2: SCANNING — suspend (dipakai dari coroutine)
    // ============================================================

    /**
     * Versi suspending dari startScan.
     * Cocok dipanggil dari dalam coroutine (auto mode, one-shot).
     */
    suspend fun scanSuspendingPublic(): List<WifiNetwork> =
        suspendCancellableCoroutine { cont ->
            startScan(
                onResult = { if (cont.isActive) cont.resume(it) },
                onError = { if (cont.isActive) cont.resume(emptyList()) }
            )
        }

    // ============================================================
    // BAGIAN 3: KLASIFIKASI (helper statis)
    // ============================================================

    companion object {
        fun getSecurityType(result: ScanResult): WifiSecurityType {
            val caps = result.capabilities.uppercase()
            return when {
                caps.contains("WPA3") && caps.contains("SAE") -> WifiSecurityType.WPA3_SAE
                caps.contains("WPA3") -> WifiSecurityType.WPA3
                caps.contains("WPA2") -> WifiSecurityType.WPA2
                caps.contains("WPA") -> WifiSecurityType.WPA
                caps.contains("WEP") -> WifiSecurityType.WEP
                caps.isBlank() || (!caps.contains("WPA") &&
                        !caps.contains("WEP") &&
                        !caps.contains("SAE") &&
                        !caps.contains("OWE")) -> WifiSecurityType.OPEN
                else -> WifiSecurityType.UNKNOWN
            }
        }

        fun getSignalQuality(rssi: Int): SignalQuality = when {
            rssi >= -50 -> SignalQuality.EXCELLENT
            rssi >= -60 -> SignalQuality.GOOD
            rssi >= -70 -> SignalQuality.FAIR
            rssi >= -80 -> SignalQuality.POOR
            else -> SignalQuality.VERY_POOR
        }
    }

    // ============================================================
    // BAGIAN 4: AUTO-CONNECT (suggestion API, Android 10+)
    // ============================================================

    fun suggestOpenNetwork(ssid: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        return try {
            val suggestion = WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .setIsAppInteractionRequired(false)
                .build()
            val status = wifiManager.addNetworkSuggestions(listOf(suggestion))
            status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS
        } catch (_: Exception) {
            false
        }
    }

    fun suggestWpa2Network(ssid: String, password: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        return try {
            val suggestion = WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .setIsAppInteractionRequired(true)
                .build()
            val status = wifiManager.addNetworkSuggestions(listOf(suggestion))
            status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS
        } catch (_: Exception) {
            false
        }
    }

    fun clearSuggestions(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        return try {
            wifiManager.removeNetworkSuggestions(emptyList()) ==
                    WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS
        } catch (_: Exception) {
            false
        }
    }

    fun pickBestOpenNetwork(networks: List<WifiNetwork>): WifiNetwork? {
        return networks
            .filter { it.isOpen }
            .filter { !it.isWeak }
            .filter { it.ssid != "(hidden)" }
            .maxByOrNull { it.rssi }
    }

    // ============================================================
    // BAGIAN 5: CAPTIVE PORTAL
    // ============================================================

    fun isCaptivePortal(): Boolean {
        return try {
            val url = URL("http://connectivitycheck.gstatic.com/generate_204")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                instanceFollowRedirects = false
            }
            val code = conn.responseCode
            conn.disconnect()
            code != 204
        } catch (_: Exception) {
            true
        }
    }

    suspend fun isCaptivePortalAsync(): Boolean = withContext(Dispatchers.IO) {
        isCaptivePortal()
    }

    // ============================================================
    // BAGIAN 6: WIFI INFO / CONTROL
    // ============================================================

    @Suppress("DEPRECATION")
    fun getCurrentWifiInfo() = runCatching { wifiManager.connectionInfo }.getOrNull()

    fun isWifiEnabled(): Boolean = wifiManager.isWifiEnabled

    fun setWifiEnabled(enabled: Boolean) {
        runCatching { wifiManager.isWifiEnabled = enabled }.getOrDefault(false)
    }

    // ============================================================
    // BAGIAN 7: ORKESTRASI AUTO-CONNECT
    // ============================================================

    suspend fun scanAndAutoConnect(
        onProgress: (String) -> Unit = {}
    ): AutoConnectResult = withContext(Dispatchers.Main) {

        if (!isWifiEnabled()) {
            onProgress("WiFi mati, menyalakan...")
            setWifiEnabled(true)
            delay(2000)
        }

        onProgress("Memindai jaringan...")
        val networks = scanSuspendingPublic()

        if (networks.isEmpty()) return@withContext AutoConnectResult.NoNetworks

        onProgress("Mencari WiFi terbuka terbaik...")
        val best = pickBestOpenNetwork(networks)
            ?: return@withContext AutoConnectResult.NoOpenNetwork

        onProgress("Menyarankan \"${best.ssid}\" ke sistem...")
        if (!suggestOpenNetwork(best.ssid)) {
            return@withContext AutoConnectResult.SuggestionFailed
        }

        onProgress("Menunggu koneksi...")
        delay(5000)

        onProgress("Memeriksa koneksi internet...")
        val captive = isCaptivePortalAsync()

        return@withContext if (captive) {
            AutoConnectResult.ConnectedCaptivePortal(best.ssid)
        } else {
            AutoConnectResult.ConnectedInternetOk(best.ssid)
        }
    }
}