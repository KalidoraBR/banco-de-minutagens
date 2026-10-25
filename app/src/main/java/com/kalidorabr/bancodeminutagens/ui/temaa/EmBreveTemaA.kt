package com.kalidorabr.bancodeminutagens.ui.temaa

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kalidorabr.bancodeminutagens.model.SerieItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val formatoBr = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Data do primeiro episódio exibido (ignora especiais, "temporada 0"), ou null. */
fun estreiaSerieTemaA(serie: SerieItem): LocalDate? =
    serie.temporadas
        .filterNot { it.numero.trim() == "0" || it.nome.contains("especia", ignoreCase = true) }
        .flatMap { it.episodios }
        .mapNotNull { ep -> runCatching { LocalDate.parse(ep.dataExibicao.trim(), formatoBr) }.getOrNull() }
        .minOrNull()

/**
 * Aba Em breve: separa as séries "Em breve" em
 * estreias futuras (com contagem) e prontas para começar (botão Começar).
 */
@Composable
fun EmBreveListaTemaA(
    series: List<SerieItem>,
    temSeriesNaColecao: Boolean,
    onOpenSerie: (SerieItem) -> Unit,
    onComecar: (SerieItem) -> Unit,
    onBuscarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hoje = remember { LocalDate.now() }
    val comData = remember(series) { series.map { it to estreiaSerieTemaA(it) } }
    val futuras = comData.filter { (_, d) -> d != null && d.isAfter(hoje) }.sortedBy { it.second }
    val prontas = comData.filterNot { (_, d) -> d != null && d.isAfter(hoje) }.map { it.first }

    if (series.isEmpty()) {
        EstadoVazioTemaA(
            icone = Icons.Outlined.CalendarMonth,
            titulo = "Nada em breve",
            texto = if (temSeriesNaColecao) "Séries que você adicionar pela Buscar aparecem aqui até você começar."
            else "Adicione séries pela Buscar. Elas ficam aqui até você começar.",
            acao = "Buscar séries",
            onAcao = onBuscarClick,
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = TemaATokens.SideMargin, end = TemaATokens.SideMargin, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (futuras.isNotEmpty()) {
            item(key = "h_futuras") { OverlineTemaA("Estreias futuras · ${futuras.size}", Modifier.padding(top = 4.dp)) }
            items(futuras, key = { "f_" + it.first.id }) { (serie, data) ->
                val dias = ChronoUnit.DAYS.between(hoje, data!!)
                LinhaEmBreveTemaA(
                    serie = serie,
                    detalhe = "Estreia em ${data.format(formatoBr)}",
                    modifier = Modifier.animateItem(),
                    onClick = { onOpenSerie(serie) }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (dias == 1L) "amanhã" else "$dias",
                            color = TemaATokens.TextWhite,
                            fontFamily = TemaATokens.FontMono,
                            fontSize = if (dias == 1L) 13.sp else 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (dias != 1L) Text("dias", color = TemaATokens.TextGray, fontSize = 11.sp)
                    }
                }
            }
        }
        if (prontas.isNotEmpty()) {
            item(key = "h_prontas") { OverlineTemaA("Prontas para começar · ${prontas.size}", Modifier.padding(top = 8.dp)) }
            items(prontas, key = { "p_" + it.id }) { serie ->
                val data = estreiaSerieTemaA(serie)
                LinhaEmBreveTemaA(
                    serie = serie,
                    detalhe = data?.let { "Estreou em ${it.format(formatoBr)}" } ?: "${serie.totalEpisodios} episódios",
                    modifier = Modifier.animateItem(),
                    onClick = { onOpenSerie(serie) }
                ) {
                    Row(
                        Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onComecar(serie) }
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Começar", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LinhaEmBreveTemaA(
    serie: SerieItem,
    detalhe: String,
    modifier: Modifier,
    onClick: () -> Unit,
    direita: @Composable () -> Unit
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(TemaATokens.ShapeCardCommon)
            .background(TemaATokens.DarkGraphiteSurface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(48.dp).height(68.dp).clip(RoundedCornerShape(10.dp)).background(TemaATokens.DarkCardSurface)) {
            val p = serie.posterUrl.ifBlank { serie.imageUrl }
            if (p.isNotBlank()) AsyncImage(p, serie.titulo, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(serie.titulo, color = TemaATokens.TextWhite, style = TemaATokens.TitleCard, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(detalhe, color = TemaATokens.TextGray, fontSize = 12.sp)
        }
        Spacer(Modifier.width(10.dp))
        direita()
    }
}

/** Estado vazio padrão do Tema A: ícone em círculo, título, texto e ação opcional. */
@Composable
fun EstadoVazioTemaA(
    icone: ImageVector,
    titulo: String,
    texto: String,
    modifier: Modifier = Modifier,
    acao: String? = null,
    onAcao: () -> Unit = {}
) {
    Column(
        modifier.fillMaxSize().padding(horizontal = 40.dp).padding(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        com.kalidorabr.bancodeminutagens.ui.temas.IlustracaoClaquete()
        Text(titulo, color = TemaATokens.TextWhite, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Text(texto, color = TemaATokens.TextGray, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center)
        if (acao != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                acao, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clip(TemaATokens.ShapeButtonField).background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onAcao).padding(horizontal = 18.dp, vertical = 12.dp)
            )
        }
    }
}

/** Ícones reaproveitados nos estados vazios da Início. */
val IconeColecaoVaziaTemaA: ImageVector get() = Icons.Outlined.Tv
val IconeFiltroVazioTemaA: ImageVector get() = Icons.Outlined.Search
