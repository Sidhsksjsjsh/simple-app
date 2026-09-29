package com.nexus.ai.ui.components

import android.content.Context
import androidx.compose.runtime.Composable

// Komponen pemanggil notifikasi (delegasi ke util)
@Composable
fun NeonNotificationTrigger(
    context: Context,
    title: String,
    message: String,
    onSent: () -> Unit
) {
    // Fungsi dipanggil dari parent, bukan efek
    // Placeholder composable (tidak render apapun)
}