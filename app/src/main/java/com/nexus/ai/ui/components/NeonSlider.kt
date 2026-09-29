package com.nexus.ai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.ai.R
import com.nexus.ai.ui.theme.NeonCyan
import com.nexus.ai.ui.theme.NeonViolet

@Composable
fun NeonSlider(
    text: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text ?: stringResource(R.string.processor_intensity), color = Color.White, fontSize = 15.sp)
            Text(
                "${(value * 100).toInt()}%",
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            colors = SliderDefaults.colors(
                thumbColor = NeonCyan,
                activeTrackColor = NeonViolet,
                inactiveTrackColor = Color(0xFF2A2440)
            )
        )
    }
}