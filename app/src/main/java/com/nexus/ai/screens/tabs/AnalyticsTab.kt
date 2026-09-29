package com.nexus.ai.screens.tabs

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.ai.ui.components.GlassCard
import com.nexus.ai.ui.components.SectionLabel
import com.nexus.ai.ui.theme.*

@Composable
fun AnalyticsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 120.dp)
    ) {
        // ===== Kartu statistik =====
        GlassCard {
            SectionLabel("📊", "Real-time Metrics")
            StatBar("CPU Load", 0.72f, NeonCyan)
            Spacer(Modifier.height(14.dp))
            StatBar("Memory", 0.48f, NeonViolet)
            Spacer(Modifier.height(14.dp))
            StatBar("Network I/O", 0.86f, NeonPink)
            Spacer(Modifier.height(14.dp))
            StatBar("GPU Usage", 0.61f, NeonBlue)
        }

        Spacer(Modifier.height(16.dp))

        GlassCard {
            SectionLabel("⚡", "Throughput")
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricPill("Uptime", "99.98%")
                MetricPill("Requests", "1.2M")
                MetricPill("Latency", "12ms")
            }
        }

        Spacer(Modifier.height(16.dp))

        GlassCard {
            SectionLabel("🌐", "Ringkasan Sistem")
            Text(
                "Semua subsistem beroperasi pada kapasitas optimal. " +
                "Tidak ada anomali terdeteksi dalam 24 jam terakhir. " +
                "Neural engine menggunakan 72% dari alokasi komputasi.",
                color = TextHard,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun StatBar(label: String, value: Float, color: Color) {
    // Animasi progress dari 0 → value saat tab dibuka
    val animated by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "stat"
    )

    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White, fontSize = 13.sp)
            Text(
                "${(value * 100).toInt()}%",
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x22FFFFFF))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animated)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(color.copy(alpha = 0.6f), color)
                        )
                    )
            )
        }
    }
}

@Composable
private fun MetricPill(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x22FFFFFF))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(value, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = TextSoft, fontSize = 11.sp)
    }
}