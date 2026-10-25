package com.kalidorabr.bancodeminutagens.ui.temab

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.launch
import com.kalidorabr.bancodeminutagens.model.DestinoEpisodio
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.model.SerieProgressoUtils
import com.kalidorabr.bancodeminutagens.model.TemporadaItem
import com.kalidorabr.bancodeminutagens.ui.temaa.IdentificadoresSheetContentTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.MinutagemFixaSheetContentTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.StatusSheetContentTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaATokens
import com.kalidorabr.bancodeminutagens.ui.temaa.TemporadaLinhaTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.destinoLabelTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaABottomSheet
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAMenuItem
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAOverflowMenu

private enum class SheetSerieB { STATUS, IDENTIFICADORES, MINUTAGEM_FIXA }

/**
 * Série no Tema B (canvas "B · Série"): cabeçalho escuro com a imagem e base curva,
 * pôster centralizado sobre a curva, título em maiúsculas e cards brancos.
 * Ao abrir: o cabeçalho desce e se estica dando uma "respirada", o pôster cresce no lugar,
 * o título sobe palavra por palavra e depois os cards entram em sequência.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SerieDetailTemaBScreen(
    serie: SerieItem,
    onBackClick: () -> Unit = {},
    onMoverParaLixeiraClick: () -> Unit = {},
    onTemporadaClick: (TemporadaItem) -> Unit = {},
    onSaveStatus: (String) -> Unit = {},
    onSaveIdentificadores: (String, String, String) -> Unit = { _, _, _ -> },
    onSaveMinutagemFixa: (Boolean, Boolean, Boolean, String, String, String) -> Unit = { _, _, _, _, _, _ -> },
    onContinueClick: (DestinoEpisodio) -> Unit = {},
    onSyncTmdbClick: () -> Unit = {},
    isSyncingTmdb: Boolean = false
) {
    var sheet by remember { mutableStateOf<SheetSerieB?>(null) }
    var ultimoSheet by remember { mutableStateOf(SheetSerieB.STATUS) }
    var showMenu by remember { mutableStateOf(false) }
    var sinopseAberta by remember { mutableStateOf(false) }
    val accent = MaterialTheme.colorScheme.primary
    val destino = remember(serie) { SerieProgressoUtils.encontrarProximoEpisodio(serie) }
    val ativas = listOf(serie.aberturaAtiva, serie.recapAtivo, serie.encerramentoAtivo).count { it }

    fun abrir(s: SheetSerieB) {
        ultimoSheet = s
        sheet = s
    }

    // Imagem passa por trás da barra de status; ícones acompanham o brilho do topo da imagem
    val tv = com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual
    androidx.compose.runtime.DisposableEffect(Unit) {
        tv.semFaixa = true
        onDispose { tv.semFaixa = false }
    }
    var iconesEscuros by remember(serie.id) { mutableStateOf(false) }
    val rolagem = rememberScrollState()
    val imagemNoTopo = rolagem.value < with(androidx.compose.ui.platform.LocalDensity.current) { 240.dp.toPx() }
    com.kalidorabr.bancodeminutagens.ui.temaa.components.BarraStatusAdaptativaTemaA(iconesEscuros && imagemNoTopo && sheet == null && !showMenu)

    // Cabeçalho: começa na altura do da Início (230) e estica até 320 passando um pouco do ponto
    val altura = remember { Animatable(230f) }
    val poster = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { altura.animateTo(320f, tween(440, easing = EasingRespiraB)) }
        launch { poster.animateTo(1f, tween(380, delayMillis = 160, easing = EasingQuiqueB)) }
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
                .padding(bottom = 150.dp)
        ) {
            Box(Modifier.fillMaxWidth().height(390.dp)) {
                // Cabeçalho escuro com a imagem
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(altura.value.dp)
                        .clip(BaseCurvaB(34.dp))
                        .background(EscuroCabecalhoB)
                ) {
                    val fundo = serie.imageUrl.ifBlank { serie.posterUrl }
                    if (fundo.isNotBlank()) {
                        val ctx = androidx.compose.ui.platform.LocalContext.current
                        val pedido = remember(fundo) {
                            // allowHardware(false) permite ler os pixels para medir o brilho
                            coil.request.ImageRequest.Builder(ctx).data(fundo).allowHardware(false).build()
                        }
                        AsyncImage(
                            model = pedido,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().entradaB(120, 0.dp),
                            onSuccess = { estado ->
                                iconesEscuros = runCatching {
                                    com.kalidorabr.bancodeminutagens.ui.temaa.components.topoClaroTemaA(
                                        estado.result.drawable.toBitmap()
                                    )
                                }.getOrDefault(false)
                            }
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0f to Color(0x00141218),
                                    0.4f to Color(0x0D141218),
                                    1f to Color(0x59141218)
                                )
                            )
                    )
                }
                // Pôster: cresce no lugar e assenta sobre a curva, como uma foto colada
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 214.dp)
                        .graphicsLayer {
                            val p = poster.value
                            scaleX = 0.3f + 0.7f * p
                            scaleY = 0.3f + 0.7f * p
                            alpha = p.coerceIn(0f, 1f)
                        }
                        .width(104.dp)
                        .height(148.dp)
                        .shadow(14.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(TemaATokens.DarkBackground)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(TemaATokens.Track)
                ) {
                    val p = serie.posterUrl.ifBlank { serie.imageUrl }
                    if (p.isNotBlank()) {
                        AsyncImage(
                            model = p,
                            contentDescription = "Pôster de ${serie.titulo}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Column(
                Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Título sobe palavra por palavra
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    serie.titulo.uppercase().split(" ").filter { it.isNotBlank() }.forEachIndexed { i, palavra ->
                        Text(
                            palavra,
                            color = TemaATokens.TextWhite,
                            fontSize = 22.sp,
                            lineHeight = 25.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.entradaB(260 + i.coerceAtMost(6) * 60)
                        )
                    }
                }
                val temps = serie.temporadas.size
                val meta = listOfNotNull(
                    serie.ano.takeIf { it > 0 }?.toString(),
                    "$temps ${if (temps == 1) "temporada" else "temporadas"}",
                    "${serie.totalEpisodios} ep."
                ).joinToString(" · ")
                Text(meta, color = TemaATokens.TextGray, fontSize = 12.sp, modifier = Modifier.entradaB(440))
                Row(
                    Modifier.padding(top = 4.dp).entradaB(480),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val (fg, bg) = corStatusB(serie.status)
                    Row(
                        Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bg)
                            .cliqueTemaB { abrir(SheetSerieB.STATUS) }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(serie.status.ifBlank { "Em andamento" }.uppercase(), color = fg, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        Icon(Icons.Filled.KeyboardArrowDown, null, tint = fg, modifier = Modifier.size(16.dp))
                    }
                    Box(
                        Modifier
                            .height(32.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .background(TemaATokens.DarkCardSurface)
                            .cliqueTemaB { abrir(SheetSerieB.IDENTIFICADORES) }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            serie.identificadores.ifBlank { "IMDb" }.replace(" • ", " · ").uppercase(),
                            color = TemaATokens.TextWhite, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, maxLines = 1
                        )
                    }
                }
            }

            // Resumo: três cards (o de minutagem fixa é escuro e abre o pop-up)
            Row(
                Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CardResumoB("${serie.progressoPorcentagem}%", "PROGRESSO", Modifier.weight(1f).entradaB(520), corValor = accent)
                CardResumoB("${serie.episodiosAssistidos}/${serie.totalEpisodios}", "EPISÓDIOS", Modifier.weight(1f).entradaB(570), mono = true)
                CardResumoB(
                    "$ativas/3", "MINUTAGEM FIXA",
                    Modifier.weight(1.25f).entradaB(620),
                    escuro = true, mono = true,
                    onClick = { abrir(SheetSerieB.MINUTAGEM_FIXA) }
                )
            }

            if (serie.sinopse.isNotBlank()) {
                Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp).entradaB(660)) {
                    Text(
                        serie.sinopse,
                        color = TemaATokens.TextoCorpo,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        maxLines = if (sinopseAberta) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (serie.sinopse.length > 140) {
                        Text(
                            if (sinopseAberta) "Ler menos" else "Ler mais",
                            color = accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier
                                .align(Alignment.End)
                                .cliqueTemaB { sinopseAberta = !sinopseAberta }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Row(
                Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp).fillMaxWidth().entradaB(700),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TEMPORADAS", color = TemaATokens.TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp, modifier = Modifier.weight(1f))
                Row(
                    Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .cliqueTemaB(enabled = !isSyncingTmdb, onClick = onSyncTmdbClick)
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSyncingTmdb) {
                        CircularProgressIndicator(Modifier.size(14.dp), color = accent, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Refresh, null, tint = accent, modifier = Modifier.size(14.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text("SINCRONIZAR TMDB", color = accent, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
            }
            Column(
                Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                serie.temporadas.forEachIndexed { i, temp ->
                    Box(
                        Modifier
                            .entradaB(740 + i.coerceAtMost(8) * 50, quique = true)
                            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x22281E14), spotColor = Color(0x22281E14))
                    ) {
                        TemporadaLinhaTemaA(temp) { onTemporadaClick(temp) }
                    }
                }
            }
        }

        // Botões do topo sobre a imagem
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BotaoTopoB(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", onBackClick, fundo = Color(0x80141218))
            BotaoTopoB(Icons.Filled.MoreVert, "Mais opções", { showMenu = true }, ativo = showMenu, fundo = Color(0x80141218))
        }

        // Barra escura flutuante: próximo episódio + CONTINUAR
        if (destino != null) {
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .entradaB(560, 40.dp, quique = true)
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(14.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(BarraEscuraB)
                    .cliqueTemaB(vibrar = true) { onContinueClick(destino) }
                    .padding(start = 20.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("PRÓXIMO", color = TextoSobreEscuroB, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp)
                    Text(destinoLabelTemaA(destino), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 0.6.sp)
                }
                Row(
                    Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent)
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("CONTINUAR", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
                }
            }
        }

        TemaAOverflowMenu(
            visible = showMenu,
            onDismiss = { showMenu = false },
            items = listOf(TemaAMenuItem("Mover para a lixeira", danger = true, onClick = onMoverParaLixeiraClick))
        )

        TemaABottomSheet(visible = sheet != null, onDismiss = { sheet = null }) {
            when (sheet ?: ultimoSheet) {
                SheetSerieB.STATUS -> StatusSheetContentTemaA(
                    nomeSerie = serie.titulo,
                    statusAtual = serie.status,
                    onClose = { sheet = null },
                    onSave = { onSaveStatus(it); sheet = null }
                )
                SheetSerieB.IDENTIFICADORES -> IdentificadoresSheetContentTemaA(
                    nomeSerie = serie.titulo,
                    tmdbId = serie.id,
                    imdbIdAtual = serie.imdbId,
                    tvdbIdAtual = serie.tvdbId,
                    onClose = { sheet = null },
                    onSave = { a, b, c -> onSaveIdentificadores(a, b, c); sheet = null }
                )
                SheetSerieB.MINUTAGEM_FIXA -> MinutagemFixaSheetContentTemaA(
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

private fun corStatusB(status: String): Pair<Color, Color> = when {
    status.equals("Finalizada", true) -> Color(0xFF1B7A4B) to Color(0xFFDDF1E6)
    status.equals("Incompleta", true) -> Color(0xFF5B3FB0) to Color(0xFFE9E2FA)
    status.equals("Em breve", true) -> Color(0xFF1F4FB8) to Color(0xFFDCE7FF)
    else -> Color(0xFF7A5300) to Color(0xFFFBEBC4)
}

@Composable
private fun CardResumoB(
    valor: String,
    rotulo: String,
    modifier: Modifier = Modifier,
    escuro: Boolean = false,
    mono: Boolean = false,
    corValor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier
            .height(64.dp)
            .shadow(if (escuro) 0.dp else 6.dp, RoundedCornerShape(14.dp), ambientColor = Color(0x22281E14), spotColor = Color(0x22281E14))
            .clip(RoundedCornerShape(14.dp))
            .background(if (escuro) BarraEscuraB else TemaATokens.DarkCardSurface)
            .then(if (onClick != null) Modifier.cliqueTemaB(onClick = onClick) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            valor,
            color = corValor ?: if (escuro) Color.White else TemaATokens.TextWhite,
            fontFamily = if (mono) TemaATokens.FontMono else null,
            fontSize = if (mono) 16.sp else 17.sp,
            fontWeight = if (mono) FontWeight.Bold else FontWeight.Black
        )
        Text(
            rotulo,
            color = if (escuro) Color(0xFFC9C3CE) else TemaATokens.TextGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
