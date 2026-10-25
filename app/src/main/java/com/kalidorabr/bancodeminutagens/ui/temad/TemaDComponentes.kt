package com.kalidorabr.bancodeminutagens.ui.temad

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kalidorabr.bancodeminutagens.model.DestinoEpisodio
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.model.SerieProgressoUtils
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaATokens
import com.kalidorabr.bancodeminutagens.ui.temaa.destinoLabelTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.NavDestinoTemaA

/*
 * TEMA D — "painel circular": índigo profundo, neon roxo suave, Sora.
 * O círculo é a linguagem de tudo: anéis de progresso, pontos, doca redonda.
 * Movimentos de "energia": coisas carregam, pulsam e giram, com mola leve.
 */

val CianoD = Color(0xFF22D3EE)
val AmareloD = Color(0xFFFFB547)
val VerdeD = Color(0xFF34E3A0)
val LilasD = Color(0xFFC08BFF)
val EasingSuaveD = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/** Toque do D: afunda um pouco e solta uma onda da cor de destaque. */
@Composable
fun Modifier.cliqueTemaD(enabled: Boolean = true, vibrar: Boolean = false, onClick: () -> Unit): Modifier {
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()
    val escala by animateFloatAsState(
        if (pressionado && enabled) 0.96f else 1f,
        if (pressionado) tween(90) else spring(dampingRatio = 0.5f, stiffness = 650f),
        label = "cliqueTemaD"
    )
    val haptico = LocalHapticFeedback.current
    val accent = MaterialTheme.colorScheme.primary
    return this
        .graphicsLayer { scaleX = escala; scaleY = escala }
        .clickable(interactionSource = interacao, indication = ripple(color = accent), enabled = enabled) {
            haptico.performHapticFeedback(if (vibrar) HapticFeedbackType.Confirm else HapticFeedbackType.TextHandleMove)
            onClick()
        }
}

/** Anel de progresso (0 a 1). [animar] faz ele "carregar" ao aparecer. */
@Composable
fun AnelD(
    progresso: Float,
    tamanho: Dp,
    espessura: Dp,
    cor: Color = MaterialTheme.colorScheme.primary,
    animar: Boolean = true,
    conteudo: @Composable () -> Unit = {}
) {
    val alvo = progresso.coerceIn(0f, 1f)
    val p = remember { Animatable(if (animar) 0f else alvo) }
    LaunchedEffect(alvo) { p.animateTo(alvo, tween(900, delayMillis = 120, easing = EasingSuaveD)) }
    val trilho = TemaATokens.Track
    Box(Modifier.size(tamanho), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val e = espessura.toPx()
            val tl = Offset(e / 2, e / 2)
            val sz = Size(size.width - e, size.height - e)
            drawArc(trilho, 0f, 360f, false, tl, sz, style = Stroke(e))
            if (p.value > 0f) drawArc(cor, -90f, 360f * p.value, false, tl, sz, style = Stroke(e, cap = StrokeCap.Round))
        }
        conteudo()
    }
}

/** Pôster retangular com acabamento neon suave. */
@Composable
fun PosterNeonD(url: String, largura: Dp, altura: Dp, raio: Dp = 14.dp, neon: Boolean = true, descricao: String? = null) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        Modifier
            .size(largura, altura)
            .then(if (neon) Modifier.shadow(18.dp, RoundedCornerShape(raio), ambientColor = accent, spotColor = accent) else Modifier)
            .clip(RoundedCornerShape(raio))
            .background(TemaATokens.Track)
            .border(if (neon) 1.5.dp else 1.dp, if (neon) accent.copy(alpha = 0.7f) else TemaATokens.DarkBorder, RoundedCornerShape(raio))
    ) {
        if (url.isNotBlank()) AsyncImage(model = url, contentDescription = descricao, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    }
}

/** Doca flutuante do D: ícones redondos; a bolha roxa escorrega com mola até a aba e mostra o nome. */
@Composable
fun BarraAbasTemaD(itens: List<Pair<Painter, String>>, selecionado: Int, onSelecionar: (Int) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        Modifier
            .fillMaxWidth()
            .background(TemaATokens.DarkBackground)
            .padding(start = 44.dp, end = 44.dp, top = 6.dp, bottom = 12.dp)
    ) {
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(76.dp)
                .shadow(16.dp, RoundedCornerShape(38.dp))
                .clip(RoundedCornerShape(38.dp))
                .background(TemaATokens.DarkCardSurface.copy(alpha = 0.95f))
                .border(1.dp, TemaATokens.DarkBorder, RoundedCornerShape(38.dp))
        ) {
            val passo = maxWidth / itens.size
            val x by animateDpAsState(passo * selecionado + passo / 2 - 23.dp, spring(dampingRatio = 0.62f, stiffness = 420f), label = "bolhaD")
            Box(
                Modifier
                    .offset(x = x, y = 9.dp)
                    .size(46.dp)
                    .shadow(12.dp, CircleShape, ambientColor = accent, spotColor = accent)
                    .clip(CircleShape)
                    .background(accent)
            )
            Row(Modifier.fillMaxSize()) {
                itens.forEachIndexed { i, (icone, nome) ->
                    val on = i == selecionado
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelecionar(i) }
                            .padding(top = 9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                            Icon(icone, nome, tint = if (on) Color.White else TemaATokens.TextMuted, modifier = Modifier.size(22.dp))
                        }
                        val a by animateFloatAsState(if (on) 1f else 0f, tween(220, delayMillis = if (on) 140 else 0), label = "nomeD")
                        Text(nome, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(top = 3.dp).graphicsLayer { alpha = a })
                    }
                }
            }
        }
    }
}

/** Filtros do D: cápsula contornada; o escolhido acende em roxo. */
@Composable
fun FiltrosTemaD(rotulos: List<String>, selecionado: Int, onSelecionar: (Int) -> Unit, modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(TemaATokens.DarkCardSurface)
            .border(1.dp, TemaATokens.DarkBorder, RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        rotulos.forEachIndexed { i, r ->
            val on = i == selecionado
            Box(
                Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (on) accent else Color.Transparent)
                    .cliqueTemaD { onSelecionar(i) },
                contentAlignment = Alignment.Center
            ) {
                Text(r, color = if (on) Color.White else TemaATokens.TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

/** "Continuar" do D: pôster com neon, imagem da série desfocada ao fundo, anelzinho de progresso e botão roxo. */
@Composable
fun ContinueCardTemaD(serie: SerieItem, destino: DestinoEpisodio, onOpenSerieClick: () -> Unit, onContinueClick: (DestinoEpisodio) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val fundo = remember(serie) { serie.imageUrl.ifBlank { serie.posterUrl } }
    val poster = remember(serie) { serie.posterUrl.ifBlank { serie.imageUrl } }
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(TemaATokens.DarkCardSurface)
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(28.dp))
            .cliqueTemaD(onClick = onOpenSerieClick)
    ) {
        if (fundo.isNotBlank()) {
            AsyncImage(model = fundo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().blur(18.dp).graphicsLayer { alpha = 0.28f })
        }
        Box(Modifier.matchParentSize().background(Brush.horizontalGradient(listOf(TemaATokens.DarkCardSurface.copy(alpha = 0.35f), TemaATokens.DarkCardSurface.copy(alpha = 0.85f)))))
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            PosterNeonD(poster, 96.dp, 138.dp, 14.dp, true, serie.titulo)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("CONTINUAR", color = CianoD, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                Text(serie.titulo, color = Color.White, fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnelD(serie.progressoFloat, 34.dp, 4.dp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("${serie.progressoPorcentagem}%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("${serie.episodiosAssistidos} de ${serie.totalEpisodios} episódios", color = TemaATokens.TextMuted, fontSize = 11.sp)
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(accent)
                        .cliqueTemaD(vibrar = true) { onContinueClick(destino) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(destinoLabelTemaA(destino), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** Linha da série no D: pôster retangular, nome, próximo episódio e um anelzinho com a porcentagem. */
@Composable
fun SerieTemaDCardItem(serie: SerieItem, onOpenSerieClick: (SerieItem) -> Unit, modifier: Modifier = Modifier) {
    val finalizada = serie.isFinalizada && !serie.isIncompleta
    val destino = remember(serie) { SerieProgressoUtils.encontrarProximoEpisodio(serie) }
    Row(
        modifier
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(TemaATokens.DarkCardSurface)
            .border(1.dp, TemaATokens.DarkBorder, RoundedCornerShape(22.dp))
            .cliqueTemaD { onOpenSerieClick(serie) }
            .padding(start = 10.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PosterNeonD(serie.posterUrl.ifBlank { serie.imageUrl }, 50.dp, 70.dp, 10.dp, false, serie.titulo)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(serie.titulo, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                when {
                    finalizada -> "Finalizada · ${serie.episodiosAssistidos}/${serie.totalEpisodios}"
                    destino != null -> "Próximo ${destinoLabelTemaA(destino)}"
                    else -> "${serie.episodiosAssistidos} de ${serie.totalEpisodios} episódios"
                },
                color = if (finalizada) VerdeD else TemaATokens.TextMuted,
                fontSize = 12.sp
            )
        }
        AnelD(serie.progressoFloat, 44.dp, 4.dp, if (finalizada) VerdeD else MaterialTheme.colorScheme.primary) {
            Text("${serie.progressoPorcentagem}%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

/**
 * Linha do tempo do episódio no D: um anel dividido em 3 partes (Abertura, Recap, Encerramento).
 * As partes marcadas "carregam" uma depois da outra; ao trocar de episódio o anel gira um terço de volta.
 */
@Composable
fun AnelSecoesD(marcadas: List<Boolean>, chaveEpisodio: Any?, modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    val cores = listOf(accent, AmareloD, CianoD)
    val cargas = List(3) { remember { Animatable(0f) } }
    val giro = remember { Animatable(0f) }
    val primeira = remember { booleanArrayOf(true) }
    LaunchedEffect(chaveEpisodio, marcadas) {
        if (!primeira[0]) {
            cargas.forEach { it.snapTo(0f) }
            giro.animateTo(giro.value + 120f, spring(dampingRatio = 0.7f, stiffness = 200f))
        }
        primeira[0] = false
        cargas.forEachIndexed { i, a -> if (marcadas.getOrElse(i) { false }) a.animateTo(1f, tween(480, delayMillis = if (i == 0) 120 else 60, easing = EasingSuaveD)) }
    }
    val trilho = TemaATokens.Track
    val feitas = marcadas.count { it }
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().graphicsLayer { rotationZ = giro.value }) {
                val e = 16.dp.toPx()
                val tl = Offset(e / 2, e / 2)
                val sz = Size(size.width - e, size.height - e)
                val fatia = 120f - 14f
                for (i in 0..2) {
                    val inicio = -90f + 7f + i * 120f
                    drawArc(trilho, inicio, fatia, false, tl, sz, style = Stroke(e, cap = StrokeCap.Round))
                    val v = cargas[i].value
                    if (v > 0f) drawArc(cores[i], inicio, fatia * v, false, tl, sz, style = Stroke(e, cap = StrokeCap.Round))
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$feitas/3", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                Text("seções marcadas", color = TemaATokens.TextMuted, fontSize = 12.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            listOf("Abertura", "Recap", "Encerramento").forEachIndexed { i, n ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(cores[i]))
                    Spacer(Modifier.width(6.dp))
                    Text(n, color = TemaATokens.TextGray, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Transições do D ("energia em anéis"):
 *  - abrir série/temporada: a tela nova cresce com mola de dentro para fora, como um pulso;
 *  - episódio: nasce pequenininho do ponto tocado (cresce do centro);
 *  - trocar episódio: troca rápida (o anel gira dentro da tela);
 *  - voltar: encolhe de volta.
 */
fun androidx.compose.animation.AnimatedContentTransitionScope<NavDestinoTemaA>.transicaoNavTemaD(
    de: NavDestinoTemaA,
    para: NavDestinoTemaA
): androidx.compose.animation.ContentTransform {
    val mola = spring<Float>(dampingRatio = 0.72f, stiffness = 320f)
    val t = when {
        para.profundidade > de.profundidade && para.profundidade == 3 ->
            (scaleIn(mola, initialScale = 0.15f) + fadeIn(tween(220))) togetherWith fadeOut(tween(320), targetAlpha = 0.25f)
        para.profundidade > de.profundidade ->
            (scaleIn(mola, initialScale = 0.82f) + fadeIn(tween(240))) togetherWith
                (fadeOut(tween(300), targetAlpha = 0.25f) + scaleOut(tween(380, easing = EasingSuaveD), targetScale = 1.06f))
        para.profundidade < de.profundidade ->
            (scaleIn(tween(380, easing = EasingSuaveD), initialScale = 1.06f) + fadeIn(tween(260), initialAlpha = 0.25f)) togetherWith
                (scaleOut(tween(320, easing = EasingSuaveD), targetScale = 0.82f) + fadeOut(tween(240)))
        else -> fadeIn(tween(240, delayMillis = 60)) togetherWith fadeOut(tween(180))
    }
    return t.apply { targetContentZIndex = if (para.profundidade >= de.profundidade) para.profundidade.toFloat() + 1f else -1f }
}

private infix fun androidx.compose.animation.EnterTransition.togetherWith(exit: androidx.compose.animation.ExitTransition) =
    androidx.compose.animation.ContentTransform(this, exit)
