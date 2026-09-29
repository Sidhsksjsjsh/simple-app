package com.nexus.ai.screens.tabs

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.nexus.ai.R
import com.nexus.ai.ui.components.*
import com.nexus.ai.ui.theme.*
import com.nexus.ai.utils.NotificationHelper

@Composable
fun HomeTab() {
    val context = LocalContext.current

    var userName by remember { mutableStateOf("") }
    var switchOn by remember { mutableStateOf(true) }
    var sliderValue by remember { mutableStateOf(0.65f) }
    var selectedMode by remember { mutableStateOf("Neural Core") }
    var showDialog by remember { mutableStateOf(false) }
    val logs = remember { mutableStateListOf<String>() }

    val modes = listOf("Neural Core", "Quantum Drive", "Hyper Vision", "Deep Dream")

    fun log(msg: String) {
        logs.add(0, msg)
        if (logs.size > 10) logs.removeAt(logs.lastIndex)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> log(if (granted) "✅ Izin notifikasi diberikan" else "⚠️ Izin ditolak") }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 120.dp)
    ) {
        // TEXTBOX
        GlassCard {
            SectionLabel("👤", stringResource(R.string.section_identity))
            NeonTextBox(
                text = stringResource(R.string.label_username),
                value = userName,
                onValueChange = {
                    userName = it
                    if (it.length == 1) log("👤 Operator mulai mengetik...")
                },
                onFavoriteClick = {
                    if (userName.isNotBlank()) log("⭐ '$userName' ditandai favorit")
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        // SWITCH + SLIDER
        GlassCard {
            SectionLabel("⚙️", stringResource(R.string.section_config))
            NeonSwitch(stringResource(R.string.neural_active), "no", switchOn) {
                switchOn = it
                log(if (it) "🟢 Neural DIAKTIFKAN" else "🔴 Neural DIMATIKAN")
            }
            Spacer(Modifier.height(18.dp))
            NeonSlider(stringResource(R.string.processor_intensity), sliderValue, { sliderValue = it }) {
                log("🎚️ Intensitas ${(sliderValue * 100).toInt()}%")
            }
        }

        Spacer(Modifier.height(16.dp))

        // DROPDOWN
        GlassCard {
            SectionLabel("🧠", stringResource(R.string.section_mode))
            NeonDropdown(stringResource(R.string.operation_mode), modes, selectedMode) {
                selectedMode = it
                log("🧠 Mode → $it")
            }
        }

        Spacer(Modifier.height(16.dp))

        // TOMBOL
        PrimaryNeonButton(stringResource(R.string.button_run)) {
            log("🚀 Sistem dijalankan oleh ${userName.ifBlank { "Anonim" }}")
            showDialog = true
        }
        Spacer(Modifier.height(12.dp))
        OutlinedNeonButton(stringResource(R.string.button_notify)) {
            NotificationHelper.send(
                context,
                context.getString(R.string.notif_title),
                "Mode $selectedMode aktif, intensitas ${(sliderValue * 100).toInt()}%"
            )
            log("🔔 Notifikasi dikirim")
        }

        Spacer(Modifier.height(20.dp))

        // PARAGRAF LOG
        GlassCard {
            SectionLabel("📡", stringResource(R.string.section_log))
            InfoParagraph(stringResource(R.string.log_clear), logs) { logs.clear() }
        }
    }

    // DIALOG
    if (showDialog) {
        NeonDialog(
            title = "Konfirmasi Eksekusi",
            text = "Test Intent", //"• Operator: ${userName.ifBlank { "Anonim" }}\n" + "• Mode: $selectedMode\n" + "• Intensitas: ${(sliderValue * 100).toInt()}%\n" + "• Neural: ${if (switchOn) "AKTIF" else "NONAKTIF"}\n\n" + "Lanjutkan?",,
            confirmButtonText = "Ya, Jalankan",
            dismissButtonText = "Batal",
            onConfirm = {
                showDialog = false
                log("✅ Eksekusi — mode $selectedMode")
            },
            onDismiss = {
                showDialog = false
                log("❌ Dibatalkan")
            }
        )
    }
}