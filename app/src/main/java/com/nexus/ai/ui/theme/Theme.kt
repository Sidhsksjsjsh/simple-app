package com.nexus.ai.ui.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun NexusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = NeonPurple,
            secondary = NeonCyan,
            tertiary = NeonPink,
            background = BgDeep,
            surface = BgDeep2
        ),
        typography = Typography,
        content = content
    )
}