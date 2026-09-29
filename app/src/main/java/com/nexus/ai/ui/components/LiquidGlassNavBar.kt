package com.nexus.ai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.ai.ui.theme.*
import kotlinx.coroutines.launch

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
        verticalArrangement = Arrangement.spacedBy(14.dp),
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
    val scope = rememberCoroutineScope()

    // ── Ukuran tombol (spring bouncy saat dipilih) ─────────
    val size by animateDpAsState(
        targetValue = if (selected) 62.dp else 52.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "size"
    )

    // ── Scale ikon (bounce) ────────────────────────────────
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_scale"
    )

    // ── Warna ikon ─────────────────────────────────────────
    val iconTint by animateColorAsState(
        targetValue = if (selected) Color.White else TextSoft,
        animationSpec = tween(220),
        label = "icon_tint"
    )

    // ── Rotasi halus saat dipilih ──────────────────────────
    val iconRotation by animateFloatAsState(
        targetValue = if (selected) 4f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_rotation"
    )

    // ── Skala "press" saat ditekan ─────────────────────────
    val pressScale = remember { Animatable(1f) }

    // ── Pulse ring animasi untuk tab yang dipilih ─────────
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // ── Label pill (muncul saat aktif) ─────────────────
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(200)) +
                slideInHorizontally(tween(260)) { it / 2 },
            exit = fadeOut(tween(150)) +
                slideOutHorizontally(tween(200)) { it / 2 }
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0x40FFFFFF),
                                Color(0x18FFFFFF),
                                NeonCyan.copy(alpha = 0.25f)
                            )
                        )
                    )
                    .border(
                        width = 0.8.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xCCFFFFFF),
                                Color(0x22FFFFFF),
                                NeonCyan.copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(17.dp)
                    )
                    .padding(horizontal = 14.dp),
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

        // ── Tombol utama ────────────────────────────────────
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = pressScale.value
                    scaleY = pressScale.value
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // Press animation
                    scope.launch {
                        pressScale.animateTo(
                            0.9f,
                            animationSpec = tween(80)
                        )
                        pressScale.animateTo(
                            1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            // ── Pulse ring (hanya selected) ─────────────────
            if (selected) {
                Box(
                    Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                            alpha = pulseAlpha
                        }
                        .clip(CircleShape)
                        .border(
                            width = 1.5.dp,
                            color = NeonCyan.copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                )
            }

            // ── Outer glow (radial) — hanya selected ────────
            if (selected) {
                Box(
                    Modifier
                        .matchParentSize()
                        .scale(1.35f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    NeonViolet.copy(alpha = 0.45f),
                                    NeonCyan.copy(alpha = 0.18f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // ── Liquid glass circle utama ───────────────────
            LiquidGlassPill(
                modifier = Modifier.matchParentSize(),
                shape = CircleShape
            ) {
                // Tint ungu-cyan saat aktif
                if (selected) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        NeonPurple.copy(alpha = 0.65f),
                                        NeonBlue.copy(alpha = 0.45f),
                                        NeonCyan.copy(alpha = 0.55f)
                                    )
                                )
                            )
                    )

                    // Specular dot tambahan di atas
                    Box(
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 6.dp)
                            .size(width = 20.dp, height = 6.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.75f),
                                        Color.White.copy(alpha = 0.0f)
                                    )
                                )
                            )
                    )
                }

                // Icon
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
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                                rotationZ = iconRotation
                            }
                    )
                }
            }
        }
    }
}