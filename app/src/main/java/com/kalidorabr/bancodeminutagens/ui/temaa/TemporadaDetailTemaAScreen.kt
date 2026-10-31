package com.kalidorabr.bancodeminutagens.ui.temaa

import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.foundation.layout.aspectRatio
import com.kalidorabr.bancodeminutagens.ui.temac.ambienteC
import com.kalidorabr.bancodeminutagens.ui.temab.topoEscuroB
import com.kalidorabr.bancodeminutagens.ui.temab.sangrarB
import com.kalidorabr.bancodeminutagens.ui.temab.entradaB
import androidx.compose.foundation.lazy.grid.itemsIndexed
import com.kalidorabr.bancodeminutagens.ui.temaa.components.cliqueTemaA
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalidorabr.bancodeminutagens.model.EpisodioItem
import com.kalidorabr.bancodeminutagens.model.EstadoMinutagem
import com.kalidorabr.bancodeminutagens.model.SerieProgressoUtils
import com.kalidorabr.bancodeminutagens.model.StatusEpisodio
import com.kalidorabr.bancodeminutagens.model.TipoMinutagem
import com.kalidorabr.bancodeminutagens.ui.season.BatchFillRequest
import com.kalidorabr.bancodeminutagens.ui.season.requestApplicable
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaABottomSheet
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAMenuItem
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAOverflowMenu
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAPrimaryButton
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASecondaryButton
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASheetColors

private enum class FiltroEpTemaA(val label: String) { TODOS("Todos"), PENDENTES("Pendentes"), PARCIAIS("Parciais"), FEITOS("Concluídos"), REVISAO("Em revisão") }
private enum class TemporadaSheetTemaA { LOTE, CONFIRMAR_LIMPAR, CONFIRMAR_INTRODB, SEM_CHAVE_INTRODB }

@Composable
fun TemporadaDetailTemaAScreen(
    nomeSerie: String = "",
    nomeTemporada: String = "Temporada 1",
    episodios: List<EpisodioItem>,
    onBackClick: () -> Unit = {},
    onExportJsonClick: () -> Unit = {},
    onBatchFillConfirmed: (BatchFillRequest) -> Unit = {},
    onEpisodioClick: (EpisodioItem) -> Unit = {},
    onLimparEpisodiosConfirmed: (Set<String>) -> Unit = {},
    temChaveIntroDb: Boolean = false,
    onEnviarIntroDbConfirmed: (Set<String>) -> Unit = {},
    noIntroDb: Set<String> = emptySet(),
    emRevisaoIntroDb: Set<String> = emptySet(),
    onConferirIntroDb: () -> Unit = {},
    temporadaAnterior: String? = null,
    temporadaProxima: String? = null,
    onTrocarTemporada: (Int) -> Unit = {}
) {
    val accent = MaterialTheme.colorScheme.primary
    var filtro by remember { mutableStateOf(FiltroEpTemaA.TODOS) }
    var showMenu by remember { mutableStateOf(false) }
    var sheet by remember { mutableStateOf<TemporadaSheetTemaA?>(null) }
    var ultimoSheet by remember { mutableStateOf(TemporadaSheetTemaA.LOTE) }
    var selecionando by remember { mutableStateOf(false) }
    var selecionados by remember { mutableStateOf(emptySet<String>()) }
    // A seleção serve para limpar ou para enviar ao IntroDB
    var selecaoIntroDb by remember { mutableStateOf(false) }

    BackHandler(enabled = selecionando && sheet == null) {
        selecionando = false
        selecionados = emptySet()
    }

    val ordenados = remember(episodios) { episodios.sortedBy { it.numero } }
    val feitos = ordenados.count { it.status == StatusEpisodio.CONCLUIDO }
    val parciais = ordenados.count { it.status == StatusEpisodio.PARCIAL }
    val pendentes = ordenados.size - feitos - parciais
    val porcentagem = SerieProgressoUtils.porcentagem(feitos, ordenados.size)
    val visiveis = when (filtro) {
        FiltroEpTemaA.TODOS -> ordenados
        FiltroEpTemaA.PENDENTES -> ordenados.filter { it.status == StatusEpisodio.PENDENTE || it.status == StatusEpisodio.PROXIMO }
        FiltroEpTemaA.PARCIAIS -> ordenados.filter { it.status == StatusEpisodio.PARCIAL }
        FiltroEpTemaA.FEITOS -> ordenados.filter { it.status == StatusEpisodio.CONCLUIDO }
        FiltroEpTemaA.REVISAO -> ordenados.filter { it.id in emRevisaoIntroDb }
    }

    fun abrir(s: TemporadaSheetTemaA) {
        ultimoSheet = s
        sheet = s
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(TemaATokens.DarkBackground)
            .ambienteC()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .topoEscuroB()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = TemaATokens.SideMargin)
        ) {
            Spacer(Modifier.height(if (TemaVisual.b) 0.dp else 12.dp))
            if (TemaVisual.b) {
                com.kalidorabr.bancodeminutagens.ui.temab.CabecalhoCurvoB(
                    titulo = if (selecionando) "Selecionar" else nomeTemporada,
                    legenda = nomeSerie,
                    modifier = Modifier.sangrarB(TemaATokens.SideMargin),
                    esquerda = {
                        com.kalidorabr.bancodeminutagens.ui.temab.BotaoTopoB(
                            if (selecionando) Icons.Filled.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            if (selecionando) "Cancelar seleção" else "Voltar",
                            onClick = { if (selecionando) { selecionando = false; selecionados = emptySet() } else onBackClick() }
                        )
                    },
                    direita = { com.kalidorabr.bancodeminutagens.ui.temab.BotaoTopoB(Icons.Filled.MoreVert, "Mais opções", { showMenu = true }, ativo = showMenu) }
                )
            } else Row(verticalAlignment = Alignment.CenterVertically) {
                CircleIconButtonTemaA(onClick = {
                    if (selecionando) { selecionando = false; selecionados = emptySet() } else onBackClick()
                }) {
                    Icon(
                        if (selecionando) Icons.Filled.Close else Icons.AutoMirrored.Filled.ArrowBack,
                        if (selecionando) "Cancelar seleção" else "Voltar",
                        tint = TemaATokens.TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(nomeSerie, color = TemaATokens.TextGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        if (selecionando) "Selecionar episódios" else nomeTemporada,
                        color = TemaATokens.TextWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                CircleIconButtonTemaA(
                    onClick = { showMenu = true },
                    background = if (showMenu) TemaATokens.DarkBorder else TemaATokens.DarkCardSurface
                ) {
                    Icon(Icons.Filled.MoreVert, "Mais opções", tint = TemaATokens.TextWhite, modifier = Modifier.size(20.dp))
                }
            }

            // Setas para ir direto à temporada vizinha
            if (!selecionando && (temporadaAnterior != null || temporadaProxima != null)) {
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (temporadaAnterior != null) {
                        Row(
                            Modifier.clip(CircleShape).background(TemaATokens.DarkCardSurface)
                                .clickable { onTrocarTemporada(-1) }.padding(start = 8.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Temporada anterior", tint = TemaATokens.TextWhite, modifier = Modifier.size(20.dp))
                            Text(temporadaAnterior, color = TemaATokens.TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    if (temporadaProxima != null) {
                        Row(
                            Modifier.clip(CircleShape).background(TemaATokens.DarkCardSurface)
                                .clickable { onTrocarTemporada(1) }.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(temporadaProxima, color = TemaATokens.TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Próxima temporada", tint = TemaATokens.TextWhite, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            if (TemaVisual.d) {
                // Tema D: anel grande no centro com os números em volta, como um painel
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$feitos", color = TemaATokens.SemanticSuccess, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Text("concluídos", color = TemaATokens.TextMuted, fontSize = 11.sp)
                    }
                    com.kalidorabr.bancodeminutagens.ui.temad.AnelD(porcentagem / 100f, 108.dp, 9.dp) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$porcentagem%", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                            Text("$feitos de ${ordenados.size}", color = TemaATokens.TextMuted, fontSize = 10.sp)
                        }
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$parciais", color = TemaATokens.SemanticWarning, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Text("parciais", color = TemaATokens.TextMuted, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$pendentes", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Text("pendentes", color = TemaATokens.TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            } else Row(verticalAlignment = Alignment.Bottom) {
                Text("$feitos", color = TemaATokens.TextWhite, fontFamily = TemaATokens.FontMono, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
                Text("/${ordenados.size}", color = TemaATokens.TextMuted, fontFamily = TemaATokens.FontMono, fontSize = 18.sp, modifier = Modifier.padding(bottom = 4.dp))
                Spacer(Modifier.weight(1f))
                Text("$porcentagem% concluído", color = TemaATokens.TextGray, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))
            }
            if (!TemaVisual.d) Spacer(Modifier.height(10.dp))
            if (!TemaVisual.d) BarraSegmentadaTemaA(feitos, parciais, pendentes)
            if (!TemaVisual.d) Spacer(Modifier.height(10.dp))
            if (!TemaVisual.d) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)) {
                LegendaTemaA("Concluídos $feitos", TemaATokens.SemanticSuccess)
                LegendaTemaA("Parciais $parciais", TemaATokens.SemanticWarning)
                LegendaTemaA("Pendentes $pendentes", null)
            }

            Spacer(Modifier.height(20.dp))
            if (TemaVisual.c) {
                // Tema C: pílulas centralizadas; a escolhida fica branca
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                    FiltroEpTemaA.entries.filter { it != FiltroEpTemaA.REVISAO }.forEach { f ->
                        val on = f == filtro
                        Box(
                            Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (on) Color.White else Color.White.copy(alpha = 0.08f))
                                .clickable { filtro = f }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) { Text(f.label, color = if (on) Color(0xFF111111) else TemaATokens.TextGray, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            } else Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(if (TemaVisual.d) 24.dp else 14.dp))
                    .background(TemaATokens.DarkGraphiteSurface)
                    .then(if (TemaVisual.d) Modifier.border(1.dp, TemaATokens.DarkBorder, RoundedCornerShape(24.dp)) else Modifier)
                    .padding(4.dp)
            ) {
                FiltroEpTemaA.entries.filter { it != FiltroEpTemaA.REVISAO }.forEach { f ->
                    val on = f == filtro
                    Box(
                        Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(if (TemaVisual.d) 20.dp else 10.dp))
                            .background(if (on) (if (TemaVisual.d) MaterialTheme.colorScheme.primary else TemaATokens.DarkBorder) else Color.Transparent)
                            .clickable { filtro = f },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            f.label,
                            color = if (on) TemaATokens.TextWhite else TemaATokens.TextGray,
                            fontSize = 13.sp,
                            fontWeight = if (on) FontWeight.Bold else FontWeight.SemiBold
                        )
                    }
                }
            }

            // Só aparece quando há episódios esperando a revisão do IntroDB
            androidx.compose.runtime.LaunchedEffect(emRevisaoIntroDb.isEmpty()) {
                if (emRevisaoIntroDb.isEmpty() && filtro == FiltroEpTemaA.REVISAO) filtro = FiltroEpTemaA.TODOS
            }
            if (emRevisaoIntroDb.isNotEmpty()) {
                val on = filtro == FiltroEpTemaA.REVISAO
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(AzulRevisaoTemaA.copy(alpha = if (on) 0.28f else 0.12f))
                        .clickable { filtro = if (on) FiltroEpTemaA.TODOS else FiltroEpTemaA.REVISAO }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NuvemRevisaoTemaA(Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Em revisão · ${emRevisaoIntroDb.count { id -> ordenados.any { it.id == id } }}",
                        color = TemaATokens.TextWhite, fontSize = 13.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = if (selecionando) 90.dp else 16.dp)
            ) {
                itemsIndexed(visiveis, key = { _, e -> e.id }) { i, ep ->
                    val sel = ep.id in selecionados
                    // Tema B: os episódios "caem" em sequência quando a temporada abre
                    val entrada = if ((TemaVisual.b || TemaVisual.d) && i < 42) Modifier.entradaB(120 + i * (if (TemaVisual.d) 28 else 18), 14.dp, quique = true) else Modifier
                    CelulaEpisodioTemaA(ep, selecionando, sel, Modifier.animateItem().then(entrada), noSite = ep.id in noIntroDb, emRevisao = ep.id in emRevisaoIntroDb) {
                        if (selecionando) {
                            selecionados = if (sel) selecionados - ep.id else selecionados + ep.id
                        } else onEpisodioClick(ep)
                    }
                }
            }
        }

        if (selecionando) {
            val n = selecionados.size
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(TemaATokens.DarkBackground)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                TemaAPrimaryButton(
                    text = when {
                        n == 0 -> "Selecione episódios"
                        selecaoIntroDb -> if (n == 1) "Enviar 1 episódio" else "Enviar $n episódios"
                        n == 1 -> "Limpar 1 episódio"
                        else -> "Limpar $n episódios"
                    },
                    enabled = n > 0,
                    onClick = { abrir(if (selecaoIntroDb) TemporadaSheetTemaA.CONFIRMAR_INTRODB else TemporadaSheetTemaA.CONFIRMAR_LIMPAR) }
                )
            }
        }

        TemaAOverflowMenu(
            visible = showMenu,
            onDismiss = { showMenu = false },
            items = listOf(
                TemaAMenuItem("Preenchimento em lote") { abrir(TemporadaSheetTemaA.LOTE) },
                TemaAMenuItem("Exportar JSON", onClick = onExportJsonClick),
                TemaAMenuItem("Conferir no IntroDB", onClick = onConferirIntroDb),
                TemaAMenuItem("Enviar ao IntroDB") {
                    if (temChaveIntroDb) { selecaoIntroDb = true; selecionando = true; selecionados = emptySet() }
                    else abrir(TemporadaSheetTemaA.SEM_CHAVE_INTRODB)
                },
                TemaAMenuItem("Limpar episódios", danger = true) { selecaoIntroDb = false; selecionando = true; selecionados = emptySet() }
            )
        )

        TemaABottomSheet(visible = sheet != null, onDismiss = { sheet = null }) {
            when (sheet ?: ultimoSheet) {
                TemporadaSheetTemaA.LOTE -> LoteSheetContentTemaA(
                    episodios = ordenados,
                    onClose = { sheet = null },
                    onApply = { onBatchFillConfirmed(it); sheet = null }
                )
                TemporadaSheetTemaA.CONFIRMAR_LIMPAR -> {
                    com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASheetHeader(
                        subtitle = nomeTemporada,
                        title = "Limpar ${selecionados.size} ${if (selecionados.size == 1) "episódio" else "episódios"}?",
                        onClose = { sheet = null }
                    )
                    Text(
                        "As minutagens de abertura, recap e encerramento serão apagadas. Os episódios continuam cadastrados.",
                        color = TemaATokens.TextGray,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                    BarraAcaoTemaA(
                        confirmar = "Limpar",
                        onCancelar = { sheet = null },
                        onConfirmar = {
                            val ids = selecionados
                            sheet = null
                            selecionando = false
                            selecionados = emptySet()
                            onLimparEpisodiosConfirmed(ids)
                        }
                    )
                }
                TemporadaSheetTemaA.CONFIRMAR_INTRODB -> {
                    com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASheetHeader(
                        subtitle = nomeTemporada,
                        title = "Enviar ${selecionados.size} ${if (selecionados.size == 1) "episódio" else "episódios"} ao IntroDB?",
                        onClose = { sheet = null }
                    )
                    Text(
                        buildString {
                            append("Vão só as seções com tempo. As marcadas como \"Não possui\" e as vazias ficam de fora. Nada muda aqui no app nem na planilha.")
                            val jaLa = selecionados.count { it in noIntroDb }
                            if (jaLa > 0) append("\n\n$jaLa dos escolhidos já têm minutagem no IntroDB: as seções que já estão lá não serão enviadas de novo.")
                        },
                        color = TemaATokens.TextGray,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                    BarraAcaoTemaA(
                        confirmar = "Enviar",
                        onCancelar = { sheet = null },
                        onConfirmar = {
                            val ids = selecionados
                            sheet = null
                            selecionando = false
                            selecionados = emptySet()
                            onEnviarIntroDbConfirmed(ids)
                        }
                    )
                }
                TemporadaSheetTemaA.SEM_CHAVE_INTRODB -> {
                    com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASheetHeader(
                        subtitle = "IntroDB",
                        title = "Falta a sua chave",
                        onClose = { sheet = null }
                    )
                    Text(
                        "Crie uma chave no site introdb.app, em Conta, e cole em Ajustes › IntroDB. Depois volte aqui para enviar.",
                        color = TemaATokens.TextGray,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                    com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAPrimaryButton("Entendi", onClick = { sheet = null })
                }
            }
        }
    }
}

/** Botões lado a lado dos pop-ups: Cancelar (cinza) + ação (vermelho). */
@Composable
fun BarraAcaoTemaA(
    confirmar: String,
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit,
    enabled: Boolean = true
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TemaASecondaryButton("Cancelar", onCancelar, Modifier.weight(1f))
        TemaAPrimaryButton(confirmar, onConfirmar, Modifier.weight(1f), enabled = enabled)
    }
}

@Composable
private fun BarraSegmentadaTemaA(feitos: Int, parciais: Int, pendentes: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (feitos > 0) Box(Modifier.weight(feitos.toFloat()).fillMaxSize().background(TemaATokens.SemanticSuccess))
        if (parciais > 0) Box(Modifier.weight(parciais.toFloat()).fillMaxSize().background(TemaATokens.SemanticWarning))
        if (pendentes > 0 || feitos + parciais == 0) {
            Box(Modifier.weight(maxOf(pendentes, 1).toFloat()).fillMaxSize().background(TemaATokens.Track))
        }
    }
}

@Composable
private fun LegendaTemaA(texto: String, cor: Color?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .then(if (cor != null) Modifier.background(cor) else Modifier.border(1.5.dp, TemaATokens.TextMuted, CircleShape))
        )
        Spacer(Modifier.width(6.dp))
        Text(texto, color = TemaATokens.TextGray, fontSize = 12.sp)
    }
}

@Composable
private fun CelulaEpisodioTemaA(ep: EpisodioItem, selecionando: Boolean, selecionado: Boolean, modifier: Modifier, noSite: Boolean = false, emRevisao: Boolean = false, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val shape = if (TemaVisual.d) CircleShape else RoundedCornerShape(12.dp)
    val (bg, fg) = when (ep.status) {
        StatusEpisodio.CONCLUIDO -> TemaATokens.SuccessBg to TemaATokens.SuccessText
        StatusEpisodio.PARCIAL -> TemaATokens.WarningBg to TemaATokens.SemanticWarning
        StatusEpisodio.PROXIMO -> Color.Transparent to TemaATokens.TextWhite
        StatusEpisodio.PENDENTE -> TemaATokens.DarkGraphiteSurface to TemaATokens.TextoApagado
    }
    Box(
        modifier
            .cliqueTemaA(onClick = onClick)
            .then(if (TemaVisual.d) Modifier.aspectRatio(1f) else Modifier.height(48.dp))
            .clip(shape)
            .background(bg)
            .then(
                when {
                    selecionado -> Modifier.border(2.dp, accent, shape)
                    ep.status == StatusEpisodio.PROXIMO && !selecionando -> Modifier.border(2.dp, accent, shape)
                    else -> Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(ep.numeroFormatado, color = fg, fontFamily = TemaATokens.FontMono, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        if (selecionado) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(10.dp))
            }
        } else if (emRevisao) {
            // nuvem azul com "!": enviado ao IntroDB, esperando a revisão do site
            NuvemRevisaoTemaA(Modifier.align(Alignment.BottomEnd).padding(2.dp).size(15.dp))
        } else if (noSite) {
            // nuvem: este episódio já tem minutagem publicada no IntroDB
            Icon(
                Icons.Outlined.CloudDone, "Já no IntroDB", tint = fg.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp).size(15.dp)
            )
        }
    }
}

@Composable
private fun ColumnScope.LoteSheetContentTemaA(
    episodios: List<EpisodioItem>,
    onClose: () -> Unit,
    onApply: (BatchFillRequest) -> Unit
) {
    if (episodios.isEmpty()) {
        Text("Nenhum episódio nesta temporada.", color = TemaATokens.TextGray)
        return
    }
    val accent = MaterialTheme.colorScheme.primary
    val origemPadrao = remember(episodios) {
        episodios.lastOrNull { e -> e.minutagens.any { it.estado != EstadoMinutagem.PENDENTE } } ?: episodios.first()
    }
    var origem by remember { mutableStateOf(origemPadrao) }
    var de by remember { mutableStateOf(episodios.firstOrNull { it.numero > origemPadrao.numero } ?: episodios.first()) }
    var ate by remember { mutableStateOf(episodios.last()) }
    var tipos by remember { mutableStateOf(TipoMinutagem.entries.toSet()) }
    var substituir by remember { mutableStateOf(false) }

    val lo = minOf(de.numero, ate.numero)
    val hi = maxOf(de.numero, ate.numero)
    val alvos = episodios.filter { it.numero in lo..hi && it.id != origem.id }
    val tiposValidos = tipos.filter { t -> origem.minutagens.firstOrNull { it.tipo == t }?.estado.let { it != null && it != EstadoMinutagem.PENDENTE } }.toSet()
    val afetados = alvos.filter { requestApplicable(it, tiposValidos, substituir) }
    val pode = afetados.isNotEmpty() && tiposValidos.isNotEmpty()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Preenchimento em lote", color = TemaATokens.TextWhite, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text("Copie minutagens para vários episódios", color = TemaATokens.TextGray, fontSize = 12.sp)
        }
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(TemaASheetColors.CloseButton).clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.Close, "Fechar", tint = TemaATokens.TextWhite, modifier = Modifier.size(18.dp)) }
    }

    OverlineTemaA("Origem e intervalo")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SeletorEpTemaA("Copiar de", origem, episodios, Modifier.weight(1f)) { origem = it }
        SeletorEpTemaA("Do episódio", de, episodios, Modifier.weight(1f)) { de = it }
        SeletorEpTemaA("Até o episódio", ate, episodios, Modifier.weight(1f)) { ate = it }
    }

    OverlineTemaA("O que copiar")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(TipoMinutagem.ABERTURA to "Abertura", TipoMinutagem.RECAP to "Recap", TipoMinutagem.ENCERRAMENTO to "Encerr.").forEach { (t, nome) ->
            val on = t in tipos
            Row(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (on) TemaATokens.ErroFundo else TemaASheetColors.CloseButton)
                    .then(if (on) Modifier.border(1.dp, accent, RoundedCornerShape(14.dp)) else Modifier)
                    .clickable { tipos = if (on) tipos - t else tipos + t },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (on) accent else Color.Transparent)
                        .then(if (on) Modifier else Modifier.border(1.5.dp, TemaATokens.TextMuted, RoundedCornerShape(4.dp))),
                    contentAlignment = Alignment.Center
                ) { if (on) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(12.dp)) }
                Spacer(Modifier.width(6.dp))
                Text(nome, color = if (on) accent else TemaATokens.TextSoft, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    OverlineTemaA("Como aplicar")
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TemaASheetColors.Group)
            .padding(4.dp)
    ) {
        listOf(false to "Só campos vazios", true to "Substituir tudo").forEach { (valor, nome) ->
            val on = substituir == valor
            Box(
                Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (on) TemaATokens.DarkBorder else Color.Transparent)
                    .clickable { substituir = valor },
                contentAlignment = Alignment.Center
            ) {
                Text(nome, color = if (on) accent else TemaATokens.TextGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    val pulados = alvos.size - afetados.size
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (pode) TemaASheetColors.Group else TemaATokens.PerigoFundo)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            if (pode) "${afetados.size} ${if (afetados.size == 1) "episódio será preenchido" else "episódios serão preenchidos"}"
            else "Nada para aplicar",
            color = TemaATokens.TextWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            when {
                tiposValidos.isEmpty() -> "O episódio de origem não tem as seções escolhidas preenchidas."
                pulados > 0 -> "$pulados já preenchido(s) serão mantidos."
                else -> "Copiando de E${origem.numeroFormatado} para E${lo.toString().padStart(2, '0')}–E${hi.toString().padStart(2, '0')}."
            },
            color = TemaATokens.TextoCorpo,
            fontSize = 12.sp
        )
    }

    BarraAcaoTemaA(
        confirmar = "Aplicar em lote",
        enabled = pode,
        onCancelar = onClose,
        onConfirmar = {
            onApply(BatchFillRequest(origem.id, afetados.map { it.id }.toSet(), tiposValidos, substituir))
        }
    )
}

@Composable
private fun SeletorEpTemaA(
    label: String,
    valor: EpisodioItem,
    opcoes: List<EpisodioItem>,
    modifier: Modifier = Modifier,
    onSelect: (EpisodioItem) -> Unit
) {
    var aberto by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = TemaATokens.TextGray, fontSize = 11.sp, maxLines = 1)
        Box {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(TemaASheetColors.CloseButton)
                    .clickable { aberto = true }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "E${valor.numeroFormatado}",
                    color = TemaATokens.TextWhite,
                    fontFamily = TemaATokens.FontMono,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
                Icon(Icons.Filled.KeyboardArrowDown, null, tint = TemaATokens.TextGray, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(
                expanded = aberto,
                onDismissRequest = { aberto = false },
                containerColor = TemaASheetColors.Group
            ) {
                opcoes.forEach { ep ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "E${ep.numeroFormatado}",
                                color = if (ep.id == valor.id) MaterialTheme.colorScheme.primary else TemaATokens.TextWhite,
                                fontFamily = TemaATokens.FontMono
                            )
                        },
                        onClick = { onSelect(ep); aberto = false }
                    )
                }
            }
        }
    }
}

/** Azul-claro da nuvem "em revisão" (não usa amarelo, que já é o "parcial"). */
val AzulRevisaoTemaA: Color get() = if (TemaVisual.b) Color(0xFF0B7FA8) else Color(0xFF4CC9F0)

/** Nuvem com "!" de "enviado ao IntroDB, em revisão". */
@Composable
fun NuvemRevisaoTemaA(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Icon(Icons.Outlined.Cloud, "Em revisão no IntroDB", tint = AzulRevisaoTemaA, modifier = Modifier.fillMaxSize())
        Text("!", color = AzulRevisaoTemaA, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 2.dp))
    }
}
