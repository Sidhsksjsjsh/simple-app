package com.nexus.ai.screens.tabs

import android.Manifest
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.ai.ui.components.*
import com.nexus.ai.ui.theme.*
import com.nexus.ai.utils.AutoConnectResult
import com.nexus.ai.utils.WifiHelper
import com.nexus.ai.utils.WifiNetwork
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun WifiTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember {
        context.getSharedPreferences("wifi_prefs", Context.MODE_PRIVATE)
    }
    val wifiHelper = remember { WifiHelper(context) }

    // ===== STATE =====
    var switchOn by remember { mutableStateOf(true) }
    var autoConnect by remember {
        mutableStateOf(prefs.getBoolean("auto_connect", false))
    }
    var statusText by remember { mutableStateOf("Siap") }
    val networks = remember { mutableStateListOf<WifiNetwork>() }
    val logs = remember { mutableStateListOf<String>() }
    var autoJob by remember { mutableStateOf<Job?>(null) }

    // ===== HELPERS =====
    fun log(msg: String) {
        logs.add(0, msg)
        if (logs.size > 20) logs.removeAt(logs.lastIndex)
    }

    fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun applyNetworks(list: List<WifiNetwork>) {
        networks.clear()
        networks.addAll(list)
        val openCount = list.count { it.isOpen }
        statusText = "${list.size} jaringan ($openCount terbuka)"
        log("📶 ${list.size} jaringan ($openCount terbuka)")
    }

    fun doScan() {
        statusText = "Memindai..."
        log("🔍 Memindai WiFi...")
        wifiHelper.startScan(
            onResult = { list -> applyNetworks(list) },
            onError = { err ->
                statusText = "Error: $err"
                log("❌ $err")
            }
        )
    }

    // ===== PERMISSION =====
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) {
            log("✅ Izin WiFi diberikan")
            doScan()
        } else {
            log("⚠️ Izin WiFi ditolak")
            toast("Izin WiFi diperlukan")
        }
    }

    fun requestPermsAndScan() {
        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.NEARBY_WIFI_DEVICES,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
        permissionLauncher.launch(perms)
    }

    // ===== AUTO MODE =====
    fun startAutoMode() {
        autoJob?.cancel()
        autoJob = scope.launch {
            while (isActive) {
                log("🔄 Auto scan...")
                val list = wifiHelper.scanSuspendingPublic()
                if (list.isNotEmpty()) {
                    applyNetworks(list)
                    val best = wifiHelper.pickBestOpenNetwork(list)
                    if (best != null) {
                        statusText = "Menghubungkan ke ${best.ssid}..."
                        log("🌐 Coba connect ke ${best.ssid}")
                        wifiHelper.suggestOpenNetwork(best.ssid)
                        delay(5000)
                        val captive = wifiHelper.isCaptivePortalAsync()
                        statusText = if (captive) "🌐 ${best.ssid} — butuh login web"
                        else "✅ Terhubung ke ${best.ssid}"
                        log(statusText)
                    }
                }
                delay(30_000L)
            }
        }
    }

    fun stopAutoMode() {
        autoJob?.cancel()
        autoJob = null
        statusText = "Auto-connect dimatikan"
        log("⏹️ Auto-connect dimatikan")
    }

    LaunchedEffect(Unit) {
        if (autoConnect) startAutoMode()
    }

    DisposableEffect(Unit) {
        onDispose {
            autoJob?.cancel()
            wifiHelper.stopScan()
        }
    }

    // ===== UI =====
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 120.dp)
    ) {
        // Status card
        GlassCard {
            SectionLabel("📶", "Status")
            Text(statusText, color = Color.White, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            Text("${networks.size} jaringan ditemukan", color = TextSoft, fontSize = 12.sp)
        }

        Spacer(Modifier.height(16.dp))

        // Scan controls
        GlassCard {
            SectionLabel("⚙️", "Scan & Auto Connect")

            PrimaryNeonButton("Scan WiFi") { requestPermsAndScan() }
            Spacer(Modifier.height(10.dp))

            PrimaryNeonButton("Auto connect sekali") {
                scope.launch {
                    val result = wifiHelper.scanAndAutoConnect { progress ->
                        statusText = progress
                        log(progress)
                    }
                    val msg = when (result) {
                        is AutoConnectResult.ConnectedInternetOk ->
                            "✅ Terhubung ke ${result.ssid}"
                        is AutoConnectResult.ConnectedCaptivePortal ->
                            "🌐 ${result.ssid} — butuh login web"
                        AutoConnectResult.NoOpenNetwork -> "⚠️ Tidak ada WiFi terbuka"
                        AutoConnectResult.NoNetworks -> "⚠️ Tidak ada jaringan"
                        AutoConnectResult.SuggestionFailed -> "❌ Gagal connect"
                    }
                    statusText = msg
                    log(msg)
                    toast(msg)
                }
            }
            Spacer(Modifier.height(10.dp))

            PrimaryNeonButton("Bersihkan saran") {
                wifiHelper.clearSuggestions()
                log("🧹 Saran jaringan dibersihkan")
                toast("Saran jaringan dibersihkan")
            }
            Spacer(Modifier.height(10.dp))

            PrimaryNeonButton("Tampilkan hasil scan") { doScan() }

            Spacer(Modifier.height(18.dp))

            // // ✅ Pakai overload 4-argumen — lebih eksplisit, tidak ambigu
            // NeonSwitch("Mulai spoof lokasi", "Perlu Developer Options", switchOn) { checked ->
            //     switchOn = it
            //     log(if (it) "🟢 Neural DIAKTIFKAN" else "🔴 Neural DIMATIKAN")
            // }
        }

        Spacer(Modifier.height(16.dp))

        // Auto connect toggle
        GlassCard {
            SectionLabel("🤖", "Connect Automation")
            NeonSwitch("Auto connect", "Perlu Developer Options", autoConnect) { checked ->
                autoConnect = checked
                prefs.edit().putBoolean("auto_connect", checked).apply()
                if (checked) startAutoMode() else stopAutoMode()
            }
        }

        Spacer(Modifier.height(16.dp))

        // Daftar jaringan
        if (networks.isNotEmpty()) {
            GlassCard {
                SectionLabel("📋", "Jaringan Ditemukan")
                networks.forEach { net ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(net.ssid, color = Color.White, fontSize = 14.sp)
                            Text(
                                if (net.isOpen) "Terbuka • ${net.level} dBm"
                                else "Terkunci • ${net.level} dBm",
                                color = TextSoft,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Log
        GlassCard {
            SectionLabel("📡", "Log")
            InfoParagraph("Bersihkan Log", logs) { logs.clear() }
        }
    }
}