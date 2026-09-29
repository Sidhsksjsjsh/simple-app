package com.nexus.ai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nexus.ai.R
import com.nexus.ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeonDropdown(
    text: String,
    modes: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            label = { Text(text) },
            leadingIcon = { Icon(Icons.Filled.Settings, null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = Color(0x33FFFFFF),
                focusedLabelColor = NeonCyan,
                unfocusedLabelColor = TextSoft,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color(0xFFE5E7EB),
                focusedContainerColor = Color(0x11FFFFFF),
                unfocusedContainerColor = Color(0x11FFFFFF)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF161230))
        ) {
            modes.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode, color = Color.White) },
                    onClick = {
                        onSelected(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}