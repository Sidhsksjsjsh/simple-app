package com.nexus.ai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.ai.ui.theme.*

@Composable
fun UpdateDialog(
    versionName: String,
    changelog: String,
    mandatory: Boolean,
    downloading: Boolean,
    progress: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!mandatory && !downloading) onDismiss() },
        containerColor = Color(0xFF14102B),
        shape = RoundedCornerShape(28.dp),
        icon = { Text(if (downloading) "⬇️" else "🚀", fontSize = 34.sp) },
        title = {
            Text(
                if (downloading) "Mengunduh Update..."
                else "Update Tersedia",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                if (downloading) {
                    Text(
                        "Versi $versionName sedang diunduh.\nJangan tutup aplikasi.",
                        color = TextSoft,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = NeonCyan,
                        trackColor = Color(0x22FFFFFF)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "$progress%",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                } else {
                    Text(
                        "Versi terbaru: $versionName",
                        color = NeonCyan,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        changelog.ifBlank { "Perbaikan bug dan peningkatan performa." },
                        color = TextSoft,
                        lineHeight = 20.sp,
                        fontSize = 13.sp
                    )
                    if (mandatory) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "⚠️ Update ini wajib untuk melanjutkan.",
                            color = NeonPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!downloading) {
                TextButton(onClick = onConfirm) {
                    Text("UPDATE", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!mandatory && !downloading) {
                TextButton(onClick = onDismiss) {
                    Text("NANTI", color = NeonPink)
                }
            }
        }
    )
}