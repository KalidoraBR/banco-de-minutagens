package com.kalidorabr.bancodeminutagens.ui.theme

import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF080808)
val SurfaceDark = Color(0xFF111113)
val SurfaceCard = Color(0xFF18181B)
val RedPrimary = Color(0xFFE92035)
val StatusGreen = Color(0xFF00D26A)
val StatusYellow = Color(0xFFFFB800)
val StatusBlue = Color(0xFF2196F3)
val StatusPurple = Color(0xFF9042F5)
val BorderColor = Color(0xFF2A2A2E)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B0B0)

enum class AppThemeOpcao(
    val id: String,
    val nome: String,
    val primaryColor: Color
) {
    VERMELHO("VERMELHO", "Vermelho", Color(0xFFE92035)),
    ROXO("ROXO", "Roxo", Color(0xFFA855F7)),
    AZUL("AZUL", "Azul", Color(0xFF2563EB)),
    VERDE_ESMERALDA("VERDE_ESMERALDA", "Verde-esmeralda", Color(0xFF10B981)),
    LARANJA("LARANJA", "Laranja", Color(0xFFF97316));

    companion object {
        fun fromId(id: String?): AppThemeOpcao {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: VERMELHO
        }
    }
}
