package com.kalidorabr.bancodeminutagens.ui.temaa.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.kalidorabr.bancodeminutagens.ui.temab.cliqueTemaB
import com.kalidorabr.bancodeminutagens.ui.temac.cliqueTemaC
import com.kalidorabr.bancodeminutagens.ui.temad.cliqueTemaD
import androidx.compose.animation.core.animateFloatAsState

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/** Curva suave de "desaceleração" usada nas transições do Tema A. */
val EasingTemaA = CubicBezierEasing(0.2f, 0f, 0f, 1f)

/**
 * Anima a entrada da tela sempre que [chave] muda.
 * [profundidade] decide o sentido: indo mais fundo (Série → Temporada) a tela entra
 * pela direita; voltando, entra pela esquerda. Mesma profundidade (abas) só esmaece
 * com um leve movimento para cima.
 * Não recria a tela: só mexe na posição e na transparência, então nada é perdido.
 */
@Composable
fun Modifier.transicaoTemaA(
    chave: Any?,
    profundidade: Int,
    ativo: Boolean = true,
    animarEntrada: Boolean = false
): Modifier {
    if (!ativo) return this
    if (com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual.b && profundidade == 0) return this
    val progresso = remember { Animatable(if (animarEntrada) 0f else 1f) }
    val anterior = remember { intArrayOf(profundidade) }
    // Tema B: trocando de aba, o conteúdo desliza para o lado da aba escolhida
    val chaveAnterior = remember { arrayOf<Any?>(chave) }
    val ladoB = remember { floatArrayOf(0f) }
    val sentido = remember { floatArrayOf(0f) }
    val primeira = remember { booleanArrayOf(true) }
    val distancia = with(LocalDensity.current) { 56.dp.toPx() }
    val subida = with(LocalDensity.current) { 14.dp.toPx() }

    LaunchedEffect(chave) {
        if (primeira[0]) {
            primeira[0] = false
            if (animarEntrada) progresso.animateTo(1f, tween(280, easing = EasingTemaA))
            return@LaunchedEffect
        }
        val ca = chaveAnterior[0]
        ladoB[0] = if (chave is Int && ca is Int) (if (chave > ca) 1f else -1f) else 0f
        chaveAnterior[0] = chave
        sentido[0] = when {
            profundidade > anterior[0] -> 1f
            profundidade < anterior[0] -> -1f
            else -> 0f
        }
        anterior[0] = profundidade
        progresso.snapTo(0f)
        launch { progresso.animateTo(1f, tween(320, easing = EasingTemaA)) }
    }

    return this.graphicsLayer {
        val p = progresso.value
        alpha = 0.2f + 0.8f * p
        val tv = com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual
        if (sentido[0] == 0f && (tv.c || tv.d)) {
            val z = if (tv.c) 1f + 0.05f * (1f - p) else 0.92f + 0.08f * p
            scaleX = z; scaleY = z
        } else if (sentido[0] == 0f && tv.b && ladoB[0] != 0f) {
            translationX = (1f - p) * distancia * 0.6f * ladoB[0]
        } else if (sentido[0] == 0f) {
            translationY = (1f - p) * subida
        } else {
            translationX = (1f - p) * distancia * sentido[0]
        }
    }
}

/** Qual tela está aberta: série, temporada e episódio (nulos = mais acima). */
data class NavDestinoTemaA(val serieId: String?, val temporadaId: String?, val episodioId: String?) {
    val profundidade: Int
        get() = when {
            episodioId != null -> 3
            temporadaId != null -> 2
            serieId != null -> 1
            else -> 0
        }
}

/** Lê o valor "congelado" da tela e escreve no estado real da navegação. */
class CampoNavTemaA<T>(private val valor: T, private val definir: (T) -> Unit) {
    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T = valor
    operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, novo: T) = definir(novo)
}

/**
 * Transição completa: indo mais fundo, a nova tela entra pela direita por cima da atual,
 * que recua um pouco e escurece. Voltando, o contrário. Mesma profundidade (ex.: próximo
 * episódio) faz um esmaecer cruzado.
 */
fun androidx.compose.animation.AnimatedContentTransitionScope<NavDestinoTemaA>.transicaoNavTemaA(
    de: NavDestinoTemaA,
    para: NavDestinoTemaA
): androidx.compose.animation.ContentTransform {
    val dur = 340
    val entrar = tween<androidx.compose.ui.unit.IntOffset>(dur, easing = EasingTemaA)
    val fade = tween<Float>(dur, easing = EasingTemaA)
    return when {
        para.profundidade > de.profundidade ->
            (androidx.compose.animation.slideInHorizontally(entrar) { it } + androidx.compose.animation.fadeIn(tween(dur / 2))) togetherWith
                (androidx.compose.animation.slideOutHorizontally(entrar) { -it / 4 } + androidx.compose.animation.fadeOut(fade, targetAlpha = 0.3f))
        para.profundidade < de.profundidade ->
            (androidx.compose.animation.slideInHorizontally(entrar) { -it / 4 } + androidx.compose.animation.fadeIn(fade, initialAlpha = 0.3f)) togetherWith
                (androidx.compose.animation.slideOutHorizontally(entrar) { it } + androidx.compose.animation.fadeOut(tween(dur / 2, delayMillis = dur / 2)))
        else ->
            androidx.compose.animation.fadeIn(tween(220, delayMillis = 60)) togetherWith androidx.compose.animation.fadeOut(tween(160))
    }.apply {
        // A tela que entra fica por cima quando vamos mais fundo
        targetContentZIndex = para.profundidade.toFloat()
    }
}

private infix fun androidx.compose.animation.EnterTransition.togetherWith(exit: androidx.compose.animation.ExitTransition) =
    androidx.compose.animation.ContentTransform(this, exit)

/** Escopos da animação compartilhada (pôster que "voa" entre Início e Série). */
@OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
val LocalSharedTemaA = androidx.compose.runtime.compositionLocalOf<androidx.compose.animation.SharedTransitionScope?> { null }
val LocalAnimScopeTemaA = androidx.compose.runtime.compositionLocalOf<androidx.compose.animation.AnimatedVisibilityScope?> { null }

/**
 * Marca o pôster da série para "voar" entre telas. Fora do Tema A (sem escopo) não faz nada.
 */
@OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.posterCompartilhadoTemaA(serieId: String): Modifier {
    val shared = LocalSharedTemaA.current ?: return this
    val anim = LocalAnimScopeTemaA.current ?: return this
    return with(shared) {
        this@posterCompartilhadoTemaA.sharedElement(
            sharedContentState = rememberSharedContentState(key = "poster-$serieId"),
            animatedVisibilityScope = anim,
            boundsTransform = { _, _ -> tween(380, easing = EasingTemaA) }
        )
    }
}

/**
 * Clique do Tema A: o item "afunda" levemente (97%) enquanto o dedo está em cima.
 * [vibrar] dá uma vibração curta de confirmação (usado nos botões de salvar).
 */
@Composable
fun Modifier.cliqueTemaA(
    enabled: Boolean = true,
    vibrar: Boolean = false,
    onClick: () -> Unit
): Modifier {
    // Tema B tem o próprio toque (aperta para os lados + círculo de cor)
    if (com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual.b) {
        return cliqueTemaB(enabled, vibrar, onClick)
    }
    if (com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual.c) return cliqueTemaC(enabled, vibrar, onClick)
    if (com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual.d) return cliqueTemaD(enabled, vibrar, onClick)
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()
    val escala by animateFloatAsState(
        targetValue = if (pressionado && enabled) 0.97f else 1f,
        animationSpec = tween(if (pressionado) 90 else 160, easing = EasingTemaA),
        label = "cliqueTemaA"
    )
    val haptico = LocalHapticFeedback.current
    return this
        .graphicsLayer { scaleX = escala; scaleY = escala }
        .clickable(interactionSource = interacao, indication = null, enabled = enabled) {
            if (vibrar) haptico.performHapticFeedback(HapticFeedbackType.Confirm)
            onClick()
        }
}
