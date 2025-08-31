package com.cmu.sweet.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Material typography styles for SweetTheme
val Typography = Typography(
    // Títulos de Display (para telas de destaque, onboarding, etc. - menos comum em apps utilitários)
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default, // Ou sua fonte personalizada ex: montserratFamily
        fontWeight = FontWeight.Normal, // Pode ser Light ou Regular para display
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Títulos de Cabeçalho (para títulos de seção importantes)
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold, // Um pouco mais de peso para destaque
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Um pouco menos de peso que os headlines maiores
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Títulos (para barras de apps, títulos de diálogo, títulos de card)
    titleLarge = TextStyle( // O que você tinha como titleLarge
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold, // Mantendo Bold para títulos de barras/cards
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle( // O que você tinha como titleMedium
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold, // Bom para subtítulos ou ênfase média
        fontSize = 18.sp, // Aumentei um pouco para diferenciar mais do bodyLarge
        lineHeight = 26.sp, // Ajuste de lineHeight
        letterSpacing = 0.15.sp // Pequeno ajuste no letterSpacing
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Corpo do Texto (para a maior parte do texto de leitura)
    bodyLarge = TextStyle( // O que você tinha como bodyLarge
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp // Pode reduzir para 0.15.sp ou 0.25.sp se preferir menos espaçamento
    ),
    bodyMedium = TextStyle( // O que você tinha como bodyMedium
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Rótulos (para botões, legendas, texto sobre imagens)
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle( // O que você tinha como labelSmall
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Pode ser FontWeight.SemiBold se quiser rótulos pequenos mais fortes
        fontSize = 11.sp, // Ligeiramente menor para diferenciar mais do bodySmall
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

