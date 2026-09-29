package com.nexus.ai.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nexus.ai.R
import com.nexus.ai.ui.theme.*

@Composable
fun NeonTextBox(
    text: String,
    value: String,
    onValueChange: (String) -> Unit,
    onFavoriteClick: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text ?: stringResource(R.string.label_username)) },
        placeholder = {
            Text(text, color = Color(0x66FFFFFF))
        },
        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
        trailingIcon = {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.clickable { onFavoriteClick() }
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0x88FFFFFF),
            unfocusedBorderColor = Color(0x33FFFFFF),
            focusedLabelColor = NeonCyan,
            unfocusedLabelColor = TextSoft,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color(0xFFE5E7EB),
            cursorColor = NeonCyan,
            focusedContainerColor = Color(0x14FFFFFF),
            unfocusedContainerColor = Color(0x0CFFFFFF),
            focusedLeadingIconColor = NeonCyan,
            unfocusedLeadingIconColor = TextSoft,
            focusedTrailingIconColor = NeonPink,
            unfocusedTrailingIconColor = TextSoft
        )
    )
}