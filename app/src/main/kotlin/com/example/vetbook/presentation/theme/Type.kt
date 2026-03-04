package com.example.vetbook.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Using the system default sans-serif stack (Roboto on Android, which pairs well
// with the Inter aesthetic at the weights we need). Switching to bundled Inter TTF
// files is straightforward — place them in res/font/ and swap FontFamily.SansSerif
// for a custom FontFamily declaration.
val InterFontFamily = FontFamily.SansSerif

val Typography = Typography(
    // Large page titles (Payment Successful!, etc.)
    displayLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 28.sp,
        lineHeight    = 34.sp,
        letterSpacing = 0.sp
    ),
    // Top bar titles, section headings
    titleLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 20.sp,
        lineHeight    = 26.sp,
        letterSpacing = 0.sp
    ),
    // Card titles
    titleMedium = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 16.sp,
        lineHeight    = 22.sp,
        letterSpacing = 0.sp
    ),
    // Body copy
    bodyLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.sp
    ),
    // Captions / sub-labels
    bodySmall = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.sp
    ),
    // Tiny labels (calendar days, filter chips)
    labelSmall = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.sp
    ),
)
