package com.nexus.ai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.ai.ui.theme.*

data class NavItem(
    val icon: ImageVector,
    val label: String
)

@Composable
fun LiquidGlassNavBar(
    items: List<NavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        items.forEachIndexed { index, item ->
            LiquidGlassNavButton(
                icon = item.icon,
                label = item.label,
                selected = index == selectedIndex,
                onClick = { onSelect(index) }
            )
        }
    }
}

@Composable
private fun LiquidGlassNavButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val size by animateDpAsState(
        targetValue = if (selected) 62.dp else 54.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fab_size"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_scale"
    )

    val iconTint by animateColorAsState(
        targetValue = if (selected) Color.White else TextSoft,
        label = "icon_tint"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // Label pill (muncul saat aktif)
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn() + slideInHorizontally { it / 2 },
            exit = fadeOut() + slideOutHorizontally { it / 2 }
        ) {
            LiquidGlassPill(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .height(32.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.6.sp
                    )
                }
            }
        }

        // Tombol bulat liquid glass
        Box(
            modifier = Modifier
                .size(size)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow saat selected
            if (selected) {
                Box(
                    Modifier
                        .matchParentSize()
                        .scale(1.2f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    NeonViolet.copy(alpha = 0.55f),
                                    NeonCyan.copy(alpha = 0.20f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Liquid glass circle
            LiquidGlassPill(
                modifier = Modifier.matchParentSize(),
                shape = CircleShape
            ) {
                // Kalau selected, tambahkan tint ungu
                if (selected) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        NeonPurple.copy(alpha = 0.55f),
                                        NeonBlue.copy(alpha = 0.35f),
                                        NeonCyan.copy(alpha = 0.45f)
                                    )
                                )
                            )
                    )
                }

                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = iconTint,
                        modifier = Modifier
                            .size(22.dp)
                            .scale(iconScale)
                    )
                }
            }
        }
    }
}