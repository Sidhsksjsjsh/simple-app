package com.nexus.ai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.ai.R
import com.nexus.ai.ui.theme.NeonCyan
import com.nexus.ai.ui.theme.NeonPurple
import com.nexus.ai.ui.theme.TextSoft

@Composable
fun NeonSwitch(
    text: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text ?: stringResource(R.string.neural_active), color = Color.White, fontSize = 15.sp)
            if (text != stringResource(R.string.neural_active)) {
                Text(
                    description,
                    color = TextSoft, fontSize = 12.sp
                )
            } else if (text == stringResource(R.string.neural_active)) {
                Text(
                    if (checked) stringResource(R.string.system_optimal)
                    else stringResource(R.string.system_idle),
                    color = TextSoft, fontSize = 12.sp
                )
            }
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

@Composable
fun NeonToggle(
    text: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text ?: stringResource(R.string.neural_active), color = Color.White, fontSize = 15.sp)
            Text(
                description,
                color = TextSoft, fontSize = 12.sp
            )
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