package com.example.vetbook.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary           = Brand,
    onPrimary         = TextPrimary,
    primaryContainer  = BrandSurface,
    secondary         = BrandDark,
    background        = Background,
    surface           = SurfaceColor,
    onBackground      = TextPrimary,
    onSurface         = TextPrimary,
    onSurfaceVariant  = TextSecondary,
    error             = Error,
    outline           = Divider,
)

@Composable
fun VetBookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Always use our fixed light scheme — no dynamic colour so that our
    // brand palette is not overridden on Android 12+ devices.
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = Typography,
        content     = content
    )
}
