package com.kalidorabr.bancodeminutagens.ui.theme

// Cores usadas por algumas telas do visual Lite (vieram do antigo visual Full, removido).

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object FullTokens {
    // Espaçamentos
    val SideMargin = 16.dp
    val SpaceSmall = 4.dp
    val SpaceStandard = 8.dp
    val BlockGap = 12.dp
    val SectionGap = 16.dp
    val SpaceWide = 24.dp

    // Cantos (Radii)
    val CornerButtonField = 12.dp
    val CornerCardCommon = 16.dp
    val CornerCardFeatured = 20.dp
    val CornerPill = CircleShape
    val ShapeButtonField = RoundedCornerShape(CornerButtonField)
    val ShapeCardCommon = RoundedCornerShape(CornerCardCommon)
    val ShapeCardFeatured = RoundedCornerShape(CornerCardFeatured)

    // Alturas
    val HeightTopBar = 56.dp
    val SizeHeaderButton = 40.dp
    val HeightPrimaryButton = 48.dp
    val HeightSecondaryButton = 44.dp
    val HeightBottomNav = 68.dp
    val HeightSearchField = 48.dp

    // Tipografia
    val TitleMain = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
    )
    val TitleSection = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
    val TitleCard = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )
    val TextBody = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    )
    val TextAuxiliary = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    )
    val TextTag = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
    )

    // Cores
    val DarkBackground = Color(0xFF0D0D0E)
    val DarkGraphiteSurface = Color(0xFF181818)
    val DarkCardSurface = Color(0xFF222222)
    val DarkBorder = Color(0xFF2A2A2A)
    val SubtleBorder = Color(0xFF333333)

    val TextWhite = Color(0xFFFFFFFF)
    val TextGray = Color(0xFFB0B0B0)
    val TextMuted = Color(0xFF757575)

    // Cores Semânticas
    val SemanticSuccess = Color(0xFF00D26A)
    val SemanticWarning = Color(0xFFFFB800)
    val SemanticPending = Color(0xFF757575)
    val SemanticError = Color(0xFFE92035)

    // Efeito Glassmorphism (Sutil, escuro e sofisticado)
    val GlassCardBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xEE16161C),
            Color(0xFF111114)
        )
    )
    val GlassCardBorder = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.10f),
            Color.White.copy(alpha = 0.02f)
        )
    )
    val GlassSurfaceBackground = Color(0xCC161619)
    val GlassSurfaceBorder = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.08f),
            Color.White.copy(alpha = 0.02f)
        )
    )
}
