package com.cmu.sweet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColors = lightColorScheme(
    primary = RichGold,
    onPrimary = Color.White,
    secondary = BurntOrange,
    onSecondary = Color.White,
    background = WarmWhite,
    onBackground = DarkChocolate,
    surface = AlmondMilk,
    onSurface = DarkChocolate,
    surfaceVariant = Color.White,
    error = MutedRed,
    onError = Color.White
)

val DarkColors = darkColorScheme(
    primary = RichGold,
    onPrimary = Color.Black,
    secondary = SoftMustard,
    onSecondary = Color.Black,
    background = DarkChocolate,
    onBackground = WarmWhite,
    surface = TaupeGray,
    onSurface = WarmWhite,
    surfaceVariant = Color.Black,
    error = MutedRed,
    onError = Color.Black
)

@Composable
fun SweetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
