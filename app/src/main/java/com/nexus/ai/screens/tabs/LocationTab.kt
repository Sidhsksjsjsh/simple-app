package com.nexus.ai.screens.tabs

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.nexus.ai.R
import com.nexus.ai.ui.components.*
import com.nexus.ai.ui.theme.*
import com.nexus.ai.utils.MockedLocationService
import com.nexus.ai.utils.NotificationHelper
import com.nexus.ai.utils.WebAppInterface
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PREFS = "location_prefs"
private const val KEY_LAT = "last_lat"
private const val KEY_LNG = "last_lng"
private const val KEY_ZOOM = "last_zoom"

@Composable
fun LocationTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    // ===== STATE — koordinat & zoom =====
    var latitude by remember {
        mutableStateOf(prefs.getFloat(KEY_LAT, -6.200000f).toDouble())
    }
    var longitude by remember {
        mutableStateOf(prefs.getFloat(KEY_LNG, 106.816666f).toDouble())
    }
    var currentZoom by remember {
        mutableStateOf(prefs.getFloat(KEY_ZOOM, 13f).toDouble())
    }

    // ===== STATE — spoofer =====
    var switchOn by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf("Passive") }
    var showDialogError by remember { mutableStateOf(false) }
    var errorMessage by remember {
        mutableStateOf("Developer Options belum di-set.")
    }
    val logs = remember { mutableStateListOf<String>() }

    // ===== STATE — service binder =====
    var binder by remember { mutableStateOf<MockedLocationService.MockedBinder?>(null) }
    var serviceReady by remember { mutableStateOf(false) }

    // ===== STATE — peta =====
    var selectedMap by remember { mutableStateOf(0) } // 0 = OSM, 1 = Google
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var pageLoaded by remember { mutableStateOf(false) }
    var lastPickedFrom by remember { mutableStateOf<String?>(null) }

    val modes = listOf("Passive", "Auto", "On Trigger")
    val maps = listOf("OpenStreetMap", "Google Maps")

    // ===== HELPERS =====
    fun log(msg: String) {
        logs.add(0, msg)
        if (logs.size > 15) logs.removeAt(logs.lastIndex)
    }

    fun saveCoords(lat: Double, lng: Double, source: String) {
        latitude = lat
        longitude = lng
        prefs.edit()
            .putFloat(KEY_LAT, lat.toFloat())
            .putFloat(KEY_LNG, lng.toFloat())
            .apply()
        lastPickedFrom = source

        // Kalau spoofing sedang aktif, push koordinat baru langsung
        if (switchOn) {
            binder?.startMock(
                lng, lat,
                0.0, 0.0,
                1000L, 0, 0f
            )
        }
    }

    fun saveZoom(z: Double) {
        currentZoom = z
        prefs.edit().putFloat(KEY_ZOOM, z.toFloat()).apply()
    }

    // ===== SERVICE BINDING =====
    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                binder = service as? MockedLocationService.MockedBinder
                serviceReady = binder != null
                log("🔌 Service terhubung")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                binder = null
                serviceReady = false
                log("🔌 Service terputus")
            }
        }
    }

    DisposableEffect(Unit) {
        val intent = Intent(context, MockedLocationService::class.java)
        try {
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            log("🔌 Binding ke MockedLocationService...")
        } catch (e: Exception) {
            log("❌ Gagal bind: ${e.message}")
        }

        onDispose {
            runCatching { context.unbindService(connection) }
        }
    }

    // ===== HTML OpenStreetMap =====
    val osmHtml = remember {
        val initLat = prefs.getFloat(KEY_LAT, -6.2f)
        val initLng = prefs.getFloat(KEY_LNG, 106.816f)
        val initZoom = prefs.getFloat(KEY_ZOOM, 13f)
        """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
          <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
          <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
          <style>
            html,body,#map{height:100%;margin:0;padding:0;background:#0D0A22;}
            .leaflet-container{background:#0D0A22;}
            .leaflet-popup-content-wrapper{background:#1a1535;color:#fff;border:1px solid #8B5CF6;}
            .leaflet-popup-tip{background:#1a1535;}
          </style>
        </head>
        <body>
          <div id="map"></div>
          <script>
            var map = L.map('map', { zoomControl: true }).setView(
              [$initLat, $initLng], $initZoom
            );
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
              maxZoom: 19,
              attribution: '© OpenStreetMap'
            }).addTo(map);

            var marker = L.marker([$initLat, $initLng]).addTo(map);

            map.on('click', function(e) {
              var lat = e.latlng.lat;
              var lng = e.latlng.lng;
              marker.setLatLng([lat, lng]);
              if (window.Android && Android.setPosition) {
                Android.setPosition('(' + lat + ', ' + lng + ')');
              }
            });

            map.on('zoomend', function() {
              if (window.Android && Android.setZoom) {
                Android.setZoom(map.getZoom().toString());
              }
            });

            window.updateMarker = function(lat, lng) {
              if (marker) {
                marker.setLatLng([lat, lng]);
                map.setView([lat, lng], map.getZoom());
              }
            };
          </script>
        </body>
        </html>
        """.trimIndent()
    }

    // ===== URL Google Maps =====
    val googleMapsUrl = remember(latitude, longitude, currentZoom) {
        "https://www.google.com/maps/@$latitude,$longitude,${currentZoom}z"
    }

    // ===== JS Bridge =====
    val jsInterface = remember {
        WebAppInterface(
            onPositionPicked = { lat, lng ->
                saveCoords(lat, lng, "Peta")
                log("📍 Pilih dari peta: %.6f, %.6f".format(lat, lng))
            },
            onZoomChanged = { z -> saveZoom(z) }
        )
    }

    // ===== SYNC marker =====
    LaunchedEffect(latitude, longitude, pageLoaded, selectedMap) {
        if (pageLoaded && selectedMap == 0) {
            webViewRef?.evaluateJavascript(
                "if (typeof updateMarker === 'function') updateMarker($latitude, $longitude);",
                null
            )
        }
    }

    // ===== PERMISSION LAUNCHERS =====
    val coarseLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        log(if (granted) "✅ Izin coarse diberikan" else "⚠️ Izin coarse ditolak")
    }

    val fineLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        log(if (granted) "✅ Izin fine diberikan" else "⚠️ Izin fine ditolak")
        if (!granted) {
            NotificationHelper.send(
                context,
                context.getString(R.string.notif_title),
                "⚠️ Izin lokasi presisi ditolak. Spoofing mungkin tidak berfungsi."
            )
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            coarseLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            fineLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
        // SECTION 1: PILIH PETA
        GlassCard {
            SectionLabel("🗺️", "Pilih Peta")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                maps.forEachIndexed { index, name ->
                    MapChip(
                        text = name,
                        selected = selectedMap == index,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedMap = index
                            pageLoaded = false
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0D0A22))
            ) {
                if (selectedMap == 0) {
                    MapWebView(
                        htmlContent = osmHtml,
                        javaScriptInterface = jsInterface,
                        interfaceName = WebAppInterface.JS_NAME,
                        onPageLoaded = { pageLoaded = true },
                        onWebViewReady = { wv -> webViewRef = wv }
                    )
                } else {
                    MapWebView(
                        url = googleMapsUrl,
                        javaScriptInterface = jsInterface,
                        interfaceName = WebAppInterface.JS_NAME,
                        onPageLoaded = { pageLoaded = true },
                        onWebViewReady = { wv -> webViewRef = wv }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = if (selectedMap == 0)
                    "💡 Tap pada peta untuk memilih lokasi."
                else
                    "ℹ️ Google Maps hanya preview — tap tidak tersedia.",
                color = TextSoft,
                fontSize = 11.sp
            )

            if (lastPickedFrom != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "✅ Terakhir dipilih dari: $lastPickedFrom",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // SECTION 2: KOORDINAT
        GlassCard {
            SectionLabel("📍", stringResource(R.string.loc_sect))

            NeonTextBox(
                text = "Longitude",
                value = longitude.toString(),
                onValueChange = {
                    longitude = it.toDoubleOrNull() ?: 0.0
                    prefs.edit().putFloat(KEY_LNG, longitude.toFloat()).apply()
                },
                onFavoriteClick = {
                    log("⭐ Lokasi (${"%.4f".format(latitude)}, ${"%.4f".format(longitude)}) favorit")
                }
            )

            Spacer(Modifier.height(12.dp))

            NeonTextBox(
                text = "Latitude",
                value = latitude.toString(),
                onValueChange = {
                    latitude = it.toDoubleOrNull() ?: 0.0
                    prefs.edit().putFloat(KEY_LAT, latitude.toFloat()).apply()
                },
                onFavoriteClick = {
                    log("⭐ Lokasi (${"%.4f".format(latitude)}, ${"%.4f".format(longitude)}) favorit")
                }
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "Aktif: %.6f, %.6f  •  Zoom: ${currentZoom.toInt()}"
                    .format(latitude, longitude),
                color = TextSoft,
                fontSize = 11.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // SECTION 3: SPOOFER
        GlassCard {
            SectionLabel("🎛️", "Spoofer")

            NeonDropdown("Mode Spoof", modes, selectedMode) {
                selectedMode = it
                log("🧠 Mode → $it")
            }

            Spacer(Modifier.height(16.dp))

            NeonSwitch(
                "Mulai spoof lokasi",
                if (serviceReady) "Service siap — perlu Developer Options"
                else "Menunggu service...",
                switchOn
            ) { checked ->
                switchOn = checked

                if (checked) {
                    val b = binder
                    if (b == null) {
                        switchOn = false
                        errorMessage =
                            "Service belum siap. Tunggu beberapa detik lalu coba lagi."
                        showDialogError = true
                        log("❌ Binder null — service belum siap")
                    } else {
                        try {
                            b.startMock(
                                longitude, latitude,
                                0.0, 0.0,
                                1000L,
                                0,
                                0f
                            )
                            log("🚀 Spoofing dimulai: (%.6f, %.6f)".format(latitude, longitude))
                        } catch (e: SecurityException) {
                            switchOn = false
                            errorMessage =
                                "Akses ditolak. Pastikan:\n\n" +
                                "1. Developer Options aktif\n" +
                                "2. 'Nexus AI' dipilih sebagai mock location app\n" +
                                "3. Izin lokasi sudah diberikan"
                            showDialogError = true
                            log("❌ SecurityException: ${e.message}")
                        } catch (e: Exception) {
                            switchOn = false
                            errorMessage = e.message ?: "Error tidak diketahui"
                            showDialogError = true
                            log("❌ Error: ${e.message}")
                        }
                    }
                } else {
                    try {
                        log("⏹️ Spoofing dihentikan")
                    } catch (e: Exception) {
                        log("⚠️ Stop error: ${e.message}")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // SECTION 4: UPDATE KOORDINAT
        if (switchOn) {
            GlassCard {
                SectionLabel("🔄", "Update Koordinat")

                Text(
                    "Koordinat saat ini:",
                    color = TextSoft,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "%.6f, %.6f".format(latitude, longitude),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))

                PrimaryNeonButton("Kirim Update") {
                    binder?.startMock(
                        longitude, latitude,
                        0.0, 0.0,
                        1000L, 0, 0f
                    )
                    log("📡 Update: (%.6f, %.6f)".format(latitude, longitude))
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // SECTION 5: LOG
        GlassCard {
            SectionLabel("📡", "Log")
            InfoParagraph("Bersihkan Log", logs) { logs.clear() }
        }
    }

    // DIALOG ERROR
    if (showDialogError) {
        NeonDialog(
            title = "Location Spoofing Error",
            text = errorMessage,
            confirmButtonText = "Retry",
            dismissButtonText = "Tutup",
            onConfirm = {
                showDialogError = false
                scope.launch {
                    delay(300)
                    val b = binder ?: return@launch
                    try {
                        b.startMock(
                            longitude, latitude,
                            0.0, 0.0,
                            1000L, 0, 0f
                        )
                        switchOn = true
                        log("✅ Retry berhasil")
                    } catch (e: Exception) {
                        log("❌ Retry gagal: ${e.message}")
                    }
                }
            },
            onDismiss = {
                showDialogError = false
                log("❌ Dialog ditutup")
            }
        )
    }
}

@Composable
private fun MapChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected)
                    Brush.horizontalGradient(
                        listOf(
                            NeonPurple.copy(alpha = 0.75f),
                            NeonBlue.copy(alpha = 0.65f),
                            NeonCyan.copy(alpha = 0.60f)
                        )
                    )
                else
                    Brush.linearGradient(
                        listOf(Color(0x1AFFFFFF), Color(0x0AFFFFFF))
                    )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else TextSoft,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}