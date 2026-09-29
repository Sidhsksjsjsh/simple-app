package com.nexus.ai.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.ai.R
import com.nexus.ai.ui.theme.NeonPink
import com.nexus.ai.ui.theme.TextHard

@Composable
fun InfoParagraph(clearText: String, logs: List<String>, onClear: () -> Unit) {
    val text = if (logs.isEmpty()) {
        stringResource(R.string.log_empty)
    } else {
        logs.joinToString("\n\n")
    }
    Text(text = text, color = TextHard, fontSize = 13.sp, lineHeight = 20.sp)

    Spacer(Modifier.height(12.dp))

    if (logs.isNotEmpty()) {
        Text(
            text = clearText ?: stringResource(R.string.log_clear),
            color = NeonPink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onClear() }
        )
    }
}