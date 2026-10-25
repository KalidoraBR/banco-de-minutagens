package com.kalidorabr.bancodeminutagens.ui.temas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.kalidorabr.bancodeminutagens.data.VisualMode
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaATokens
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaATypographyProvider
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual
import androidx.compose.foundation.layout.fillMaxSize
import com.kalidorabr.bancodeminutagens.ui.theme.AppThemeOpcao

/*
 * BASE DOS TEMAS NOVOS (A, B, C e D)
 *
 * Como o app está organizado:
 *  - MainActivity.kt: a LÓGICA (dados, salvar, sincronizar, navegar). Igual para todos os temas.
 *  - ui/temas/TelasTema.kt: o "balcão" — cada Tela... recebe dados e ações e escolhe o desenho do tema ativo.
 *  - ui/temaa/, ui/temab/, ...: o VISUAL de cada tema (telas, cores, fontes, animações).
 *
 * Para criar o desenho de uma tela do Tema B:
 *  1. Crie a tela em ui/temab/ com os MESMOS parâmetros da versão do Tema A.
 *  2. Em TelasTema.kt, na função correspondente, acrescente: VisualMode.TEMA_B -> MinhaTelaTemaB(...)
 *  3. Quando todas as telas estiverem prontas, mude "pronto" do TEMA_B para true em AppThemePreferences.kt.
 */

/** Tema ativo, disponível para qualquer tela sem precisar passar parâmetro. */
val LocalModoVisual = staticCompositionLocalOf { VisualMode.TEMA_A }

/** Cores-base de cada tema novo (as telas de cada tema podem ter mais detalhes próprios). */
data class PaletaTema(
    val fundo: Color,
    val superficie: Color,
    val texto: Color,
    val textoSecundario: Color,
    val destaque: Color,
    val escuro: Boolean
)

object Paletas {
    // get(): lido na hora (as cores do A dependem do tema ativo e não podem ficar "guardadas")
    val A get() = PaletaTema(
        fundo = TemaATokens.DarkBackground,
        superficie = TemaATokens.DarkGraphiteSurface,
        texto = TemaATokens.TextWhite,
        textoSecundario = TemaATokens.TextGray,
        destaque = Color(0xFFE5484D),
        escuro = true
    )

    /** Tema B (canvas): claro e quente, cabeçalho escuro, cards brancos, vermelho #D7263D, títulos Archivo. */
    val B = PaletaTema(
        fundo = Color(0xFFF4F1EC),
        superficie = Color(0xFFFFFFFF),
        texto = Color(0xFF17140F),
        textoSecundario = Color(0xFF6E665C),
        destaque = Color(0xFFD7263D),
        escuro = false
    )

    /** Temas C e D ainda não foram desenhados: usam a base do A até lá. */
    val C get() = A
    val D get() = A

    fun de(modo: VisualMode): PaletaTema = when (modo) {
        VisualMode.TEMA_B -> B
        VisualMode.TEMA_C -> C
        VisualMode.TEMA_D -> D
        else -> A
    }
}

/**
 * Envolve o app quando um tema novo está ativo: informa o tema às telas e aplica as fontes.
 * Cada tema pode ter o seu provedor; B, C e D usam o do A enquanto não têm o próprio.
 */
@Composable
fun ProvedorTemaNovo(modo: VisualMode, corDestaque: AppThemeOpcao, content: @Composable () -> Unit) {
    // Avisa as cores compartilhadas (TemaATokens) qual tema está ativo
    val m = TemaVisual.de(modo)
    if (TemaVisual.modo != m) TemaVisual.modo = m
    CompositionLocalProvider(LocalModoVisual provides modo) {
        when (modo) {
            VisualMode.TEMA_B -> TemaATypographyProvider(corDestaque) {
                content()
            }
            else -> TemaATypographyProvider(corDestaque, content)
        }
    }
}
