package com.nexus.ai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.ai.R
import com.nexus.ai.ui.theme.*

@Composable
fun NeonDialog(
    title: String,
    text: String,
    confirmButtonText: String,
    dismissButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF14102B),
        shape = RoundedCornerShape(28.dp),
        icon = { Text("⚡", fontSize = 34.sp) },
        title = {
            Text(title ?: stringResource(R.string.dialog_title), color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(text,
                color = TextSoft,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmButtonText ?: stringResource(R.string.dialog_confirm), color = NeonCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissButtonText ?: stringResource(R.string.dialog_cancel), color = NeonPink)
            }
        }
    )
}