package com.kalidorabr.bancodeminutagens.ui.temaa

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import com.kalidorabr.bancodeminutagens.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tema A — escuro minimalista (canvas "Telas").
 * Valores tirados direto do canvas: fundo #0B0B0D, cartões #141417 / #17171B,
 * vermelho #E5484D (vem da cor escolhida em Ajustes), fontes Manrope + JetBrains Mono.
 */
/**
 * Qual tema novo está desenhando agora: 0 = A, 1 = B, 2 = C, 3 = D.
 * Definido ao abrir o app e ao trocar o tema (antes de redesenhar).
 */
object TemaVisual {
    private val estado = androidx.compose.runtime.mutableIntStateOf(0)
    var modo: Int
        get() = estado.intValue
        set(v) { estado.intValue = v }
    val b: Boolean get() = modo == 1
    val c: Boolean get() = modo == 2
    val d: Boolean get() = modo == 3

    fun de(m: com.kalidorabr.bancodeminutagens.data.VisualMode): Int = when (m) {
        com.kalidorabr.bancodeminutagens.data.VisualMode.TEMA_B -> 1
        com.kalidorabr.bancodeminutagens.data.VisualMode.TEMA_C -> 2
        com.kalidorabr.bancodeminutagens.data.VisualMode.TEMA_D -> 3
        else -> 0
    }

    // Tema B: true enquanto a tela da Série está aberta. A imagem passa por trás do relógio
    // e do Wi-Fi (sem a faixa escura) e os ícones seguem o brilho da imagem, como no Tema A.
    private val semFaixaEstado = androidx.compose.runtime.mutableStateOf(false)
    var semFaixa: Boolean
        get() = semFaixaEstado.value
        set(v) { semFaixaEstado.value = v }
}

object TemaATokens {
    // Fontes variáveis (res/font): um arquivo cobre todos os pesos.
    @OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
    private fun variavel(res: Int, vararg pesos: FontWeight) = FontFamily(
        pesos.map { w ->
            Font(res, weight = w, variationSettings = FontVariation.Settings(FontVariation.weight(w.weight)))
        }
    )
    private val Manrope: FontFamily = variavel(
        R.font.manrope, FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold
    )
    private val Archivo: FontFamily = variavel(
        R.font.archivo, FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold,
        FontWeight.ExtraBold, FontWeight.Black
    )
    /** Tema A usa Manrope; Tema B usa Archivo (como no canvas). */
    private val Figtree: FontFamily = variavel(
        R.font.figtree, FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold,
        FontWeight.ExtraBold, FontWeight.Black
    )
    private val Sora: FontFamily = variavel(
        R.font.sora, FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold
    )
    /** A: Manrope · B: Archivo · C: Figtree · D: Sora. */
    val FontSans: FontFamily get() = when (TemaVisual.modo) {
        1 -> Archivo
        2 -> Figtree
        3 -> Sora
        else -> Manrope
    }
    val FontMono: FontFamily = variavel(
        R.font.jetbrains_mono, FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold
    )

    // Espaçamentos
    val SideMargin = 20.dp
    val SpaceSmall = 4.dp
    val SpaceStandard = 8.dp
    val BlockGap = 10.dp
    val SectionGap = 18.dp
    val SpaceWide = 22.dp

    // Cantos
    val CornerButtonField = 14.dp
    val CornerCardCommon = 18.dp
    val CornerCardFeatured = 24.dp
    val CornerPill = CircleShape
    val ShapeButtonField = RoundedCornerShape(CornerButtonField)
    val ShapeCardCommon = RoundedCornerShape(CornerCardCommon)
    val ShapeCardFeatured = RoundedCornerShape(CornerCardFeatured)
    /** C usa botões em pílula; os outros, cantos de 16. */
    val ShapePrimaryButton get() = if (TemaVisual.c) RoundedCornerShape(50) else RoundedCornerShape(16.dp)

    // Alturas
    val HeightTopBar = 56.dp
    val SizeHeaderButton = 44.dp
    val HeightPrimaryButton = 54.dp
    val HeightSecondaryButton = 46.dp
    val HeightBottomNav = 64.dp
    val HeightSearchField = 48.dp
    val HeightChip = 36.dp

    // Tipografia
    val TitleMain = TextStyle(
        fontFamily = FontSans,
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.5).sp
    )
    val TitleSection = TextStyle(
        fontFamily = FontSans,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold
    )
    val TitleCard = TextStyle(
        fontFamily = FontSans,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold
    )
    val TextBody = TextStyle(
        fontFamily = FontSans,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 21.sp
    )
    val TextAuxiliary = TextStyle(
        fontFamily = FontSans,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    )
    val TextTag = TextStyle(
        fontFamily = FontSans,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
    )
    val TextOverline = TextStyle(
        fontFamily = FontSans,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
    val TextNumber = TextStyle(
        fontFamily = FontMono,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold
    )

    // Cores: cada uma tem a versão de cada tema (A escuro, B claro e quente, C grafite streaming, D índigo neon).
    // Elas leem TemaVisual.modo, então mudam na hora quando o tema é trocado.
    private fun c(a: Long, bb: Long, cc: Long = a, dd: Long = a) = Color(
        when (TemaVisual.modo) { 1 -> bb; 2 -> cc; 3 -> dd; else -> a }
    )

    val DarkBackground get() = c(0xFF0B0B0D, 0xFFF4F1EC, 0xFF16171B, 0xFF0B0918)
    val DarkGraphiteSurface get() = c(0xFF141417, 0xFFFFFFFF, 0xFF222329, 0xFF151129)
    val DarkCardSurface get() = c(0xFF17171B, 0xFFFFFFFF, 0xFF222329, 0xFF151129)
    val DarkBorder get() = c(0xFF2A2A30, 0xFFE4DDD3, 0xFF33343C, 0xFF2E2758)
    val SubtleBorder get() = c(0xFF34343B, 0xFFD8D1C7, 0xFF3A3B44, 0xFF3A3170)
    val Track get() = c(0xFF26262C, 0xFFECE7DF, 0xFF2C2D34, 0xFF211A40)
    val NavSurface get() = c(0xEB1C1C21, 0xFF1A1A1F, 0xEB16171B, 0xE6151129)

    val TextWhite get() = c(0xFFF2F2F3, 0xFF1A1A1F, 0xFFFFFFFF, 0xFFFFFFFF)
    val TextGray get() = c(0xFF9A9AA3, 0xFF6E6873, 0xFFB4B5BD, 0xFFC4BEE6)
    val TextSoft get() = c(0xFFC9C9CF, 0xFF4A454F, 0xFFD0D1D8, 0xFFDCD6FF)
    val TextMuted get() = c(0xFF6B6B73, 0xFF8A8390, 0xFF8A8B93, 0xFF8C84B0)

    // Cores extras (antes fixas dentro das telas)
    val ErroTexto get() = c(0xFFFF8A8E, 0xFFB0182B, 0xFFFF8A8E, 0xFFFF8FB0)
    val ErroTextoForte get() = c(0xFFFFB3B5, 0xFF8E1424, 0xFFFFB3B5, 0xFFFFC2D4)
    val ErroSub get() = c(0xFFC9A0A2, 0xFF9A5A62)
    val ErroFundo get() = c(0xFF2A1416, 0xFFFCE4E7, 0xFF2A1416, 0xFF2A1430)
    val Perigo get() = c(0xFFFF6B70, 0xFFC21F33, 0xFFFF6B70, 0xFFFF6B8B)
    val PerigoFundo get() = c(0xFF241315, 0xFFFBE3E6, 0xFF241315, 0xFF2A1430)
    val Neutro get() = c(0xFF1F1F24, 0xFFEFEAE2, 0xFF2C2D34, 0xFF211A40)
    val TextoCorpo get() = c(0xFFA8A8B0, 0xFF5E5850, 0xFFB4B5BD, 0xFFC4BEE6)
    val TextoApagado get() = c(0xFF8A8A93, 0xFF8A8390, 0xFF8A8B93, 0xFF8C84B0)
    val Desativado get() = c(0xFF4A4A52, 0xFFC9C2B8, 0xFF4A4B52, 0xFF3A3466)
    val InfoTexto get() = c(0xFF8DB1FF, 0xFF1F4FB8)
    val InfoFundo get() = c(0xFF15213A, 0xFFDCE7FF)

    // Cores de destaque (mesma ordem das opções do app). O Tema B usa tons um pouco mais firmes.
    fun accentFor(opcao: com.kalidorabr.bancodeminutagens.ui.theme.AppThemeOpcao): Color = when (opcao) {
        com.kalidorabr.bancodeminutagens.ui.theme.AppThemeOpcao.VERMELHO -> c(0xFFE5484D, 0xFFD7263D, 0xFFF0233B, 0xFFF0233B)
        com.kalidorabr.bancodeminutagens.ui.theme.AppThemeOpcao.ROXO -> c(0xFF8E6CF0, 0xFF7A4FE0, 0xFF8E6CF0, 0xFF7C4DFF)
        com.kalidorabr.bancodeminutagens.ui.theme.AppThemeOpcao.AZUL -> c(0xFF3E7BFA, 0xFF2F6BF0, 0xFF3E7BFA, 0xFF3E7BFA)
        com.kalidorabr.bancodeminutagens.ui.theme.AppThemeOpcao.VERDE_ESMERALDA -> c(0xFF2FB57A, 0xFF1F9D68, 0xFF2BD07F, 0xFF2BD07F)
        com.kalidorabr.bancodeminutagens.ui.theme.AppThemeOpcao.LARANJA -> c(0xFFF07A2E, 0xFFE8692A, 0xFFFF7A1A, 0xFFFF7A1A)
    }

    // Cores semânticas
    val SemanticSuccess get() = c(0xFF3BB273, 0xFF1F9D68, 0xFF2BD07F, 0xFF34E3A0)
    val SuccessText get() = c(0xFF6FD99A, 0xFF1B7A4B, 0xFF2BD07F, 0xFF34E3A0)
    val SuccessBg get() = c(0xFF173A27, 0xFFDDF1E6, 0xFF0F3A27, 0xFF0F3330)
    val SemanticWarning get() = c(0xFFF5C04A, 0xFF7A5300, 0xFFFFC34D, 0xFFFFB547)
    val WarningBg get() = c(0xFF3A2E12, 0xFFFBEBC4, 0xFF3A2E12, 0xFF3A2A10)
    val SemanticPending get() = c(0xFF6B6B73, 0xFF8A8390, 0xFF8A8B93, 0xFF8C84B0)
    val SemanticError get() = c(0xFFE5484D, 0xFFD7263D, 0xFFF0233B, 0xFFFF6B8B)

    // O Tema A não usa vidro/gradiente: superfícies sólidas.
    val GlassCardBackground: Brush get() = Brush.verticalGradient(listOf(DarkGraphiteSurface, DarkGraphiteSurface))
    val GlassCardBorder: Brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
    val GlassSurfaceBackground get() = DarkCardSurface
    val GlassSurfaceBorder: Brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
}

/** Aplica Manrope em toda a tipografia quando o Tema A está ativo. */
@androidx.compose.runtime.Composable
fun TemaATypographyProvider(
    selectedTheme: com.kalidorabr.bancodeminutagens.ui.theme.AppThemeOpcao,
    content: @androidx.compose.runtime.Composable () -> Unit
) {
    val base = androidx.compose.material3.MaterialTheme.typography
    val f = TemaATokens.FontSans
    val t = base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = f), displayMedium = base.displayMedium.copy(fontFamily = f),
        displaySmall = base.displaySmall.copy(fontFamily = f), headlineLarge = base.headlineLarge.copy(fontFamily = f),
        headlineMedium = base.headlineMedium.copy(fontFamily = f), headlineSmall = base.headlineSmall.copy(fontFamily = f),
        titleLarge = base.titleLarge.copy(fontFamily = f), titleMedium = base.titleMedium.copy(fontFamily = f),
        titleSmall = base.titleSmall.copy(fontFamily = f), bodyLarge = base.bodyLarge.copy(fontFamily = f),
        bodyMedium = base.bodyMedium.copy(fontFamily = f), bodySmall = base.bodySmall.copy(fontFamily = f),
        labelLarge = base.labelLarge.copy(fontFamily = f), labelMedium = base.labelMedium.copy(fontFamily = f),
        labelSmall = base.labelSmall.copy(fontFamily = f)
    )
    androidx.compose.material3.MaterialTheme(
        colorScheme = androidx.compose.material3.MaterialTheme.colorScheme.copy(
            primary = TemaATokens.accentFor(selectedTheme),
            secondary = TemaATokens.accentFor(selectedTheme)
        ),
        typography = t
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalTextStyle provides t.bodyLarge, content = content
        )
    }
}
