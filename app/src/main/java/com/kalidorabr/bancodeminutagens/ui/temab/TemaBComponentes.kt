package com.kalidorabr.bancodeminutagens.ui.temab

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaATokens

/*
 * TEMA B — peças visuais e movimentos próprios.
 * Estética "papel e camadas": as coisas deslizam, se desdobram e se encaixam,
 * com parada firme (0,25–0,35 s) em vez de flutuar como no Tema A.
 */

/** Parada firme e decidida. */
val EasingFirmeB = CubicBezierEasing(0.2f, 0.9f, 0.25f, 1f)
/** Passa um pouco do ponto e volta: o cabeçalho "respira". */
val EasingRespiraB = CubicBezierEasing(0.3f, 1.35f, 0.5f, 1f)
/** Pequeno quique ao parar (pôster, pop-ups, cards). */
val EasingQuiqueB = CubicBezierEasing(0.34f, 1.5f, 0.55f, 1f)

/** Fundo escuro do cabeçalho do Tema B. */
val EscuroCabecalhoB = Color(0xFF221F27)
val BarraEscuraB = Color(0xFF1A1A1F)
val TextoSobreEscuroB = Color(0xFFB9B3BE)

/** Faixa escura atrás da barra de status (ícones brancos sempre legíveis). */
@Composable
fun FaixaStatusTemaB() {
    val semFaixa = com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual.semFaixa
    val alpha by animateFloatAsState(if (semFaixa) 0f else 1f, tween(220), label = "faixaB")
    if (alpha == 0f) return
    Box(
        Modifier.graphicsLayer { this.alpha = alpha }.then(
        Modifier
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.statusBars)
            .background(EscuroCabecalhoB)
    ))
}

/** Forma com a base em curva suave (o "papel dobrado" do cabeçalho). */
class BaseCurvaB(private val curva: Dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val c = with(density) { curva.toPx() }.coerceAtMost(size.height / 2)
        val p = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - c)
            quadraticTo(size.width / 2f, size.height + c, 0f, size.height - c)
            close()
        }
        return Outline.Generic(p)
    }
}

/**
 * Faz o item "vazar" o espaçamento do pai: ocupa a largura toda e sobe [topo].
 * Usado para o cabeçalho escuro encostar nas bordas mesmo dentro de telas com margem.
 */
fun Modifier.sangrarB(lateral: Dp, topo: Dp = 0.dp): Modifier = layout { m, c ->
    val l = lateral.roundToPx()
    val t = topo.roundToPx()
    val largura = if (c.hasBoundedWidth) c.maxWidth + 2 * l else c.maxWidth
    val p = m.measure(c.copy(minWidth = largura, maxWidth = largura))
    val w = if (c.hasBoundedWidth) c.maxWidth else p.width
    layout(w, (p.height - t).coerceAtLeast(0)) { p.place(-l, -t) }
}

/**
 * Toque do Tema B: o botão "aperta" para os lados (mais largo e mais baixo),
 * volta com um leve quique, e um círculo da cor de destaque se espalha do dedo.
 */
@Composable
fun Modifier.cliqueTemaB(
    enabled: Boolean = true,
    vibrar: Boolean = false,
    onClick: () -> Unit
): Modifier {
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()
    val p by animateFloatAsState(
        targetValue = if (pressionado && enabled) 1f else 0f,
        animationSpec = if (pressionado) tween(90) else spring(dampingRatio = 0.45f, stiffness = 700f),
        label = "cliqueTemaB"
    )
    val haptico = LocalHapticFeedback.current
    val accent = MaterialTheme.colorScheme.primary
    return this
        .graphicsLayer {
            scaleX = 1f + 0.025f * p
            scaleY = 1f - 0.06f * p
        }
        .clickable(
            interactionSource = interacao,
            indication = ripple(color = accent),
            enabled = enabled
        ) {
            haptico.performHapticFeedback(if (vibrar) HapticFeedbackType.Confirm else HapticFeedbackType.TextHandleMove)
            onClick()
        }
}

/**
 * Entrada em cascata: o item sobe [distancia] e aparece depois de [atrasoMs].
 * Usado no título da série (palavra por palavra), nos cards e na lista de episódios.
 */
@Composable
fun Modifier.entradaB(atrasoMs: Int, distancia: Dp = 22.dp, quique: Boolean = false): Modifier {
    val prog = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        prog.animateTo(1f, tween(if (quique) 340 else 300, delayMillis = atrasoMs, easing = if (quique) EasingQuiqueB else EasingFirmeB))
    }
    val d = with(LocalDensity.current) { distancia.toPx() }
    return this.graphicsLayer {
        val v = prog.value
        alpha = v.coerceIn(0f, 1f)
        translationY = (1f - v) * d
    }
}

/** Botão redondo transparente do cabeçalho escuro. */
@Composable
fun BotaoTopoB(icone: ImageVector, descricao: String, onClick: () -> Unit, ativo: Boolean = false, fundo: Color = Color.Transparent) {
    Box(
        Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (ativo) Color.White.copy(alpha = 0.14f) else fundo)
            .cliqueTemaB(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icone, descricao, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

/**
 * Cabeçalho escuro do Tema B: base curva, título em maiúsculas centralizado na linha
 * dos botões e legenda embaixo. Quando o título muda (ex.: troca de aba), ele desliza
 * um pouco para o lado.
 */
@Composable
fun CabecalhoCurvoB(
    titulo: String,
    legenda: String = "",
    modifier: Modifier = Modifier,
    esquerda: (@Composable () -> Unit)? = null,
    direita: (@Composable () -> Unit)? = null,
    extra: (@Composable ColumnScope.() -> Unit)? = null
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(BaseCurvaB(26.dp))
            .background(EscuroCabecalhoB)
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 30.dp)
    ) {
        Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.widthIn(min = 44.dp)) { esquerda?.invoke() }
            AnimatedContent(
                targetState = titulo to legenda,
                transitionSpec = {
                    (slideInHorizontally(tween(260, easing = EasingFirmeB)) { it / 6 } + fadeIn(tween(200))) togetherWith
                        (slideOutHorizontally(tween(200, easing = EasingFirmeB)) { -it / 6 } + fadeOut(tween(140)))
                },
                modifier = Modifier.weight(1f),
                label = "tituloB"
            ) { (t, l) ->
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (t.isNotBlank()) {
                        Text(
                            t.uppercase(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (l.isNotBlank()) {
                        Text(l, color = TextoSobreEscuroB, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                    }
                }
            }
            Box(Modifier.widthIn(min = 44.dp), contentAlignment = Alignment.CenterEnd) { direita?.invoke() }
        }
        extra?.invoke(this)
    }
}

/**
 * Filtros do Tema B: três botões iguais e um marcador escuro que escorrega até a aba
 * escolhida, esticando no meio do caminho (a borda da frente corre, a de trás vem depois).
 */
@Composable
fun FiltrosTemaB(
    opcoes: List<String>,
    rotulos: List<String>,
    selecionado: Int,
    onSelecionar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier.fillMaxWidth().height(44.dp)) {
        val n = opcoes.size.coerceAtLeast(1)
        val gap = 8.dp
        val w = (maxWidth - gap * (n - 1)) / n
        val inicioAlvo = (w + gap) * selecionado
        val fimAlvo = inicioAlvo + w
        val anterior = remember { intArrayOf(selecionado) }
        val indoDireita = selecionado >= anterior[0]
        anterior[0] = selecionado
        val rapido = spring<Dp>(dampingRatio = 0.72f, stiffness = 900f)
        val lento = spring<Dp>(dampingRatio = 0.62f, stiffness = 260f)
        val inicio by animateDpAsState(inicioAlvo, if (indoDireita) lento else rapido, label = "inicioPilula")
        val fim by animateDpAsState(fimAlvo, if (indoDireita) rapido else lento, label = "fimPilula")
        Box(
            Modifier
                .offset(x = inicio)
                .width((fim - inicio).coerceAtLeast(w / 2))
                .height(44.dp)
                .shadow(6.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(BarraEscuraB)
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            rotulos.forEachIndexed { i, r ->
                val on = i == selecionado
                val cor by androidx.compose.animation.animateColorAsState(
                    if (on) Color.White else TemaATokens.TextWhite, tween(220), label = "corFiltro"
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (on) Color.Transparent else TemaATokens.DarkCardSurface)
                        .cliqueTemaB { onSelecionar(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(r.uppercase(), color = cor, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, maxLines = 1)
                }
            }
        }
    }
}

/** Barra escura flutuante do Tema B: a aba ativa vira um botão vermelho com nome. */
@Composable
fun BarraAbasTemaB(
    itens: List<Pair<Painter, String>>,
    selecionado: Int,
    onSelecionar: (Int) -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        Modifier
            .fillMaxWidth()
            .background(TemaATokens.DarkBackground)
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 12.dp)
            .height(64.dp)
            .shadow(14.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(BarraEscuraB)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        itens.forEachIndexed { i, (icone, nome) ->
            val on = i == selecionado
            Row(
                Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (on) accent else Color.Transparent)
                    .cliqueTemaB { onSelecionar(i) }
                    .animateContentSize(spring(dampingRatio = 0.7f, stiffness = 500f))
                    .padding(horizontal = if (on) 16.dp else 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icone, nome, tint = if (on) Color.White else TextoSobreEscuroB, modifier = Modifier.size(22.dp))
                if (on) {
                    Spacer(Modifier.width(8.dp))
                    Text(nome.uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
                }
            }
        }
    }
}


/**
 * "Carimbo": quando a [chave] muda (ex.: a seção foi preenchida com "Igual ao anterior"),
 * o card cresce um pouco, ganha borda e fundo vermelhos e assenta de volta.
 */
@Composable
fun Modifier.carimboB(chave: Any?): Modifier {
    val prog = remember { Animatable(0f) }
    val primeira = remember { booleanArrayOf(true) }
    LaunchedEffect(chave) {
        if (primeira[0]) { primeira[0] = false; return@LaunchedEffect }
        prog.snapTo(1f)
        prog.animateTo(0f, tween(520, delayMillis = 60, easing = EasingFirmeB))
    }
    val accent = MaterialTheme.colorScheme.primary
    val forma = RoundedCornerShape(18.dp)
    return this
        .graphicsLayer {
            val v = prog.value
            scaleX = 1f + 0.05f * v
            scaleY = 1f + 0.05f * v
        }
        .then(
            if (prog.value > 0.01f) Modifier
                .border(2.dp, accent.copy(alpha = prog.value), forma)
                .background(accent.copy(alpha = 0.12f * prog.value), forma)
            else Modifier
        )
}

/**
 * Transições de navegação do Tema B:
 *  - Início → Série: a série aparece rápido; o próprio cabeçalho se estica e "respira".
 *  - Série → Temporada: a página cresce a partir do card, como um papel se desdobrando.
 *  - → Episódio: sobe como uma folha por cima, com sombra.
 *  - Mesma profundidade (próximo episódio): vira a página para a esquerda.
 *  - Voltando: o contrário (a folha desce, a página encolhe).
 */
fun androidx.compose.animation.AnimatedContentTransitionScope<com.kalidorabr.bancodeminutagens.ui.temaa.components.NavDestinoTemaA>.transicaoNavTemaB(
    de: com.kalidorabr.bancodeminutagens.ui.temaa.components.NavDestinoTemaA,
    para: com.kalidorabr.bancodeminutagens.ui.temaa.components.NavDestinoTemaA
): androidx.compose.animation.ContentTransform {
    val firmeOff = tween<androidx.compose.ui.unit.IntOffset>(340, easing = EasingFirmeB)
    val volta = tween<androidx.compose.ui.unit.IntOffset>(280, easing = EasingFirmeB)
    val origemCard = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.62f)
    val t = when {
        // Abrindo a série a partir da Início
        para.profundidade == 1 && de.profundidade == 0 ->
            fadeIn(tween(140)) togetherWith fadeOut(tween(220, delayMillis = 120))
        // Abrindo a temporada: a página cresce do card
        para.profundidade == 2 && de.profundidade == 1 ->
            (androidx.compose.animation.scaleIn(tween(320, easing = EasingFirmeB), initialScale = 0.86f, transformOrigin = origemCard) + fadeIn(tween(180))) togetherWith
                fadeOut(tween(260, delayMillis = 80))
        // Abrindo o episódio: folha sobe
        para.profundidade == 3 && de.profundidade < 3 ->
            androidx.compose.animation.slideInVertically(firmeOff) { it } togetherWith
                (fadeOut(tween(340), targetAlpha = 0.55f) + androidx.compose.animation.scaleOut(tween(340), targetScale = 0.96f))
        // Voltando do episódio: folha desce
        de.profundidade == 3 && para.profundidade < 3 ->
            (fadeIn(tween(240), initialAlpha = 0.55f) + androidx.compose.animation.scaleIn(tween(280), initialScale = 0.96f)) togetherWith
                androidx.compose.animation.slideOutVertically(volta) { it }
        // Voltando da temporada: a página encolhe para dentro do card
        de.profundidade == 2 && para.profundidade == 1 ->
            fadeIn(tween(200)) togetherWith
                (androidx.compose.animation.scaleOut(tween(260, easing = EasingFirmeB), targetScale = 0.86f, transformOrigin = origemCard) + fadeOut(tween(200, delayMillis = 60)))
        // Trocar de episódio (mesma profundidade): pilha de papel.
        // A folha nova desliza por cima, inteira; a atual recua um pouco, encolhe e escurece,
        // como se ficasse por baixo da pilha. "Anterior" faz o mesmo no sentido contrário.
        para.profundidade == de.profundidade && para.profundidade == 3 -> {
            val lado = if (DirecaoEpisodioB.anterior) -1 else 1
            val folha = tween<androidx.compose.ui.unit.IntOffset>(480, easing = EasingFirmeB)
            (slideInHorizontally(folha) { it * lado } + fadeIn(tween(200))) togetherWith
                (slideOutHorizontally(tween(480, easing = EasingFirmeB)) { -it * lado / 4 } +
                    androidx.compose.animation.scaleOut(tween(480, easing = EasingFirmeB), targetScale = 0.9f) +
                    fadeOut(tween(420, easing = EasingFirmeB), targetAlpha = 0.25f))
        }
        para.profundidade < de.profundidade ->
            fadeIn(tween(220)) togetherWith (slideOutHorizontally(volta) { it / 5 } + fadeOut(tween(220)))
        else -> fadeIn(tween(220, delayMillis = 60)) togetherWith fadeOut(tween(160))
    }
    return t.apply {
        targetContentZIndex = when {
            para.profundidade == de.profundidade && para.profundidade == 3 -> 5f
            para.profundidade >= de.profundidade -> para.profundidade.toFloat()
            else -> -1f
        }
    }
}

/**
 * Pinta de escuro só a área atrás do relógio/Wi-Fi, como parte da própria tela.
 * Assim, nas transições, o topo se move junto com a tela (antes ficava "congelado").
 * Fora do Tema B não faz nada.
 */
@Composable
fun Modifier.topoEscuroB(): Modifier {
    if (!com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual.b) return this
    val insets = WindowInsets.statusBars
    val densidade = LocalDensity.current
    return this.drawBehind {
        val altura = insets.getTop(densidade).toFloat()
        drawRect(EscuroCabecalhoB, size = Size(size.width, altura))
    }
}

/** Sentido da última troca de episódio (true = foi para o anterior). Lido pela transição. */
object DirecaoEpisodioB {
    var anterior: Boolean = false
}

/**
 * Tema B nas abas: pinta o escuro atrás do relógio "por cima" do conteúdo da aba,
 * para ele deslizar e esmaecer junto com o cabeçalho na troca de aba (sem ficar congelado).
 */
@Composable
fun Modifier.faixaAcimaB(): Modifier {
    // Desativado: no Tema B a faixa do topo é fixa e a troca de aba não anima o cabeçalho
    if (true) return this
    val insets = WindowInsets.statusBars
    val densidade = LocalDensity.current
    return this.drawBehind {
        val altura = insets.getTop(densidade).toFloat()
        drawRect(EscuroCabecalhoB, topLeft = Offset(0f, -altura), size = Size(size.width, altura + 1f))
    }
}
