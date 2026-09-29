package com.nexus.ai.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.ai.ui.components.GlassCard
import com.nexus.ai.ui.components.SectionLabel
import com.nexus.ai.ui.theme.*

@Composable
fun SettingsTab() {
    var haptics by remember { mutableStateOf(true) }
    var animations by remember { mutableStateOf(true) }
    var autoSync by remember { mutableStateOf(false) }
    var betaFeatures by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 120.dp)
    ) {
        GlassCard {
            SectionLabel("🎛️", "Preferensi")
            SettingRow("Haptic Feedback", "Getaran saat interaksi", haptics) { haptics = it }
            Spacer(Modifier.height(14.dp))
            SettingRow("Animasi UI", "Efek transisi dan transparansi", animations) { animations = it }
            Spacer(Modifier.height(14.dp))
            SettingRow("Auto Sync", "Sinkronisasi otomatis tiap 15 menit", autoSync) { autoSync = it }
        }

        Spacer(Modifier.height(16.dp))

        GlassCard {
            SectionLabel("🧪", "Eksperimental")
            SettingRow("Beta Features", "Aktifkan fitur uji coba terbaru", betaFeatures) { betaFeatures = it }
        }

        Spacer(Modifier.height(16.dp))

        GlassCard {
            SectionLabel("ℹ️", "Tentang")
            Text("Nexus AI", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Versi 4.2.0 (build 1)", color = TextSoft, fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            Text(
                "Aplikasi kontrol neural futuristik dengan antarmuka glassmorphism. " +
                "Dibangun menggunakan Jetpack Compose.",
                color = TextHard, fontSize = 13.sp, lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp)
            Text(subtitle, color = TextSoft, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NeonPurple,
                checkedBorderColor = NeonCyan,
                uncheckedThumbColor = Color(0xFF6B7280),
                uncheckedTrackColor = Color(0xFF1F1B36),
                uncheckedBorderColor = Color(0x33FFFFFF)
            )
        )
    }
}