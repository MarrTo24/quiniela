package com.quiniela.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Paleta ──────────────────────────────────────────────────────────────────

val GreenField = Color(0xFF1B5E20)       // Verde cancha oscuro
val GreenFieldLight = Color(0xFF2E7D32)
val GreenAccent = Color(0xFF4CAF50)
val GoldAccent = Color(0xFFFFC107)
val BackgroundDark = Color(0xFF0D1B0E)
val SurfaceDark = Color(0xFF1A2B1C)
val SurfaceVariantDark = Color(0xFF243326)

// Estados de partido
val StatusPending = Color(0xFF78909C)
val StatusSaved = Color(0xFF1565C0)
val StatusResult = Color(0xFF2E7D32)
val StatusClosed = Color(0xFF37474F)

private val DarkColorScheme = darkColorScheme(
    primary = GreenAccent,
    onPrimary = Color.Black,
    primaryContainer = GreenFieldLight,
    onPrimaryContainer = Color.White,
    secondary = GoldAccent,
    onSecondary = Color.Black,
    background = BackgroundDark,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFB0BEC5),
    outline = Color(0xFF37474F)
)

@Composable
fun QuinielaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
