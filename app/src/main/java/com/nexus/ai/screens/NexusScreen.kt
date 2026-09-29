package com.nexus.ai.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.ai.R
import com.nexus.ai.screens.tabs.AnalyticsTab
import com.nexus.ai.screens.tabs.HomeTab
import com.nexus.ai.screens.tabs.LocationTab
import com.nexus.ai.screens.tabs.SettingsTab
import com.nexus.ai.screens.tabs.WifiTab
import com.nexus.ai.ui.components.GlassOrb
import com.nexus.ai.ui.components.LiquidGlassNavBar
import com.nexus.ai.ui.components.NavItem
import com.nexus.ai.ui.theme.*

@Composable
fun NexusScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val stateHolder = rememberSaveableStateHolder()

    // 5 tab — urutan sama dengan when(tab)
    val navItems = listOf(
        NavItem(Icons.Filled.Home, stringResource(R.string.tab_home)),
        NavItem(Icons.Filled.BarChart, stringResource(R.string.tab_analytics)),
        NavItem(Icons.Filled.MyLocation, stringResource(R.string.tab_spoof)),
        NavItem(Icons.Filled.Wifi, "WiFi"),
        NavItem(Icons.Filled.Settings, stringResource(R.string.tab_settings))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, BgDeep2, BgDeep3, BgDeep)))
    ) {
        // ── SOFT BLURRED COLOR ORBS ────────────────────────
        Box(
            Modifier
                .size(360.dp)
                .offset((-120).dp, (-80).dp)
                .blur(90.dp)
        ) { GlassOrb(color = NeonPurple, alpha = 0.75f) }

        Box(
            Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(90.dp, 220.dp)
                .blur(90.dp)
        ) { GlassOrb(color = NeonCyan, alpha = 0.55f) }

        Box(
            Modifier
                .size(300.dp)
                .align(Alignment.BottomStart)
                .offset((-60).dp, (-40).dp)
                .blur(90.dp)
        ) { GlassOrb(color = NeonPink, alpha = 0.45f) }

        // ── KONTEN ─────────────────────────────────────────
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = stringResource(R.string.header_title),
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            listOf(NeonCyan, NeonViolet, NeonPink)
                        ),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = when (selectedTab) {
                        0 -> stringResource(R.string.header_subtitle)
                        1 -> "Analytics Dashboard"
                        2 -> "Location & Map Picker"
                        3 -> "WiFi Scanner & Auto Connect"
                        else -> "Settings & Preferences"
                    },
                    color = TextSoft,
                    fontSize = 12.sp
                )
            }

            // Konten tab
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(durationMillis = 160),
                label = "tab_content"
            ) { tab ->
                stateHolder.SaveableStateProvider(tab) {
                    when (tab) {
                        0 -> HomeTab()
                        1 -> AnalyticsTab()
                        2 -> LocationTab()
                        3 -> WifiTab()
                        else -> SettingsTab()
                    }
                }
            }
        }

        // ── NAV BAR (vertikal kanan bawah) ─────────────────
        LiquidGlassNavBar(
            items = navItems,
            selectedIndex = selectedTab,
            onSelect = { selectedTab = it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = 20.dp)
        )
    }
}