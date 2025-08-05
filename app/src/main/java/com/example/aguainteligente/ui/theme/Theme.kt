package com.example.aguainteligente.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BlueElectric,
    onPrimary = Color.White,
    primaryContainer = SurfaceDark,
    onPrimaryContainer = BlueElectric,
    inversePrimary = BlueElectric,
    secondary = GreenAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF00643C),
    onSecondaryContainer = Color(0xFFC0FFD9),
    tertiary = Color(0xFF98DAF5),
    onTertiary = Color(0xFF003544),
    tertiaryContainer = Color(0xFF004D60),
    onTertiaryContainer = Color(0xFFB3F2FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = BlueDark,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = GreyLight,
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C6CF),
    outline = Color(0xFF8D9199),
    inverseOnSurface = Color(0xFF0D1B2A),
    inverseSurface = Color(0xFFE0E0E0),
    surfaceTint = BlueElectric,
    outlineVariant = Color(0xFF43474E),
    scrim = Color(0xFF000000),
)

private val LightColorScheme = lightColorScheme(
    primary = BlueElectric,
    secondary = GreenAccent,
    tertiary = BlueDark,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

@Composable
fun AguaInteligenteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}