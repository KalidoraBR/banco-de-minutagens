package com.kalidorabr.bancodeminutagens.ui.temaa

import com.kalidorabr.bancodeminutagens.ui.temac.ambienteC
import com.kalidorabr.bancodeminutagens.ui.temaa.components.cliqueTemaA
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import com.kalidorabr.bancodeminutagens.ui.temaa.components.BarraStatusAdaptativaTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.topoClaroTemaA
import com.kalidorabr.bancodeminutagens.model.DestinoEpisodio
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.model.SerieProgressoUtils
import com.kalidorabr.bancodeminutagens.model.TemporadaItem
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaABottomSheet
import com.kalidorabr.bancodeminutagens.ui.temaa.components.posterCompartilhadoTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAMenuItem
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAOverflowMenu
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAPrimaryButton

private enum class SerieSheetTemaA { STATUS, IDENTIFICADORES, MINUTAGEM_FIXA }

@Composable
fun SerieDetailTemaAScreen(
    serie: SerieItem,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onStatusClick: () -> Unit = {},
    onIdentificadoresClick: () -> Unit = {},
    onMinutagemFixaClick: () -> Unit = {},
    onMoverParaLixeiraClick: () -> Unit = {},
    onTemporadaClick: (TemporadaItem) -> Unit = {},
    onSaveStatus: (String) -> Unit = {},
    onSaveIdentificadores: (String, String, String) -> Unit = { _, _, _ -> },
    onSaveMinutagemFixa: (Boolean, Boolean, Boolean, String, String, String) -> Unit = { _, _, _, _, _, _ -> },
    onContinueClick: (DestinoEpisodio) -> Unit = {},
    onSyncTmdbClick: () -> Unit = {},
    isSyncingTmdb: Boolean = false
) {
    var sheet by remember { mutableStateOf<SerieSheetTemaA?>(null) }
    var ultimoSheet by remember { mutableStateOf(SerieSheetTemaA.STATUS) }
    var showMenu by remember { mutableStateOf(false) }
    var sinopseAberta by remember { mutableStateOf(false) }
    var iconesEscuros by remember(serie.id) { mutableStateOf(false) }
    // Ícones da barra de status acompanham o brilho do topo da imagem
    val rolagem = rememberScrollState()
    // Ao rolar, a imagem sai do topo e o fundo fica escuro: ícones voltam a ser brancos
    val imagemNoTopo = rolagem.value < with(androidx.compose.ui.platform.LocalDensity.current) { 220.dp.toPx() }
    BarraStatusAdaptativaTemaA(iconesEscuros && imagemNoTopo && sheet == null && !showMenu)
    val accent = MaterialTheme.colorScheme.primary
    val destino = remember(serie) { SerieProgressoUtils.encontrarProximoEpisodio(serie) }
    val ativas = listOf(serie.aberturaAtiva, serie.recapAtivo, serie.encerramentoAtivo).count { it }

    fun abrir(s: SerieSheetTemaA) {
        ultimoSheet = s
        sheet = s
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(TemaATokens.DarkBackground)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rolagem)
                .padding(bottom = 140.dp)
        ) {
            // Imagem de fundo passa por baixo da barra de status
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                val fundo = serie.imageUrl.ifBlank { serie.posterUrl }
                if (fundo.isNotBlank()) {
                    val ctx = LocalContext.current
                    val pedido = remember(fundo) {
                        // allowHardware(false) permite ler os pixels para medir o brilho
                        ImageRequest.Builder(ctx).data(fundo).allowHardware(false).build()
                    }
                    AsyncImage(
                        model = pedido,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onSuccess = { estado ->
                            iconesEscuros = runCatching {
                                topoClaroTemaA(estado.result.drawable.toBitmap())
                            }.getOrDefault(false)
                        }
                    )
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.45f to Color.Transparent,
                                1f to TemaATokens.DarkBackground
                            )
                        )
                )
            }

            Column(
                Modifier
                    .offset(y = (-96).dp)
                    .padding(horizontal = TemaATokens.SideMargin),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Box(
                        Modifier
                            .posterCompartilhadoTemaA(serie.id)
                            .width(96.dp)
                            .height(140.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(TemaATokens.DarkCardSurface)
                            .border(1.dp, TemaATokens.SubtleBorder, RoundedCornerShape(14.dp))
                    ) {
                        val poster = serie.posterUrl.ifBlank { serie.imageUrl }
                        if (poster.isNotBlank()) {
                            AsyncImage(
                                model = poster,
                                contentDescription = "Pôster de ${serie.titulo}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.padding(bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Texto sobre imagem: dentro de etiqueta sólida
                        Text(
                            serie.titulo,
                            color = TemaATokens.TextWhite,
                            fontSize = 24.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        val temps = serie.temporadas.size
                        val meta = listOfNotNull(
                            serie.ano.takeIf { it > 0 }?.toString(),
                            "$temps ${if (temps == 1) "temporada" else "temporadas"}",
                            "${serie.totalEpisodios} ep."
                        ).joinToString(" · ")
                        Text(meta, color = TemaATokens.TextGray, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SeloStatusTemaA(serie.status) { abrir(SerieSheetTemaA.STATUS) }
                            SeloTemaA(
                                serie.identificadores.ifBlank { "IMDb" }.replace(" • ", " · "),
                                TemaATokens.TextSoft,
                                TemaATokens.Neutro
                            ) { abrir(SerieSheetTemaA.IDENTIFICADORES) }
                        }
                    }
                }

                if (serie.sinopse.isNotBlank()) {
                    Column {
                        Text(
                            serie.sinopse,
                            color = TemaATokens.TextoCorpo,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            maxLines = if (sinopseAberta) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (serie.sinopse.length > 180) {
                            Text(
                                if (sinopseAberta) "Ler menos" else "Ler mais",
                                color = TemaATokens.TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .clickable { sinopseAberta = !sinopseAberta }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                // Resumo: Progresso · Episódios · Minutagem fixa (abre o pop-up)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(TemaATokens.ShapeCardCommon)
                        .background(TemaATokens.DarkGraphiteSurface)
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ResumoItemTemaA("${serie.progressoPorcentagem}%", "Progresso", Modifier.weight(1f))
                    Box(Modifier.width(1.dp).height(36.dp).background(TemaATokens.Track))
                    ResumoItemTemaA("${serie.episodiosAssistidos}/${serie.totalEpisodios}", "Episódios", Modifier.weight(1f))
                    Box(Modifier.width(1.dp).height(36.dp).background(TemaATokens.Track))
                    ResumoItemTemaA(
                        "$ativas/3",
                        "Minutagem fixa",
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { abrir(SerieSheetTemaA.MINUTAGEM_FIXA) }
                    )
                }

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Temporadas", color = TemaATokens.TextWhite, style = TemaATokens.TitleSection, modifier = Modifier.weight(1f))
                    Row(
                        Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, TemaATokens.DarkBorder, CircleShape)
                            .clickable(enabled = !isSyncingTmdb, onClick = onSyncTmdbClick)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSyncingTmdb) {
                            CircularProgressIndicator(Modifier.size(14.dp), color = TemaATokens.TextSoft, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Refresh, null, tint = TemaATokens.TextSoft, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("Sincronizar TMDB", color = TemaATokens.TextSoft, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    serie.temporadas.forEach { temp ->
                        TemporadaLinhaTemaA(temp) { onTemporadaClick(temp) }
                    }
                }
            }
        }

        // Botões do topo
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CircleIconButtonTemaA(onClick = onBackClick, background = Color(0x990B0B0D)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = TemaATokens.TextWhite, modifier = Modifier.size(20.dp))
            }
            CircleIconButtonTemaA(
                onClick = { showMenu = true },
                background = if (showMenu) TemaATokens.DarkBorder else Color(0x990B0B0D)
            ) {
                Icon(Icons.Filled.MoreVert, "Mais opções", tint = TemaATokens.TextWhite, modifier = Modifier.size(20.dp))
            }
        }

        // Botão Continuar fixo embaixo, acima da barra de 3 botões
        if (destino != null) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, TemaATokens.DarkBackground, TemaATokens.DarkBackground))
                    )
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp)
            ) {
                if (TemaVisual.c) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .cliqueTemaA(vibrar = true) { onContinueClick(destino) }
                            .height(52.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.PlayArrow, null, tint = Color(0xFF111111), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Continuar ${destinoLabelTemaA(destino)}", color = Color(0xFF111111), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    }
                } else TemaAPrimaryButton(
                    text = "Continuar ${destinoLabelTemaA(destino)}",
                    onClick = { onContinueClick(destino) },
                    leading = { Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        TemaAOverflowMenu(
            visible = showMenu,
            onDismiss = { showMenu = false },
            items = listOf(TemaAMenuItem("Mover para a lixeira", danger = true, onClick = onMoverParaLixeiraClick))
        )

        TemaABottomSheet(visible = sheet != null, onDismiss = { sheet = null }) {
            when (sheet ?: ultimoSheet) {
                SerieSheetTemaA.STATUS -> StatusSheetContentTemaA(
                    nomeSerie = serie.titulo,
                    statusAtual = serie.status,
                    onClose = { sheet = null },
                    onSave = { onSaveStatus(it); sheet = null }
                )
                SerieSheetTemaA.IDENTIFICADORES -> IdentificadoresSheetContentTemaA(
                    nomeSerie = serie.titulo,
                    tmdbId = serie.id,
                    imdbIdAtual = serie.imdbId,
                    tvdbIdAtual = serie.tvdbId,
                    onClose = { sheet = null },
                    onSave = { a, b, c -> onSaveIdentificadores(a, b, c); sheet = null }
                )
                SerieSheetTemaA.MINUTAGEM_FIXA -> MinutagemFixaSheetContentTemaA(
                    nomeSerie = serie.titulo,
                    aberturaAtiva = serie.aberturaAtiva,
                    recapAtivo = serie.recapAtivo,
                    encerramentoAtivo = serie.encerramentoAtivo,
                    duracaoFixaAbertura = serie.duracaoFixaAbertura,
                    duracaoFixaRecap = serie.duracaoFixaRecap,
                    duracaoFixaEncerramento = serie.duracaoFixaEncerramento,
                    onClose = { sheet = null },
                    onSave = { a, b, c, d, e, f -> onSaveMinutagemFixa(a, b, c, d, e, f); sheet = null }
                )
            }
        }
    }
}

@Composable
private fun SeloStatusTemaA(status: String, onClick: () -> Unit) {
    val (fg, bg) = when {
        status.equals("Finalizada", true) || status.equals("Concluída", true) -> TemaATokens.SuccessText to TemaATokens.SuccessBg
        status.equals("Em breve", true) -> TemaATokens.InfoTexto to TemaATokens.InfoFundo
        status.equals("Incompleta", true) -> TemaATokens.TextSoft to TemaATokens.Neutro
        else -> TemaATokens.SemanticWarning to TemaATokens.WarningBg
    }
    SeloTemaA(status.ifBlank { "Em andamento" }, fg, bg, onClick)
}

@Composable
private fun SeloTemaA(text: String, fg: Color, bg: Color, onClick: () -> Unit) {
    Text(
        text,
        color = fg,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    )
}

@Composable
private fun ResumoItemTemaA(valor: String, rotulo: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(vertical = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, color = TemaATokens.TextWhite, style = TemaATokens.TextNumber)
        Text(rotulo, color = TemaATokens.TextGray, fontSize = 11.sp)
    }
}

@Composable
internal fun TemporadaLinhaTemaA(temp: TemporadaItem, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val numero = SerieProgressoUtils.numeroTemporada(temp.numero)
    Row(
        Modifier
            .cliqueTemaA(onClick = onClick)
            .fillMaxWidth()
            .clip(TemaATokens.ShapeCardCommon)
            .background(TemaATokens.DarkGraphiteSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 5.dp.toPx()
                val inset = stroke / 2 + 2.dp.toPx()
                val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                drawArc(TemaATokens.Track, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
                drawArc(
                    accent, -90f, 360f * temp.progressoFloat.coerceIn(0f, 1f), false,
                    Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
            Text(
                numero?.let { "T" + it.toString().padStart(2, '0') } ?: temp.numero,
                color = TemaATokens.TextWhite,
                fontFamily = TemaATokens.FontMono,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(temp.nome.ifBlank { "Temporada ${numero ?: ""}" }, color = TemaATokens.TextWhite, style = TemaATokens.TitleCard)
            Text("${temp.episodiosConcluidos} de ${temp.totalEpisodios} episódios", color = TemaATokens.TextGray, fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TemaATokens.TextMuted)
    }
}
