package com.kalidorabr.bancodeminutagens.ui.temad

import com.kalidorabr.bancodeminutagens.ui.temac.ambienteC
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalidorabr.bancodeminutagens.model.DestinoEpisodio
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.model.SerieProgressoUtils
import com.kalidorabr.bancodeminutagens.model.TemporadaItem
import com.kalidorabr.bancodeminutagens.ui.temaa.IdentificadoresSheetContentTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.MinutagemFixaSheetContentTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.StatusSheetContentTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaATokens
import com.kalidorabr.bancodeminutagens.ui.temaa.destinoLabelTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaABottomSheet
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAMenuItem
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAOverflowMenu

private enum class SheetSerieD { STATUS, IDENTIFICADORES, MINUTAGEM_FIXA }

/** Série no Tema D (canvas "D · Série"): pôster com neon ao centro, três anéis de números e temporadas com anel. */
@Composable
fun SerieDetailTemaDScreen(
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
    var sheet by remember { mutableStateOf<SheetSerieD?>(null) }
    var ultimo by remember { mutableStateOf(SheetSerieD.STATUS) }
    var menu by remember { mutableStateOf(false) }
    var sinopseAberta by remember { mutableStateOf(false) }
    val accent = MaterialTheme.colorScheme.primary
    val destino = remember(serie) { SerieProgressoUtils.encontrarProximoEpisodio(serie) }
    val ativas = listOf(serie.aberturaAtiva, serie.recapAtivo, serie.encerramentoAtivo).count { it }
    fun abrir(s: SheetSerieD) { ultimo = s; sheet = s }

    Box(Modifier.fillMaxSize().background(TemaATokens.DarkBackground).ambienteC()) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 60.dp, bottom = 150.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PosterNeonD(serie.posterUrl.ifBlank { serie.imageUrl }, 132.dp, 192.dp, 18.dp, true, "Pôster de ${serie.titulo}")
            Spacer(Modifier.height(4.dp))
            Text(serie.titulo, color = Color.White, fontSize = 24.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            val temps = serie.temporadas.size
            Text(
                listOfNotNull(serie.ano.takeIf { it > 0 }?.toString(), "$temps ${if (temps == 1) "temporada" else "temporadas"}", "${serie.totalEpisodios} ep.").joinToString(" · "),
                color = TemaATokens.TextMuted, fontSize = 12.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val (cor, nome) = corStatusD(serie.status)
                Chip("● ${nome}", cor) { abrir(SheetSerieD.STATUS) }
                Chip(serie.identificadores.ifBlank { "IMDb" }.replace(" • ", " · "), TemaATokens.TextGray) { abrir(SheetSerieD.IDENTIFICADORES) }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(TemaATokens.DarkCardSurface)
                    .border(1.dp, TemaATokens.DarkBorder, RoundedCornerShape(24.dp))
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Mini(serie.progressoFloat, "${serie.progressoPorcentagem}%", "Progresso", accent)
                Mini(serie.progressoFloat, "${serie.episodiosAssistidos}/${serie.totalEpisodios}", "Episódios", CianoD)
                Mini(ativas / 3f, "$ativas/3", "Minutagem fixa", LilasD) { abrir(SheetSerieD.MINUTAGEM_FIXA) }
            }
            if (serie.sinopse.isNotBlank()) {
                Text(
                    serie.sinopse,
                    color = TemaATokens.TextGray, fontSize = 13.sp, lineHeight = 20.sp,
                    maxLines = if (sinopseAberta) Int.MAX_VALUE else 3, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().cliqueTemaD { sinopseAberta = !sinopseAberta }
                )
            }
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Temporadas", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Row(
                    Modifier.clip(RoundedCornerShape(16.dp)).cliqueTemaD(enabled = !isSyncingTmdb, onClick = onSyncTmdbClick).padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSyncingTmdb) CircularProgressIndicator(Modifier.size(14.dp), color = CianoD, strokeWidth = 2.dp)
                    else Icon(Icons.Filled.Refresh, null, tint = CianoD, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Atualizar episódios", color = CianoD, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            serie.temporadas.forEach { temp ->
                val numero = SerieProgressoUtils.numeroTemporada(temp.numero)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(TemaATokens.DarkCardSurface)
                        .border(1.dp, TemaATokens.DarkBorder, RoundedCornerShape(22.dp))
                        .cliqueTemaD { onTemporadaClick(temp) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnelD(temp.progressoFloat, 52.dp, 5.dp) {
                        Text(numero?.let { "T" + it.toString().padStart(2, '0') } ?: temp.numero, color = Color.White, fontFamily = TemaATokens.FontMono, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(temp.nome.ifBlank { "Temporada ${numero ?: ""}" }, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("${temp.episodiosConcluidos} de ${temp.totalEpisodios} episódios", color = TemaATokens.TextMuted, fontSize = 12.sp)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TemaATokens.TextMuted)
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BotaoRedondo(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", onBackClick)
            BotaoRedondo(Icons.Filled.MoreVert, "Mais opções", { menu = true })
        }

        // Continuar: botão redondo dentro do anel de progresso da série
        if (destino != null) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, TemaATokens.DarkBackground)))
            )
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .shadow(16.dp, RoundedCornerShape(40.dp))
                    .clip(RoundedCornerShape(40.dp))
                    .background(TemaATokens.DarkCardSurface)
                    .border(1.dp, TemaATokens.DarkBorder, RoundedCornerShape(40.dp))
                    .cliqueTemaD(vibrar = true) { onContinueClick(destino) }
                    .padding(start = 6.dp, end = 22.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnelD(serie.progressoFloat, 64.dp, 4.dp) {
                    Box(Modifier.size(48.dp).clip(CircleShape).background(accent), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Continuar", color = TemaATokens.TextMuted, fontSize = 12.sp)
                    Text(destinoLabelTemaA(destino), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        TemaAOverflowMenu(visible = menu, onDismiss = { menu = false }, items = listOf(TemaAMenuItem("Mover para a lixeira", danger = true, onClick = onMoverParaLixeiraClick)))

        TemaABottomSheet(visible = sheet != null, onDismiss = { sheet = null }) {
            when (sheet ?: ultimo) {
                SheetSerieD.STATUS -> StatusSheetContentTemaA(serie.titulo, serie.status, onClose = { sheet = null }, onSave = { onSaveStatus(it); sheet = null })
                SheetSerieD.IDENTIFICADORES -> IdentificadoresSheetContentTemaA(serie.titulo, serie.id, serie.imdbId, serie.tvdbId, onClose = { sheet = null }, onSave = { a, b, c -> onSaveIdentificadores(a, b, c); sheet = null })
                SheetSerieD.MINUTAGEM_FIXA -> MinutagemFixaSheetContentTemaA(
                    serie.titulo, serie.aberturaAtiva, serie.recapAtivo, serie.encerramentoAtivo,
                    serie.duracaoFixaAbertura, serie.duracaoFixaRecap, serie.duracaoFixaEncerramento,
                    onClose = { sheet = null },
                    onSave = { a, b, c, d, e, f -> onSaveMinutagemFixa(a, b, c, d, e, f); sheet = null }
                )
            }
        }
    }
}

private fun corStatusD(status: String): Pair<Color, String> = when {
    status.equals("Finalizada", true) -> VerdeD to "Finalizada"
    status.equals("Incompleta", true) -> LilasD to "Incompleta"
    status.equals("Em breve", true) -> Color(0xFF5B8CFF) to "Não iniciada"
    else -> AmareloD to status.ifBlank { "Em andamento" }
}

@Composable
private fun Chip(texto: String, cor: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(cor.copy(alpha = 0.1f))
            .border(1.dp, cor.copy(alpha = 0.4f), RoundedCornerShape(15.dp))
            .cliqueTemaD(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) { Text(texto, color = cor, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
}

@Composable
private fun Mini(progresso: Float, valor: String, rotulo: String, cor: Color, onClick: (() -> Unit)? = null) {
    Column(
        Modifier.clip(RoundedCornerShape(16.dp)).then(if (onClick != null) Modifier.cliqueTemaD(onClick = onClick) else Modifier).padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AnelD(progresso, 70.dp, 6.dp, cor) { Text(valor, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold) }
        Text(rotulo, color = TemaATokens.TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun BotaoRedondo(icone: ImageVector, descricao: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(TemaATokens.DarkBackground.copy(alpha = 0.6f))
            .border(1.dp, TemaATokens.DarkBorder, CircleShape)
            .cliqueTemaD(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icone, descricao, tint = Color.White, modifier = Modifier.size(20.dp)) }
}
