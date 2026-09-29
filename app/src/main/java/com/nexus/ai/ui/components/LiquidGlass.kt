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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexus.ai.ui.theme.*

/**
 * LiquidGlassSurface — meniru efek iOS 26 Liquid Glass dengan 6 layer:
 *  1. Base tint translusen
 *  2. Top sheen (highlight dari sumber cahaya atas)
 *  3. Chromatic reflection (ungu/cyan dari lingkungan sekitar)
 *  4. Bottom inner glow (subsurface scattering)
 *  5. Rim light (garis tepi memantulkan cahaya)
 *  6. Ambient shadow (bayangan lembut di bawah)
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    tint: Color = GlassBase,
    tintBright: Color = GlassBaseBright,
    borderWidth: Dp = 0.8.dp,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            // ---- Layer 6: Ambient shadow (di luar area) ----
            .drawBehind {
                drawRoundRect(
                    color = GlassShadow,
                    topLeft = Offset(0f, 8f),
                    size = size.copy(height = size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        size.height * 0.35f, size.height * 0.35f
                    )
                )
            }
            .clip(shape)
            // ---- Layer 1: Base tint ----
            .background(tint)
            // ---- Layer 2: Top sheen (linear, gelap di tengah) ----
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        GlassSheen,
                        Color.Transparent,
                        tintBright
                    ),
                    start = Offset(0f, 0f),
                    end = Offset.Infinite
                )
            )
            // ---- Layer 3: Chromatic reflection ----
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        GlassChromaticA,
                        Color.Transparent,
                        GlassChromaticB
                    ),
                    center = Offset(0f, 0f),
                    radius = 600f
                )
            )
            // ---- Layer 4: Bottom inner glow ----
            .background(
                Brush.verticalGradient(
                    0.7f to Color.Transparent,
                    1f to Color.White.copy(alpha = 0.10f)
                )
            )
            // ---- Layer 5: Rim light ----
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GlassRim,          // kiri atas terang
                        GlassRimSoft,      // tengah lembut
                        Color.White.copy(alpha = 0.15f),
                        NeonCyan.copy(alpha = 0.45f) // kanan bawah sedikit neon
                    )
                ),
                shape = shape
            ),
        content = content
    )
}

/**
 * Varian liquid glass untuk pill / tombol bulat kecil
 */
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

/**
 * Ornamen orb yang dipakai untuk background (soft blurred color blobs)
 * — pakai bersamaan dengan Modifier.blur() untuk efek seperti iOS
 */
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