package com.example.dsy1105_006d_lc7.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary = Burdeos,
    onPrimary = Color.White,
    primaryContainer = BurdeosClaro,
    onPrimaryContainer = BurdeosOscuro,
    secondary = Acero,
    onSecondary = Color.White,
    secondaryContainer = AceroClaro,
    onSecondaryContainer = Color(0xFF1C1F24),
    tertiary = Ambar,
    onTertiary = Color.White,
    tertiaryContainer = AmbarClaro,
    onTertiaryContainer = Color(0xFF2B1700),
    background = FondoApp,
    onBackground = Color(0xFF1B1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFEDEDF0),
    onSurfaceVariant = Color(0xFF45464F)
)

@Composable
fun DSY1105006DLC7Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}