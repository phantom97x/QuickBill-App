package com.example.quickbill.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun QuickBillLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Background squircle with gradient
            val bgGradient = Brush.linearGradient(
                colors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF06B6D4)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
            drawRoundRect(
                brush = bgGradient,
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                cornerRadius = CornerRadius(w * 0.28f, h * 0.28f)
            )

            // Receipt Paper Sheet in the background
            val paperMarginX = w * 0.22f
            val paperTop = h * 0.18f
            val paperBottom = h * 0.82f
            val paperWidth = w * 0.56f

            val paperPath = Path().apply {
                moveTo(paperMarginX, paperTop)
                lineTo(paperMarginX + paperWidth, paperTop)
                lineTo(paperMarginX + paperWidth, paperBottom)
                // Zigzag bottom edge for receipt feel
                val segments = 4
                val segWidth = paperWidth / segments
                for (i in segments downTo 1) {
                    val xPeak = paperMarginX + (i - 0.5f) * segWidth
                    val xValley = paperMarginX + (i - 1) * segWidth
                    lineTo(xPeak, paperBottom - (h * 0.04f))
                    lineTo(xValley, paperBottom)
                }
                close()
            }
            drawPath(
                path = paperPath,
                color = Color.White.copy(alpha = 0.95f),
                style = Fill
            )

            // Horizontal receipt lines
            val linePaintColor = Color(0xFF93C5FD)
            val strokeW = w * 0.04f
            drawLine(
                color = linePaintColor,
                start = Offset(paperMarginX + w * 0.08f, h * 0.32f),
                end = Offset(paperMarginX + paperWidth - w * 0.08f, h * 0.32f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = linePaintColor,
                start = Offset(paperMarginX + w * 0.08f, h * 0.44f),
                end = Offset(paperMarginX + paperWidth - w * 0.18f, h * 0.44f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )

            // Energetic Lightning Bolt / Fast Checkmark across the receipt
            val boltPath = Path().apply {
                moveTo(w * 0.56f, h * 0.34f)
                lineTo(w * 0.40f, h * 0.56f)
                lineTo(w * 0.52f, h * 0.56f)
                lineTo(w * 0.44f, h * 0.78f)
                lineTo(w * 0.68f, h * 0.50f)
                lineTo(w * 0.56f, h * 0.50f)
                close()
            }
            val boltGradient = Brush.linearGradient(
                colors = listOf(Color(0xFFFACC15), Color(0xFFF97316)),
                start = Offset(w * 0.4f, h * 0.34f),
                end = Offset(w * 0.68f, h * 0.78f)
            )
            drawPath(
                path = boltPath,
                brush = boltGradient,
                style = Fill
            )
            drawPath(
                path = boltPath,
                color = Color(0xFF78350F).copy(alpha = 0.3f),
                style = Stroke(width = w * 0.015f, join = StrokeJoin.Round)
            )
        }
    }
}
