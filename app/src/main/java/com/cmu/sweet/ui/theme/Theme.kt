package com.cmu.sweet.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color // Import Color se precisar de algum ajuste fino aqui
import androidx.compose.ui.platform.LocalContext


private val NewLightColorScheme = lightColorScheme(
    primary = RichGold,                 // Cor principal para botões, FABs, elementos ativos
    onPrimary = DarkChocolate,          // Texto/ícones sobre a primária
    primaryContainer = LightCamel,      // Container para elementos primários (ex: fundo de um card destacado)
    onPrimaryContainer = DarkChocolate,

    secondary = DeepCaramel,            // Cor secundária
    onSecondary = WarmWhite,            // Texto/ícones sobre a secundária
    secondaryContainer = AlmondMilk,    // Container para elementos secundários
    onSecondaryContainer = DarkChocolate,

    tertiary = SoftMustard,             // Cor terciária para acentos (pode ser BurntOrange se quiser mais impacto)
    onTertiary = DarkChocolate,
    tertiaryContainer = PaleButterYellow,
    onTertiaryContainer = DarkChocolate,

    error = MutedRed,                   // Cor para erros
    onError = WarmWhite,                // Texto/ícone sobre a cor de erro
    errorContainer = Color(0xFFFFDAD6), // Vermelho muito claro para fundo de erro (pode ajustar para um tom mais quente)
    onErrorContainer = Color(0xFF410002),// Texto escuro sobre o container de erro

    background = WarmWhite,             // Cor de fundo principal das telas
    onBackground = DarkChocolate,       // Cor do texto principal sobre o fundo

    surface = AlmondMilk,               // Cor de superfície para Cards, Sheets, Menus
    onSurface = DarkChocolate,          // Texto sobre as superfícies

    surfaceVariant = LightCamel.copy(alpha = 0.6f), // Para variantes de superfície, como contornos de campos de texto ou fundos de chip
    onSurfaceVariant = TaupeGray,       // Texto/ícones sobre surfaceVariant

    outline = TaupeGray.copy(alpha = 0.5f), // Para outlines (bordas)
    outlineVariant = TaupeGray.copy(alpha = 0.2f), // Para outlines mais sutis

    scrim = Color.Black.copy(alpha = 0.4f), // Para escurecer o fundo atrás de dialogs/sheets

    inverseSurface = DarkChocolate,         // Para elementos que precisam de contraste invertido
    inverseOnSurface = WarmWhite,
    inversePrimary = LightCamel,            // Primária invertida
    surfaceTint = RichGold                  // Cor de tonalidade aplicada a superfícies elevadas
)

// --- Novo Dark Theme Color Scheme (Complementar à paleta Camel/Amarelo) ---
private val NewDarkColorScheme = darkColorScheme(
    primary = SoftMustard,              // Amarelo mais suave para o tema escuro
    onPrimary = DarkChocolate,          // Texto sobre a primária
    primaryContainer = DeepCaramel.copy(alpha = 0.6f), // Um camel escuro para container
    onPrimaryContainer = PaleButterYellow,

    secondary = LightCamel,             // Camel claro como secundário
    onSecondary = DarkChocolate,
    secondaryContainer = DarkChocolate.copy(alpha = 0.5f),
    onSecondaryContainer = PaleButterYellow,

    tertiary = AmberYellow.copy(alpha = 0.8f), // Âmbar mais sutil
    onTertiary = DarkChocolate,
    tertiaryContainer = DarkChocolate.copy(alpha = 0.3f),
    onTertiaryContainer = SoftMustard,

    error = MutedRed.copy(alpha = 0.9f),
    onError = WarmWhite,
    errorContainer = MutedRed.copy(alpha = 0.3f),
    onErrorContainer = PaleButterYellow,

    background = Color(0xFF201A10),     // Um marrom muito escuro, quase preto, com tom quente
    onBackground = PaleButterYellow,    // Texto claro sobre fundo escuro

    surface = Color(0xFF2D251A),        // Superfície um pouco mais clara que o fundo, também quente
    onSurface = AlmondMilk,

    surfaceVariant = TaupeGray.copy(alpha = 0.4f),
    onSurfaceVariant = LightCamel.copy(alpha = 0.8f),

    outline = TaupeGray.copy(alpha = 0.7f),
    outlineVariant = TaupeGray.copy(alpha = 0.4f),

    scrim = Color.Black.copy(alpha = 0.5f),

    inverseSurface = PaleButterYellow,
    inverseOnSurface = DarkChocolate,
    inversePrimary = RichGold,
    surfaceTint = SoftMustard
)

@Composable
fun SweetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> NewDarkColorScheme
        else -> NewLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

