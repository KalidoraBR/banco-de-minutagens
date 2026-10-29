package com.kalidorabr.bancodeminutagens.ui.temac

import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kalidorabr.bancodeminutagens.model.DestinoEpisodio
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaATokens
import com.kalidorabr.bancodeminutagens.ui.temaa.components.NavDestinoTemaA

/*
 * TEMA C — "streaming / sala de cinema".
 * Grafite e vermelho, Figtree, imagens grandes que se apagam no fundo,
 * botões em pílula e movimentos longos de desaceleração (como um corte de cena).
 */

/** Desaceleração longa, de cinema. */
val EasingCinemaC = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

/** Toque do C: o botão cresce um pouco, como o "foco" do controle remoto. */
@Composable
fun Modifier.cliqueTemaC(enabled: Boolean = true, vibrar: Boolean = false, onClick: () -> Unit): Modifier {
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()
    val escala by animateFloatAsState(
        if (pressionado && enabled) 1.035f else 1f,
        if (pressionado) tween(120) else spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "cliqueTemaC"
    )
    val haptico = LocalHapticFeedback.current
    return this
        .graphicsLayer { scaleX = escala; scaleY = escala }
        .clickable(interactionSource = interacao, indication = ripple(color = Color.White), enabled = enabled) {
            if (vibrar) haptico.performHapticFeedback(HapticFeedbackType.Confirm)
            onClick()
        }
}

/**
 * Barra de abas do C: pílula flutuante, solta do rodapé. A aba ativa vira uma
 * pílula branca com ícone e nome (como os botões do C); as outras mostram só o ícone.
 */
@Composable
fun BarraAbasTemaC(itens: List<Pair<Painter, String>>, selecionado: Int, onSelecionar: (Int) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(TemaATokens.DarkBackground)
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 10.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(14.dp, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF2C2D34))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(32.dp))
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            itens.forEachIndexed { i, (icone, nome) ->
                val on = i == selecionado
                Row(
                    Modifier
                        .height(48.dp)
                        .animateContentSize(androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 500f))
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (on) Color.White else Color.Transparent)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelecionar(i) }
                        .padding(horizontal = if (on) 18.dp else 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icone, nome, tint = if (on) Color(0xFF16171B) else TemaATokens.TextSoft, modifier = Modifier.size(if (on) 20.dp else 22.dp))
                    if (on) {
                        Spacer(Modifier.width(8.dp))
                        Text(nome, color = Color(0xFF16171B), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    }
                }
            }
        }
    }
}

/**
 * "Continuar de onde parou" do C: imagem grande da série que se apaga no fundo,
 * título de filme, botão branco em pílula e a barra de progresso dentro de uma gota de vidro.
 */
@Composable
fun ContinueCardTemaC(
    serie: SerieItem,
    destino: DestinoEpisodio,
    onOpenSerieClick: () -> Unit,
    onContinueClick: (DestinoEpisodio) -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    val imagem = remember(serie) { serie.imageUrl.ifBlank { serie.posterUrl } }
    val fundo = TemaATokens.DarkBackground
    // A imagem começa atrás do relógio; o título "Minutagens" e a lupa ficam por cima dela
    Box(Modifier.fillMaxWidth()) {
        if (imagem.isNotBlank()) {
            // Fundo: a mesma imagem desfocada preenche o espaço alto (sem mostrar o corte)
            AsyncImage(
                model = imagem,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .blur(28.dp)
                    .graphicsLayer { alpha = 0.55f }
            )
            // Na frente: a imagem quase inteira (larga, como no cinema), que se dissolve no fundo
            AsyncImage(
                model = imagem,
                contentDescription = serie.titulo,
                contentScale = ContentScale.Crop,
                alignment = androidx.compose.ui.BiasAlignment(0f, -0.3f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            Brush.verticalGradient(0.3f to Color.Black, 0.6f to Color.Black.copy(alpha = 0.6f), 0.85f to Color.Black.copy(alpha = 0.15f), 1f to Color.Transparent),
                            blendMode = androidx.compose.ui.graphics.BlendMode.DstIn
                        )
                    }
            )
        }
        // A foto se apaga aos poucos até o fundo: sem linha de corte
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.7f),
                        0.22f to Color.Black.copy(alpha = 0.2f),
                        0.32f to Color.Transparent,
                        0.5f to fundo.copy(alpha = 0.6f),
                        0.68f to fundo.copy(alpha = 0.88f),
                        0.85f to fundo,
                        1f to fundo
                    )
                )
        )
        Column(
            Modifier
                .statusBarsPadding()
                .padding(start = TemaATokens.SideMargin, end = TemaATokens.SideMargin, top = 200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("CONTINUAR DE ONDE PAROU", style = TextStyle(color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, shadow = Shadow(Color.Black.copy(alpha = 0.7f), blurRadius = 12f)))
            Text(
                serie.titulo,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 30.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.2).sp,
                    shadow = Shadow(Color.Black.copy(alpha = 0.6f), blurRadius = 24f)
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val meta = listOfNotNull(serie.ano.takeIf { it > 0 }?.toString(), serie.generos.replace(" / ", " · ").replace(" • ", " · ").takeIf { it.isNotBlank() }).joinToString(" · ")
            if (meta.isNotBlank()) Text(meta, color = TemaATokens.TextGray, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier
                        .weight(1f)
                        .cliqueTemaC(vibrar = true) { onContinueClick(destino) }
                        .height(46.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.PlayArrow, null, tint = Color(0xFF111111), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Continuar ${com.kalidorabr.bancodeminutagens.ui.temaa.destinoLabelTemaA(destino)}", color = Color(0xFF111111), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                }
                Box(
                    Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .cliqueTemaC(onClick = onOpenSerieClick),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Info, "Abrir série", tint = Color.White, modifier = Modifier.size(20.dp)) }
            }
            // Gota de vidro só com a barra grossa
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.07f))
                    .padding(8.dp)
            ) {
                Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.14f))) {
                    Box(Modifier.fillMaxWidth(serie.progressoFloat.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(50)).background(accent))
                }
            }
        }
    }
}

/**
 * Transições do C ("sala de cinema"):
 *  - abrir: a tela nova cresce do fundo enquanto a anterior escurece, como a luz da sala apagando;
 *  - temporada: sobe;
 *  - episódio: abre como apertar "play" (cresce do centro);
 *  - próximo/anterior: a página desliza e desfoca;
 *  - voltar: o contrário.
 */
fun androidx.compose.animation.AnimatedContentTransitionScope<NavDestinoTemaA>.transicaoNavTemaC(
    de: NavDestinoTemaA,
    para: NavDestinoTemaA
): androidx.compose.animation.ContentTransform {
    val longo = 560
    val t = when {
        para.profundidade == 1 && de.profundidade == 0 ->
            (scaleIn(tween(longo, easing = EasingCinemaC), initialScale = 0.88f) + fadeIn(tween(320))) togetherWith
                (fadeOut(tween(longo), targetAlpha = 0.15f) + scaleOut(tween(longo, easing = EasingCinemaC), targetScale = 0.96f))
        para.profundidade == 2 && de.profundidade == 1 ->
            (slideInVertically(tween(440, easing = EasingCinemaC)) { it / 8 } + fadeIn(tween(300))) togetherWith
                fadeOut(tween(360), targetAlpha = 0.2f)
        para.profundidade == 3 && de.profundidade < 3 ->
            (scaleIn(tween(520, easing = EasingCinemaC), initialScale = 0.2f) + fadeIn(tween(260))) togetherWith
                fadeOut(tween(420), targetAlpha = 0.2f)
        para.profundidade == de.profundidade && para.profundidade == 3 ->
            (slideInHorizontally(tween(360, easing = EasingCinemaC)) { it / 6 } + fadeIn(tween(300, delayMillis = 60))) togetherWith
                (slideOutHorizontally(tween(320, easing = EasingCinemaC)) { -it / 6 } + fadeOut(tween(220)))
        para.profundidade < de.profundidade ->
            (fadeIn(tween(360), initialAlpha = 0.2f) + scaleIn(tween(420, easing = EasingCinemaC), initialScale = 0.96f)) togetherWith
                (scaleOut(tween(360, easing = EasingCinemaC), targetScale = 0.9f) + fadeOut(tween(300)))
        else -> fadeIn(tween(260)) togetherWith fadeOut(tween(200))
    }
    return t.apply { targetContentZIndex = if (para.profundidade >= de.profundidade) para.profundidade.toFloat() + 1f else -1f }
}

private infix fun androidx.compose.animation.EnterTransition.togetherWith(exit: androidx.compose.animation.ExitTransition) =
    androidx.compose.animation.ContentTransform(this, exit)

/** "Minhas séries" do C: fileira de pôsteres que desliza para o lado, com a etiqueta do próximo episódio e a barra de progresso. */
@Composable
fun CarrosselSeriesTemaC(series: List<SerieItem>, onOpen: (SerieItem) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Column(Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Minhas séries", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = TemaATokens.SideMargin))
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = TemaATokens.SideMargin),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(series, key = { it.id }) { serie ->
                val destino = remember(serie) { com.kalidorabr.bancodeminutagens.model.SerieProgressoUtils.encontrarProximoEpisodio(serie) }
                Column(Modifier.width(124.dp).cliqueTemaC { onOpen(serie) }, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.width(124.dp).height(180.dp).clip(RoundedCornerShape(10.dp)).background(TemaATokens.Track)) {
                        val poster = serie.posterUrl.ifBlank { serie.imageUrl }
                        if (poster.isNotBlank()) AsyncImage(model = poster, contentDescription = serie.titulo, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        if (destino != null) {
                            Text(
                                com.kalidorabr.bancodeminutagens.ui.temaa.destinoLabelTemaA(destino),
                                color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = TemaATokens.FontMono,
                                modifier = Modifier.align(Alignment.BottomStart).padding(start = 6.dp, bottom = 10.dp).clip(RoundedCornerShape(6.dp)).background(Color.Black.copy(alpha = 0.72f)).padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                        Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().height(4.dp).background(Color.White.copy(alpha = 0.2f))) {
                            Box(Modifier.fillMaxWidth(serie.progressoFloat.coerceIn(0f, 1f)).fillMaxHeight().background(if (serie.isFinalizada && !serie.isIncompleta) TemaATokens.SemanticSuccess else accent))
                        }
                    }
                    Text(serie.titulo, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/** Tema C: luz suave da cor de destaque vindo do topo, como o "clima" da série nos apps de streaming. */
@Composable
fun Modifier.ambienteC(): Modifier {
    val tv = com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual
    val accent = MaterialTheme.colorScheme.primary
    if (tv.d) {
        // Tema D: luz roxa no canto e anéis bem fracos, como no canvas
        return this.drawBehind {
            val centro = androidx.compose.ui.geometry.Offset(size.width * 0.88f, size.height * 0.02f)
            drawCircle(Brush.radialGradient(listOf(accent.copy(alpha = 0.3f), Color.Transparent), center = centro, radius = size.width * 0.75f), radius = size.width * 0.75f, center = centro)
            val raio = 80.dp.toPx()
            for (i in 0 until 5) {
                drawCircle(accent.copy(alpha = 0.14f - i * 0.024f), radius = raio + i * 60.dp.toPx(), center = centro, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
            }
        }
    }
    if (!tv.c) return this
    return this.drawBehind {
        drawRect(
            Brush.radialGradient(
                listOf(accent.copy(alpha = 0.34f), accent.copy(alpha = 0.12f), Color.Transparent),
                center = androidx.compose.ui.geometry.Offset(size.width / 2f, 0f),
                radius = size.width * 1.1f
            )
        )
    }
}
