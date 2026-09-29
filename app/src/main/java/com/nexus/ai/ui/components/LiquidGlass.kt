package com.nexus.ai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexus.ai.ui.theme.*

/**
 * LiquidGlassSurface — 7 layer efek kaca iOS 26:
 *  1. Ambient shadow (luar)
 *  2. Base tint translusen
 *  3. Top specular highlight (sumber cahaya atas-kiri)
 *  4. Chromatic reflection (3 warna: pink–ungu–cyan dari lingkungan)
 *  5. Inner bottom glow (subsurface scattering)
 *  6. Inner shadow tipis (di bawah permukaan)
 *  7. Rim light / fresnel edge (border gradien)
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    tint: Color = GlassBase,
    borderWidth: Dp = 0.8.dp,
    shadowHeight: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            // Layer 1: Ambient shadow
            .drawBehind {
                val cr = CornerRadius(size.height * 0.35f, size.height * 0.35f)
                drawRoundRect(
                    color = GlassAmbient,
                    topLeft = Offset(0f, 6f),
                    size = size,
                    cornerRadius = cr
                )
            }
            .clip(shape)
            // Layer 2: Base tint
            .background(tint)
            // Layer 3: Top specular
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        GlassSpecular,
                        GlassSpecularSoft,
                        Color.Transparent
                    ),
                    start = Offset(0f, 0f),
                    end = Offset.Infinite
                )
            )
            // Layer 4: Chromatic reflection
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        GlassChromaticR,
                        GlassChromaticV,
                        GlassChromaticC,
                        Color.Transparent
                    ),
                    center = Offset(0f, 0f),
                    radius = 800f
                )
            )
            // Layer 5: Inner bottom glow
            .background(
                Brush.verticalGradient(
                    0.65f to Color.Transparent,
                    1f to Color.White.copy(alpha = 0.12f)
                )
            )
            // Layer 6: Inner shadow (subtle dark band atas)
            .background(
                Brush.verticalGradient(
                    0f to GlassInnerShadow,
                    0.15f to Color.Transparent
                )
            )
            // Layer 7: Fresnel rim light
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GlassFresnel,
                        GlassFresnelSoft,
                        Color.White.copy(alpha = 0.18f),
                        NeonCyan.copy(alpha = 0.55f)
                    )
                ),
                shape = shape
            ),
        content = content
    )
}

/** Pill kecil (label, chip) */
@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(50),
    content: @Composable BoxScope.() -> Unit = {}
) {
    LiquidGlassSurface(
        modifier = modifier,
        shape = shape,
        tint = GlassBaseBright,
        borderWidth = 0.7.dp,
        content = content
    )
}

/** Orb dekoratif untuk background */
@Composable
fun GlassOrb(
    modifier: Modifier = Modifier,
    color: Color = NeonPurple,
    alpha: Float = 0.5f
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = alpha),
                        color.copy(alpha = alpha * 0.4f),
                        Color.Transparent
                    )
                )
            )
    )
}