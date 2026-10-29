package com.kalidorabr.bancodeminutagens.ui.temaa

import com.kalidorabr.bancodeminutagens.ui.temaa.components.cliqueTemaA
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import com.kalidorabr.bancodeminutagens.ui.temab.sangrarB
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kalidorabr.bancodeminutagens.R
import com.kalidorabr.bancodeminutagens.model.DestinoEpisodio
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.model.SerieProgressoUtils
import com.kalidorabr.bancodeminutagens.ui.theme.BancoDeMinutagensTheme
import com.kalidorabr.bancodeminutagens.ui.temaa.components.posterCompartilhadoTemaA

/**
 * "Continuar de onde parou": série do último episódio salvo; se ela já foi
 * concluída (sem próximo episódio), pula para a próxima em andamento mais recente.
 */
fun encontrarContinuarTemaA(series: List<SerieItem>): Pair<SerieItem, DestinoEpisodio>? =
    series.sortedByDescending { it.lastUpdatedAt ?: 0L }
        .asSequence()
        .mapNotNull { serie -> SerieProgressoUtils.encontrarProximoEpisodio(serie)?.let { serie to it } }
        .firstOrNull()

/**
 * Cabeçalho padrão das abas do Tema A (Início, Buscar, Em breve, Ajustes):
 * título grande + linha de apoio logo abaixo, sempre na mesma altura.
 * [acao] fica à direita do título (ex.: lupa) sem mudar a posição do texto.
 */
@Composable
fun CabecalhoPaginaTemaA(
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier,
    acao: (@Composable () -> Unit)? = null
) {
    if (TemaVisual.b) {
        com.kalidorabr.bancodeminutagens.ui.temab.CabecalhoCurvoB(
            titulo = titulo,
            legenda = subtitulo,
            modifier = modifier.sangrarB(TemaATokens.SideMargin),
            direita = acao
        )
        return
    }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(titulo, color = TemaATokens.TextWhite, style = TemaATokens.TitleMain, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            acao?.invoke()
        }
        if (subtitulo.isNotBlank()) {
            Text(subtitulo, color = TemaATokens.TextGray, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun HomeHeaderTemaA(
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    subtitulo: String = "Continuar de onde parou"
) {
    // Lupa da Início: busca dentro da sua coleção (a aba Buscar procura no TMDB)
    var buscando by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(searchQuery.isNotEmpty()) }
    val foco = remember { androidx.compose.ui.focus.FocusRequester() }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = TemaATokens.SideMargin, end = TemaATokens.SideMargin, top = if (TemaVisual.b) 0.dp else 16.dp)
            .then(if (TemaVisual.b) Modifier else Modifier.androidx_animateContentSizeTemaA())
    ) {
        CabecalhoPaginaTemaA(
            titulo = "Minutagens",
            subtitulo = if (buscando) "Buscar na sua coleção" else subtitulo,
            acao = if (TemaVisual.b) ({
                com.kalidorabr.bancodeminutagens.ui.temab.BotaoTopoB(
                    if (buscando) Icons.Filled.Close else Icons.Filled.Search,
                    if (buscando) "Fechar busca" else "Buscar na coleção",
                    onClick = {
                        if (buscando) onSearchQueryChange("")
                        buscando = !buscando
                    },
                    ativo = buscando
                )
            }) else ({
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (buscando) TemaATokens.DarkBorder else TemaATokens.DarkCardSurface)
                        .clickable {
                            if (buscando) onSearchQueryChange("")
                            buscando = !buscando
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (buscando) {
                        Icon(Icons.Filled.Close, "Fechar busca", tint = TemaATokens.TextWhite, modifier = Modifier.size(18.dp))
                    } else {
                        Icon(painterResource(id = R.drawable.ic_search), "Buscar na coleção", tint = TemaATokens.TextWhite, modifier = Modifier.size(18.dp))
                    }
                }
            })
        )
        if (buscando) {
            Spacer(Modifier.height(14.dp))
            SearchFieldTemaA(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = "Nome da série…",
                modifier = Modifier.focusRequester(foco)
            )
            androidx.compose.runtime.LaunchedEffect(Unit) { runCatching { foco.requestFocus() } }
        }
    }
}

private fun Modifier.androidx_animateContentSizeTemaA(): Modifier =
    this.then(Modifier.animateContentSize(androidx.compose.animation.core.tween(220)))

@Composable
fun CircleIconButtonTemaA(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = TemaATokens.DarkCardSurface,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(TemaATokens.SizeHeaderButton)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
fun OverlineTemaA(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = TemaATokens.TextGray,
        style = TemaATokens.TextOverline,
        modifier = modifier
    )
}

@Composable
fun ProgressLineTemaA(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Int = 4,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(height.dp))
            .background(TemaATokens.Track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(height.dp))
                .background(color)
        )
    }
}

@Composable
fun ContinueCardTemaA(
    serie: SerieItem,
    destino: DestinoEpisodio,
    onOpenSerieClick: () -> Unit,
    onContinueClick: (DestinoEpisodio) -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    val imageSource = remember(serie) { serie.imageUrl.ifBlank { serie.posterUrl } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = TemaATokens.SideMargin, end = TemaATokens.SideMargin, top = 14.dp, bottom = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .cliqueTemaA(onClick = onOpenSerieClick)
                .clip(TemaATokens.ShapeCardFeatured)
                .background(TemaATokens.DarkCardSurface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (imageSource.isNotBlank()) {
                    AsyncImage(
                        model = imageSource,
                        contentDescription = serie.titulo,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.25f to Color.Transparent,
                                0.70f to TemaATokens.DarkCardSurface.copy(alpha = 0.85f),
                                1f to TemaATokens.DarkCardSurface
                            )
                        )
                )
                StatusPillTemaA(
                    serie = serie,
                    solid = true,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = serie.titulo,
                        color = TemaATokens.TextWhite,
                        fontFamily = TemaATokens.FontSans,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val meta = listOfNotNull(
                        serie.ano.takeIf { it > 0 }?.toString(),
                        serie.generos.replace(" / ", " · ").replace(" • ", " · ").takeIf { it.isNotBlank() }
                    ).joinToString(" · ")
                    if (meta.isNotBlank()) {
                        Text(meta, color = TemaATokens.TextSoft, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Próximo: ", color = TemaATokens.TextGray, fontSize = 13.sp)
                    Text(destinoLabelTemaA(destino), color = TemaATokens.TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${serie.episodiosAssistidos}/${serie.totalEpisodios}",
                        color = TemaATokens.TextWhite,
                        fontFamily = TemaATokens.FontMono,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
                ProgressLineTemaA(progress = serie.progressoFloat, height = 6)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TemaATokens.HeightSecondaryButton)
                        .clip(TemaATokens.ShapeButtonField)
                        .background(accent)
                        .clickable { onContinueClick(destino) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Continuar minutagem", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }
            }
        }
    }
}

fun destinoLabelTemaA(destino: DestinoEpisodio): String =
    "T${destino.seasonNumber.toString().padStart(2, '0')} · E${destino.episodeNumber.toString().padStart(2, '0')}"

@Composable
fun StatusPillTemaA(serie: SerieItem, modifier: Modifier = Modifier, solid: Boolean = false) {
    val (label, fg, bg) = when {
        serie.isFinalizada && !serie.isIncompleta -> Triple("Finalizada", TemaATokens.SuccessText, TemaATokens.SuccessBg)
        serie.isIncompleta -> Triple("Incompleta", TemaATokens.TextSoft, TemaATokens.DarkCardSurface)
        serie.status.equals("Em breve", true) -> Triple(com.kalidorabr.bancodeminutagens.model.StatusPlanilha.rotulo(serie.status), TemaATokens.InfoTexto, TemaATokens.InfoFundo)
        else -> Triple(com.kalidorabr.bancodeminutagens.model.StatusPlanilha.rotulo(serie.status), TemaATokens.SemanticWarning, TemaATokens.WarningBg)
    }
    Text(
        text = label,
        color = fg,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(CircleShape)
            .background(if (solid) TemaATokens.DarkBackground.copy(alpha = 0.75f) else bg)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
fun EmBreveHeaderTemaA(
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = TemaATokens.SideMargin, end = TemaATokens.SideMargin, top = if (TemaVisual.b) 0.dp else 16.dp, bottom = 8.dp)
    ) {
        CabecalhoPaginaTemaA("Em breve", "Estreias e séries para começar")
        Spacer(Modifier.height(18.dp))
        SearchFieldTemaA(searchQuery, onSearchQueryChange, "Buscar em breve")
    }
}

@Composable
fun SearchFieldTemaA(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TemaATokens.HeightSearchField)
            .clip(TemaATokens.ShapeButtonField)
            .background(TemaATokens.DarkCardSurface)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = null,
            tint = TemaATokens.TextGray,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = TemaATokens.TextWhite, fontSize = 14.sp, fontFamily = TemaATokens.FontSans),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(placeholder, color = TemaATokens.TextMuted, fontSize = 14.sp)
                inner()
            }
        )
    }
}

@Composable
fun FilterChipsRowTemaA(
    filters: List<String> = listOf("Todas", "Em andamento", "Finalizadas"),
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    totalCount: Int? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TemaATokens.SideMargin, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        filters.forEach { filter ->
            val selected = filter.equals(selectedFilter, ignoreCase = true) ||
                (filter == "Finalizadas" && selectedFilter == "Concluídas") ||
                (filter == "Concluídas" && selectedFilter == "Finalizadas")
            val label = if (filter == "Todas" && totalCount != null) "Todas · $totalCount" else filter
            Box(
                modifier = Modifier
                    .height(TemaATokens.HeightChip)
                    .clip(CircleShape)
                    .then(
                        if (selected) Modifier.background(TemaATokens.TextWhite)
                        else Modifier.border(1.dp, TemaATokens.DarkBorder, CircleShape)
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selected) TemaATokens.DarkBackground else TemaATokens.TextSoft,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun SerieTemaACardItem(
    serie: SerieItem,
    onOpenSerieClick: (SerieItem) -> Unit = {},
    onContinueClick: (DestinoEpisodio) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val finalizada = serie.isFinalizada && !serie.isIncompleta
    Row(
        modifier = modifier
            .cliqueTemaA { onOpenSerieClick(serie) }
            .fillMaxWidth()
            .padding(horizontal = TemaATokens.SideMargin, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val poster = serie.posterUrl.ifBlank { serie.imageUrl }
        Box(
            modifier = Modifier
                .posterCompartilhadoTemaA(serie.id)
                .width(52.dp)
                .height(72.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TemaATokens.DarkCardSurface)
        ) {
            if (poster.isNotBlank()) {
                AsyncImage(
                    model = poster,
                    contentDescription = serie.titulo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                serie.titulo,
                color = TemaATokens.TextWhite,
                style = TemaATokens.TitleCard,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (finalizada) {
                Text(
                    "Finalizada · ${serie.episodiosAssistidos}/${serie.totalEpisodios}",
                    color = TemaATokens.SuccessText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    "${serie.episodiosAssistidos} de ${serie.totalEpisodios} episódios · ${serie.progressoPorcentagem}%",
                    color = TemaATokens.TextGray,
                    fontSize = 12.sp
                )
            }
            ProgressLineTemaA(
                progress = serie.progressoFloat,
                color = if (finalizada) TemaATokens.SemanticSuccess else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TemaATokens.TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(name = "Linha de série — Tema A", showBackground = true, backgroundColor = 0xFF0B0B0D)
@Composable
private fun SerieTemaACardItemPreview() {
    BancoDeMinutagensTheme {
        SerieTemaACardItem(
            serie = SerieItem(
                id = "1",
                titulo = "Kamen Rider Wizard",
                ano = 2012,
                generos = "Ação",
                episodiosAssistidos = 5,
                totalEpisodios = 53,
                proximoEpisodio = "T01E06",
                imageUrl = ""
            )
        )
    }
}
