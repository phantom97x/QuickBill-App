package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val QuickBillDarkBlue = Color(0xFF1E3A8A)
val QuickBillBlue = Color(0xFF2563EB)
val QuickBillCyan = Color(0xFF06B6D4)
val QuickBillOrange = Color(0xFFF97316)
val QuickBillRose = Color(0xFFF43F5E)
val QuickBillGreen = Color(0xFF10B981)
val QuickBillGreenDark = Color(0xFF059669)
val QuickBillRed = Color(0xFFEF4444)
val QuickBillRedDark = Color(0xFFDC2626)
val QuickBillPurple = Color(0xFF7C3AED)
val QuickBillPink = Color(0xFFEC4899)

val AppBackground = Color(0xFFF5F7FB)
val CardBackground = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF0F172A)
val TextSecondary = Color(0xFF64748B)
val TextMuted = Color(0xFF94A3B8)
val BorderLight = Color(0xFFE2E8F0)

// Gradients
val HeaderGradient = Brush.linearGradient(
    colors = listOf(QuickBillDarkBlue, QuickBillBlue, QuickBillCyan)
)

val HeroAddBillGradient = Brush.linearGradient(
    colors = listOf(QuickBillOrange, QuickBillRose)
)

val DailySaleGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF1E40AF), QuickBillCyan)
)

val MonthlySaleGradient = Brush.linearGradient(
    colors = listOf(QuickBillPurple, QuickBillPink)
)

val SuccessGradient = Brush.linearGradient(
    colors = listOf(QuickBillGreen, QuickBillGreenDark)
)

val DangerGradient = Brush.linearGradient(
    colors = listOf(QuickBillRed, QuickBillRedDark)
)
