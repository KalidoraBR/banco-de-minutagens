package com.kalidorabr.bancodeminutagens.ui.temas

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.kalidorabr.bancodeminutagens.model.EpisodiosNovosSerie
import com.kalidorabr.bancodeminutagens.model.Estatisticas
import com.kalidorabr.bancodeminutagens.model.MinutagemSecao
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.ui.temaa.OverlineTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.SubtelaTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaATokens
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAPrimaryButton
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASecondaryButton
import com.kalidorabr.bancodeminutagens.ui.temaa.components.cliqueTemaA
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/*
 * Peças da versão 1.7, usadas por todos os temas novos (as cores vêm de TemaATokens,
 * que já mudam conforme o tema ativo).
 */

// ---------- Início: episódios novos ----------

@Composable
fun CartaoEpisodiosNovos(
    novidades: List<EpisodiosNovosSerie>,
    onAdicionar: (EpisodiosNovosSerie) -> Unit,
    onDispensar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = TemaATokens.SideMargin, vertical = 6.dp)
            .clip(TemaATokens.ShapeCardCommon)
            .background(TemaATokens.DarkGraphiteSurface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(8.dp))
            Text("Saíram episódios novos", color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        }
        novidades.take(3).forEach { n ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = n.posterUrl, contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.size(40.dp, 56.dp).clip(RoundedCornerShape(8.dp)).background(TemaATokens.Track)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(n.titulo, color = TemaATokens.TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(n.resumo, color = TemaATokens.TextGray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Adicionar", color = TemaATokens.DarkBackground, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.clip(CircleShape).background(TemaATokens.TextWhite)
                        .cliqueTemaA(vibrar = true) { onAdicionar(n) }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                )
            }
        }
        if (novidades.size > 3) {
            Text("e mais ${novidades.size - 3} série(s)", color = TemaATokens.TextGray, fontSize = 12.sp)
        }
        Text(
            "Dispensar por agora", color = TemaATokens.TextGray, fontSize = 13.sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onDispensar).padding(8.dp)
        )
    }
}

// ---------- Início: lembrete de backup ----------

@Composable
fun FaixaLembreteBackup(
    dias: Int,
    nuncaFeito: Boolean,
    onFazer: () -> Unit,
    onAdiar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val azul = Color(0xFF3E7BFA)
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = TemaATokens.SideMargin, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(azul.copy(alpha = 0.14f))
            .padding(start = 14.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Storage, null, tint = azul, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                if (nuncaFeito) "Faça um backup das suas minutagens" else "Sem backup há $dias dias",
                color = TemaATokens.TextWhite, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Agora não", color = TemaATokens.TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onAdiar).padding(vertical = 2.dp)
            )
        }
        Text(
            "Fazer", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.clip(CircleShape).background(azul)
                .cliqueTemaA(vibrar = true, onClick = onFazer)
                .padding(horizontal = 16.dp, vertical = 9.dp)
        )
    }
}

// ---------- Estatísticas ----------

@Composable
fun TelaEstatisticas(est: Estatisticas, onBackClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    SubtelaTemaA(titulo = "Estatísticas", onBackClick = onBackClick) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuadroNumero("Séries", "${est.totalSeries}", if (est.seriesFinalizadas == 1) "1 completa" else "${est.seriesFinalizadas} completas", Modifier.weight(1f), TemaATokens.SuccessText)
            QuadroNumero("Episódios minutados", "${est.episodiosConcluidos}", "de ${est.episodiosTotal} no total", Modifier.weight(1f))
        }

        Column(
            Modifier.fillMaxWidth().clip(TemaATokens.ShapeCardCommon).background(TemaATokens.DarkGraphiteSurface).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Últimos 7 dias", color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                Text(
                    if (est.episodiosSemana == 1) "1 episódio" else "${est.episodiosSemana} episódios",
                    color = TemaATokens.TextGray, fontSize = 13.sp
                )
            }
            val maximo = (est.semana.maxOrNull() ?: 0).coerceAtLeast(1)
            val letras = remember {
                val hoje = java.time.LocalDate.now()
                (6 downTo 0).map { "DSTQQSS"[hoje.minusDays(it.toLong()).dayOfWeek.value % 7].toString() }
            }
            Row(Modifier.fillMaxWidth().height(110.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                est.semana.forEachIndexed { i, qtd ->
                    val hoje = i == est.semana.lastIndex
                    Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        if (qtd > 0) Text("$qtd", color = TemaATokens.TextGray, fontSize = 11.sp)
                        Box(
                            Modifier.fillMaxWidth().height((4 + 66 * qtd / maximo).dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (hoje && qtd > 0) accent else if (qtd > 0) accent.copy(alpha = 0.45f) else TemaATokens.Track)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(letras[i], color = if (hoje) TemaATokens.TextWhite else TemaATokens.TextGray, fontSize = 11.sp, fontWeight = if (hoje) FontWeight.ExtraBold else FontWeight.Normal)
                    }
                }
            }
            if (est.episodiosSemana == 0) {
                Text("O gráfico começa a contar a partir desta versão: cada episódio que você completar aparece aqui.", color = TemaATokens.TextGray, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuadroNumero("Abertura média", est.aberturaMedia?.let(MinutagemSecao::formatSecondsToMmSs) ?: "--:--", null, Modifier.weight(1f), mono = true)
            QuadroNumero("Encerramento médio", est.encerramentoMedio?.let(MinutagemSecao::formatSecondsToMmSs) ?: "--:--", null, Modifier.weight(1f), mono = true)
        }
        QuadroNumero(
            "Dias seguidos minutando",
            if (est.diasSeguidos == 1) "1 dia" else "${est.diasSeguidos} dias",
            if (est.diasSeguidos == 0) "Complete um episódio hoje para começar" else null,
            Modifier.fillMaxWidth()
        )

        if (est.faltando.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OverlineTemaA("Quanto falta em cada série")
                est.faltando.forEach { f ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row {
                            Text(f.titulo, color = TemaATokens.TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            Text(if (f.faltam == 1) "falta 1" else "faltam ${f.faltam}", color = TemaATokens.TextGray, fontSize = 13.sp, fontFamily = TemaATokens.FontMono)
                        }
                        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(TemaATokens.Track)) {
                            Box(Modifier.fillMaxWidth(f.progresso).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(accent))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuadroNumero(rotulo: String, valor: String, detalhe: String?, modifier: Modifier, corDetalhe: Color = TemaATokens.TextGray, mono: Boolean = false) {
    Column(
        modifier.clip(TemaATokens.ShapeCardCommon).background(TemaATokens.DarkGraphiteSurface).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(rotulo, color = TemaATokens.TextGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(valor, color = TemaATokens.TextWhite, fontSize = if (mono) 22.sp else 26.sp, fontWeight = FontWeight.ExtraBold, fontFamily = if (mono) TemaATokens.FontMono else null)
        if (detalhe != null) Text(detalhe, color = corDetalhe, fontSize = 12.sp)
    }
}

// ---------- Comemoração: série finalizada ----------

@Composable
fun DialogoSerieFinalizada(serie: SerieItem?, onVerSerie: () -> Unit, onFechar: () -> Unit) {
    if (serie == null) return
    val view = LocalView.current
    val entrada = remember(serie.id) { Animatable(0f) }
    val confete = remember(serie.id) { Animatable(0f) }
    LaunchedEffect(serie.id) {
        launch { entrada.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 260f)) }
        launch { confete.animateTo(1f, tween(2200, easing = LinearEasing)) }
        // três toques curtos de vibração, como uma pequena comemoração
        repeat(3) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            delay(140)
        }
    }
    val cores = listOf(MaterialTheme.colorScheme.primary, TemaATokens.SemanticWarning, TemaATokens.SemanticSuccess, Color(0xFF7FA6FF), Color.White)
    val pecas = remember(serie.id) {
        val r = java.util.Random(serie.id.hashCode().toLong())
        List(36) { Triple(r.nextFloat(), r.nextFloat() * 360f, 0.5f + r.nextFloat()) }
    }
    Dialog(onDismissRequest = onFechar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier.fillMaxSize()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onFechar),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val t = confete.value
                pecas.forEachIndexed { i, (x, ang, vel) ->
                    val y = -40f + (size.height * 0.9f) * t * vel
                    val px = size.width * x + 30f * sin((t * 6f + i) * 1.3f)
                    val alfa = (1f - t).coerceIn(0f, 1f)
                    rotate(ang + t * 360f * vel, Offset(px, y)) {
                        drawRect(cores[i % cores.size].copy(alpha = alfa), topLeft = Offset(px - 8f, y - 4f), size = Size(16f, 8f))
                    }
                }
            }
            Column(
                Modifier
                    .padding(horizontal = 28.dp)
                    .widthIn(max = 380.dp)
                    .graphicsLayer {
                        val e = entrada.value
                        scaleX = 0.85f + 0.15f * e; scaleY = scaleX; alpha = e.coerceIn(0f, 1f)
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .background(TemaATokens.DarkGraphiteSurface)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                    val verde = TemaATokens.SemanticSuccess
                    Canvas(Modifier.fillMaxSize()) {
                        drawArc(verde, -90f, 360f * entrada.value.coerceIn(0f, 1f), false, style = Stroke(width = 8.dp.toPx()))
                    }
                    AsyncImage(
                        model = serie.posterUrl, contentDescription = serie.titulo, contentScale = ContentScale.Crop,
                        modifier = Modifier.size(122.dp).clip(CircleShape).background(TemaATokens.Track)
                    )
                    Box(
                        Modifier.align(Alignment.BottomEnd).size(40.dp).clip(CircleShape).background(verde),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
                }
                Text("SÉRIE FINALIZADA", color = TemaATokens.SuccessText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                Text(serie.titulo, color = TemaATokens.TextWhite, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                Text(
                    "${serie.totalEpisodios} de ${serie.totalEpisodios} episódios minutados.",
                    color = TemaATokens.TextGray, fontSize = 14.sp, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TemaASecondaryButton("Ver a série", onVerSerie, Modifier.weight(1f))
                    TemaAPrimaryButton("Fechar", onFechar, Modifier.weight(1f))
                }
            }
        }
    }
}

// ---------- Ilustração dos estados vazios ----------

/** Claquete desenhada (tema de cinema), usada no lugar do ícone pequeno dos estados vazios. */
@Composable
fun IlustracaoClaquete(modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    val corpo = TemaATokens.DarkGraphiteSurface
    val borda = TemaATokens.Track
    val traco = TemaATokens.TextMuted
    val balanco = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        // a parte de cima "bate" uma vez quando a tela aparece
        balanco.animateTo(1f, tween(350))
        balanco.animateTo(0f, spring(dampingRatio = 0.35f, stiffness = 500f))
    }
    Canvas(modifier.size(170.dp, 140.dp)) {
        val w = size.width
        val h = size.height
        val topoCorpo = h * 0.36f
        drawRoundRect(corpo, topLeft = Offset(w * 0.12f, topoCorpo), size = Size(w * 0.76f, h * 0.56f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx()))
        drawRoundRect(borda, topLeft = Offset(w * 0.12f, topoCorpo), size = Size(w * 0.76f, h * 0.56f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx()), style = Stroke(2.dp.toPx()))
        // linhas do quadro
        listOf(0.52f, 0.66f, 0.80f).forEach { f ->
            drawLine(traco.copy(alpha = 0.5f), Offset(w * 0.22f, h * f), Offset(w * (if (f == 0.80f) 0.55f else 0.78f), h * f), strokeWidth = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
        // parte de cima, que abre e fecha
        rotate(-18f * balanco.value - 6f, pivot = Offset(w * 0.12f, topoCorpo)) {
            val barraTopo = topoCorpo - h * 0.14f
            drawRoundRect(borda, topLeft = Offset(w * 0.12f, barraTopo), size = Size(w * 0.76f, h * 0.13f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
            var x = w * 0.16f
            while (x < w * 0.84f) {
                drawLine(accent, Offset(x, barraTopo + h * 0.13f), Offset(x + w * 0.06f, barraTopo), strokeWidth = 6.dp.toPx())
                x += w * 0.14f
            }
        }
        // brilhinhos
        drawCircle(accent, 3.dp.toPx(), Offset(w * 0.92f, h * 0.12f))
        drawCircle(traco, 2.dp.toPx(), Offset(w * 0.05f, h * 0.5f))
        val c = Offset(w * 0.86f, h * 0.24f)
        val r = 5.dp.toPx()
        repeat(4) { k ->
            val a = Math.toRadians(45.0 + 90.0 * k)
            drawLine(traco, c, Offset(c.x + r * cos(a).toFloat(), c.y + r * sin(a).toFloat()), strokeWidth = 1.5.dp.toPx())
        }
    }
}

// ---------- Início: versão nova no GitHub ----------

@Composable
fun CartaoAtualizacao(versao: String, onAtualizar: () -> Unit, onDepois: () -> Unit, modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = TemaATokens.SideMargin, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.14f))
            .padding(start = 14.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.SystemUpdate, null, tint = accent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Versão $versao disponível", color = TemaATokens.TextWhite, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "Depois", color = TemaATokens.TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onDepois).padding(vertical = 2.dp)
            )
        }
        Text(
            "Atualizar", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.clip(CircleShape).background(accent)
                .cliqueTemaA(vibrar = true, onClick = onAtualizar)
                .padding(horizontal = 16.dp, vertical = 9.dp)
        )
    }
}

// ---------- Início: minutagem pronta no IntroDB ----------

/** Episódios em que o IntroDB já tem tempos para seções ainda vazias aqui. "Abrir" leva ao primeiro deles. */
@Composable
fun CartaoIntroDbPronto(quantos: Int, serie: String, onAbrir: () -> Unit, onDepois: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = TemaATokens.SideMargin, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(TemaATokens.SuccessBg)
            .padding(start = 14.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.CloudDone, null, tint = TemaATokens.SuccessText, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                if (quantos == 1) "1 episódio pronto no IntroDB" else "$quantos episódios prontos no IntroDB",
                color = TemaATokens.TextWhite, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(serie, color = TemaATokens.TextGray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                Text(
                    "  ·  Depois", color = TemaATokens.TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onDepois).padding(vertical = 2.dp)
                )
            }
        }
        Text(
            "Abrir", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.clip(CircleShape).background(TemaATokens.SemanticSuccess)
                .cliqueTemaA(vibrar = true, onClick = onAbrir)
                .padding(horizontal = 16.dp, vertical = 9.dp)
        )
    }
}
