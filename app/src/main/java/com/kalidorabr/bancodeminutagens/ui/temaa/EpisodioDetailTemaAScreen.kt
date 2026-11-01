package com.kalidorabr.bancodeminutagens.ui.temaa

import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.kalidorabr.bancodeminutagens.ui.temaa.components.cliqueTemaA
import androidx.compose.material.icons.filled.Add
import com.kalidorabr.bancodeminutagens.ui.temac.ambienteC
import com.kalidorabr.bancodeminutagens.ui.temab.topoEscuroB
import com.kalidorabr.bancodeminutagens.ui.temab.carimboB
import com.kalidorabr.bancodeminutagens.ui.temab.sangrarB
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalidorabr.bancodeminutagens.model.EstadoMinutagem
import com.kalidorabr.bancodeminutagens.model.MinutagemSecao
import com.kalidorabr.bancodeminutagens.model.SerieProgressoUtils
import com.kalidorabr.bancodeminutagens.model.TipoMinutagem
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAMenuItem
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAOverflowMenu
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaABottomSheet
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAPrimaryButton
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASheetColors
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASheetHeader

fun nomeSecaoTemaA(tipo: TipoMinutagem) = when (tipo) {
    TipoMinutagem.ABERTURA -> "Abertura"
    TipoMinutagem.RECAP -> "Recap"
    TipoMinutagem.ENCERRAMENTO -> "Encerramento"
}

@Composable
fun EpisodioDetailTemaAScreen(
    nomeSerie: String = "",
    temporadaLabel: String = "T01",
    numeroEpisodio: Int = 1,
    tituloEpisodio: String = "",
    dataExibicao: String = "",
    statusEpisodioLabel: String = "Pendente",
    minutagens: List<MinutagemSecao>,
    isAnteriorEnabled: Boolean = true,
    isProximoEnabled: Boolean = true,
    isCopyEnabled: Boolean = true,
    copySourceLabel: String = "",
    onBackClick: () -> Unit = {},
    onCopyClick: (TipoMinutagem?) -> Unit = {},
    onSecaoClick: (MinutagemSecao) -> Unit = {},
    onAnteriorClick: () -> Unit = {},
    onProximoClick: () -> Unit = {},
    secaoEditando: MinutagemSecao? = null,
    duracaoFixaEditando: String = "",
    onSaveSecao: (MinutagemSecao) -> Unit = {},
    onDismissEdit: () -> Unit = {},
    minutagensAnterior: List<MinutagemSecao> = emptyList(),
    rotuloAnterior: String = "",
    rotuloProximo: String = "",
    duracaoTipicaEditando: Int? = null,
    onConcluirClick: () -> Unit = onBackClick,
    introDbResumo: String? = null,
    statusPlanilha: String? = null,
    statusIntroDb: String? = null,
    onTocarPlanilha: () -> Unit = {},
    onTocarIntroDb: () -> Unit = {},
    temporadaCompleta: Boolean = false,
    rotuloProximaTemporada: String = "",
    onProximaTemporada: () -> Unit = {},
    onIrProximoPendente: () -> Unit = {},
    onVoltarTemporada: () -> Unit = onBackClick,
    onUsarIntroDb: () -> Unit = {},
    duracaoEpisodioMin: Int = 0
) {
    val tNum = SerieProgressoUtils.numeroTemporada(temporadaLabel) ?: 1
    val rotulo = "T${tNum.toString().padStart(2, '0')} · E${numeroEpisodio.toString().padStart(2, '0')}"
    val ordem = listOf(TipoMinutagem.ABERTURA, TipoMinutagem.RECAP, TipoMinutagem.ENCERRAMENTO)
    val secoes = ordem.map { t -> minutagens.firstOrNull { it.tipo == t } ?: MinutagemSecao(tipo = t) }
    var ultimaEditada by remember { mutableStateOf<MinutagemSecao?>(null) }
    var menuCopiar by remember { mutableStateOf(false) }
    // Cópia pedida que vai substituir dados já existentes: pede confirmação (tipo nulo = tudo)
    var confirmarCopia by remember { mutableStateOf<Pair<TipoMinutagem?, Boolean>?>(null) }
    var ultimaCopia by remember { mutableStateOf<TipoMinutagem?>(null) }
    fun temDados(tipo: TipoMinutagem?) = minutagens.any { secao ->
        (tipo == null || secao.tipo == tipo) &&
            (secao.estado != EstadoMinutagem.PENDENTE || secao.tempoInicio.isNotBlank() || secao.tempoFim.isNotBlank())
    }
    fun pedirCopia(tipo: TipoMinutagem?) {
        if (temDados(tipo)) {
            ultimaCopia = tipo
            confirmarCopia = tipo to true
        } else onCopyClick(tipo)
    }
    if (secaoEditando != null) ultimaEditada = secaoEditando

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = TemaATokens.SideMargin)
                .padding(top = if (TemaVisual.b) 0.dp else 12.dp, bottom = if (TemaVisual.d) 96.dp else 172.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (TemaVisual.b) {
                com.kalidorabr.bancodeminutagens.ui.temab.CabecalhoCurvoB(
                    titulo = "Episódio $numeroEpisodio",
                    legenda = "$rotulo · $nomeSerie",
                    modifier = Modifier.sangrarB(TemaATokens.SideMargin),
                    esquerda = { com.kalidorabr.bancodeminutagens.ui.temab.BotaoTopoB(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", onBackClick) },
                    direita = { com.kalidorabr.bancodeminutagens.ui.temab.BotaoTopoB(Icons.Filled.MoreVert, "Mais opções", { menuCopiar = true }, ativo = menuCopiar) }
                )
            } else Row(verticalAlignment = Alignment.CenterVertically) {
                CircleIconButtonTemaA(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = TemaATokens.TextWhite, modifier = Modifier.size(20.dp))
                }
                Text(
                    "$nomeSerie · Temporada $tNum",
                    color = TemaATokens.TextGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                CircleIconButtonTemaA(
                    onClick = { menuCopiar = true },
                    background = if (menuCopiar) TemaATokens.DarkBorder else TemaATokens.DarkCardSurface
                ) {
                    Icon(Icons.Filled.MoreVert, "Mais opções", tint = TemaATokens.TextWhite, modifier = Modifier.size(20.dp))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // No Tema B o cabeçalho curvo já mostra o número do episódio
                val linha = listOfNotNull(
                    if (TemaVisual.b) null else "Episódio $numeroEpisodio",
                    dataExibicao.takeIf { it.isNotBlank() }
                ).joinToString(" · ")
                if (linha.isNotBlank()) Text(linha, color = TemaATokens.TextGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(
                    tituloEpisodio.ifBlank { "Episódio $numeroEpisodio" },
                    color = TemaATokens.TextWhite,
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                EtiquetasEpisodioTemaA(statusEpisodioLabel, statusPlanilha, statusIntroDb, temporadaCompleta, onTocarPlanilha, onTocarIntroDb)
            }

            val concluido = secoes.all { it.estado != EstadoMinutagem.PENDENTE }

            if (TemaVisual.d) {
                com.kalidorabr.bancodeminutagens.ui.temad.AnelSecoesD(
                    marcadas = secoes.map { it.estado != EstadoMinutagem.PENDENTE },
                    chaveEpisodio = numeroEpisodio
                )
            } else LinhaDoTempoTemaA(secoes, duracaoEpisodioMin * 60)

            if (introDbResumo != null) CartaoIntroDbTemaA(introDbResumo, onUsarIntroDb)

            Column(
                if (TemaVisual.d) Modifier else Modifier.fillMaxWidth().clip(TemaATokens.ShapeCardCommon).background(TemaATokens.DarkGraphiteSurface),
                verticalArrangement = Arrangement.spacedBy(if (TemaVisual.d) 8.dp else 0.dp)
            ) {
                secoes.forEachIndexed { i, s ->
                    if (i > 0 && !TemaVisual.d) Box(Modifier.fillMaxWidth().height(1.dp).background(TemaATokens.DarkBorder))
                    val anterior = minutagensAnterior.firstOrNull { it.tipo == s.tipo }
                    val podeCopiar = isCopyEnabled && s.estado == EstadoMinutagem.PENDENTE &&
                        anterior != null && anterior.estado != EstadoMinutagem.PENDENTE
                    Box(if (TemaVisual.b) Modifier.carimboB(s.estado to s.tempoInicio) else Modifier) {
                        CartaoSecaoTemaA(
                            s,
                            copiarRotulo = if (podeCopiar) rotuloAnterior else null,
                            onCopiar = { onCopyClick(s.tipo) }
                        ) { onSecaoClick(s) }
                    }
                }
            }
        }

        // Botão principal: com o episódio completo, leva ao que falta. Passa pelo mesmo envio do Concluir.
        val concluidoAgora = secoes.all { it.estado != EstadoMinutagem.PENDENTE }
        val irTemporada = concluidoAgora && temporadaCompleta && rotuloProximaTemporada.isNotBlank()
        val irEpisodio = concluidoAgora && !temporadaCompleta && rotuloProximo.isNotBlank()
        val textoPrincipal = when {
            irTemporada -> "Ir para a $rotuloProximaTemporada"
            irEpisodio -> "Ir para o $rotuloProximo"
            else -> "Concluir"
        }
        val acaoPrincipal = when {
            irTemporada -> onProximaTemporada
            irEpisodio -> onIrProximoPendente
            else -> onConcluirClick
        }
        val iconeConcluir: (@Composable () -> Unit)? = if (irTemporada || irEpisodio) null else {
            { Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
        }
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(TemaATokens.DarkBackground)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (TemaVisual.d) {
                val redondo: @Composable (androidx.compose.ui.graphics.vector.ImageVector, String, Boolean, () -> Unit) -> Unit = { icone, desc, on, acao ->
                    Box(
                        Modifier.size(56.dp).clip(CircleShape).background(TemaATokens.DarkCardSurface)
                            .border(1.dp, TemaATokens.DarkBorder, CircleShape).cliqueTemaA(enabled = on, onClick = acao),
                        contentAlignment = Alignment.Center
                    ) { Icon(icone, desc, tint = if (on) Color.White else TemaATokens.Desativado, modifier = Modifier.size(22.dp)) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    redondo(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Anterior", isAnteriorEnabled, onAnteriorClick)
                    TemaAPrimaryButton(textoPrincipal, onClick = acaoPrincipal, modifier = Modifier.width(180.dp).clip(RoundedCornerShape(28.dp)), height = 56.dp, leading = iconeConcluir)
                    redondo(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Próximo", isProximoEnabled, onProximoClick)
                }
            } else {
                TemaAPrimaryButton(textoPrincipal, onClick = acaoPrincipal, modifier = Modifier.fillMaxWidth(), leading = iconeConcluir)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BotaoSetaEpTemaA(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Episódio anterior", isAnteriorEnabled, onAnteriorClick)
                    Box(
                        Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(TemaATokens.DarkCardSurface)
                            .clickable(onClick = onVoltarTemporada),
                        contentAlignment = Alignment.Center
                    ) { Text("Voltar à temporada", color = TemaATokens.TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                    BotaoSetaEpTemaA(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Próximo episódio", isProximoEnabled, onProximoClick)
                }
            }
        }

        val origem = rotuloAnterior.ifBlank { copySourceLabel }
        TemaAOverflowMenu(
            visible = menuCopiar,
            onDismiss = { menuCopiar = false },
            items = if (isCopyEnabled) listOf(
                TemaAMenuItem("Copiar tudo do $origem") { pedirCopia(null) },
                TemaAMenuItem("Só a abertura do $origem") { pedirCopia(TipoMinutagem.ABERTURA) },
                TemaAMenuItem("Só o recap do $origem") { pedirCopia(TipoMinutagem.RECAP) },
                TemaAMenuItem("Só o encerramento do $origem") { pedirCopia(TipoMinutagem.ENCERRAMENTO) }
            ) else listOf(TemaAMenuItem("O episódio anterior não tem dados para copiar") {})
        )

        TemaABottomSheet(visible = confirmarCopia != null, onDismiss = { confirmarCopia = null }) {
            val oQue = when (ultimaCopia) {
                TipoMinutagem.ABERTURA -> "A abertura deste episódio será substituída"
                TipoMinutagem.RECAP -> "O recap deste episódio será substituído"
                TipoMinutagem.ENCERRAMENTO -> "O encerramento deste episódio será substituído"
                null -> "As minutagens deste episódio serão substituídas"
            }
            TemaASheetHeader(rotulo, "Substituir dados atuais?", onClose = { confirmarCopia = null })
            Text("$oQue pelos dados do $origem.", color = TemaATokens.TextGray, fontSize = 13.sp, lineHeight = 19.sp)
            BarraAcaoTemaA("Substituir", onCancelar = { confirmarCopia = null }, onConfirmar = {
                confirmarCopia = null
                onCopyClick(ultimaCopia)
            })
        }

        TemaABottomSheet(visible = secaoEditando != null, onDismiss = onDismissEdit) {
            ultimaEditada?.let { s ->
                EditarSecaoSheetContentTemaA(
                    contexto = "$rotulo · ${tituloEpisodio.ifBlank { "Episódio $numeroEpisodio" }}",
                    secao = s,
                    duracaoFixa = duracaoFixaEditando,
                    duracaoTipica = duracaoTipicaEditando,
                    duracaoEpisodioSeg = duracaoEpisodioMin * 60,
                    outrasSecoes = minutagens,
                    onClose = onDismissEdit,
                    onSave = onSaveSecao
                )
            }
        }
    }
}

/** Etiquetas do topo: status, planilha e IntroDB. A cor mostra o estado; com a setinha, tocar faz a ação. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun EtiquetasEpisodioTemaA(
    status: String,
    planilha: String?,
    introDb: String?,
    temporadaCompleta: Boolean,
    onTocarPlanilha: () -> Unit,
    onTocarIntroDb: () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        Modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val (fgStatus, bgStatus) = when (status) {
            "Concluído" -> TemaATokens.SuccessText to TemaATokens.SuccessBg
            "Parcial" -> TemaATokens.SemanticWarning to TemaATokens.WarningBg
            "Próximo" -> TemaATokens.ErroTexto to TemaATokens.ErroFundo
            else -> TemaATokens.TextSoft to TemaATokens.DarkCardSurface
        }
        EtiquetaTemaA(status, status, fgStatus, bgStatus)
        if (temporadaCompleta) EtiquetaTemaA("Temporada completa", "Temporada completa", TemaATokens.SuccessText, TemaATokens.SuccessBg)
        if (planilha != null) {
            val pendente = planilha == "Pendente"
            EtiquetaTemaA(
                "Planilha", "Planilha: $planilha",
                if (pendente) TemaATokens.SemanticWarning else TemaATokens.SuccessText,
                if (pendente) TemaATokens.WarningBg else TemaATokens.SuccessBg,
                if (pendente) onTocarPlanilha else null
            )
        }
        if (introDb != null) {
            val (fg, bg) = when (introDb) {
                "Publicado" -> TemaATokens.SuccessText to TemaATokens.SuccessBg
                "Em revisão" -> AzulRevisaoTemaA to AzulRevisaoTemaA.copy(alpha = 0.14f)
                else -> TemaATokens.TextGray to TemaATokens.DarkCardSurface
            }
            EtiquetaTemaA("IntroDB", "IntroDB: $introDb", fg, bg, if (introDb == "Não enviado") onTocarIntroDb else null)
        }
    }
}

@Composable
private fun EtiquetaTemaA(texto: String, descricao: String, fg: Color, bg: Color, onClick: (() -> Unit)? = null) {
    Text(
        if (onClick != null) "$texto ›" else texto,
        color = fg, fontSize = 12.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics { contentDescription = descricao }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

/** Cor de cada seção. Encerramento: lilás, ou ciano quando a cor de destaque já é roxa. */
@Composable
private fun corSecaoTemaA(tipo: TipoMinutagem): Color {
    val accent = MaterialTheme.colorScheme.primary
    return when (tipo) {
        TipoMinutagem.ABERTURA -> accent
        TipoMinutagem.RECAP -> if (TemaVisual.d) com.kalidorabr.bancodeminutagens.ui.temad.AmareloD else TemaATokens.SemanticWarning
        else -> when {
            TemaVisual.d -> com.kalidorabr.bancodeminutagens.ui.temad.CianoD
            accent.blue > accent.green && accent.red > 0.35f && accent.blue > 0.6f -> if (TemaVisual.b) Color(0xFF0B7FA8) else Color(0xFF4CC9F0)
            TemaVisual.b -> Color(0xFF6D4FC7)
            else -> Color(0xFFA78BFA)
        }
    }
}

@Composable
private fun LinhaDoTempoTemaA(secoes: List<MinutagemSecao>, duracaoSeg: Int = 0) {
    val trechos = secoes.mapNotNull { s ->
        if (s.estado != EstadoMinutagem.PREENCHIDO) return@mapNotNull null
        val a = MinutagemSecao.parseMmSsToSeconds(s.tempoInicio)
        val b = MinutagemSecao.parseMmSsToSeconds(s.tempoFim)
        if (a != null && b != null && b > a) Triple(s.tipo, a, b) else null
    }
    val fimMax = trechos.maxOfOrNull { it.third } ?: 0
    // vai até a duração real do episódio (TMDB); sem ela, 24 minutos
    val escala = maxOf(if (duracaoSeg > 0) duracaoSeg else 24 * 60, fimMax + 60).toFloat()
    val cores = TipoMinutagem.entries.associateWith { corSecaoTemaA(it) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val c = TemaVisual.c
        if (c) {
            // Tema C: nome de cada "capítulo" em cima da barra, como num player
            BoxWithConstraints(Modifier.fillMaxWidth().height(16.dp)) {
                var livreAPartir = (-200).dp
                trechos.sortedBy { it.second }.forEach { (tipo, a, _) ->
                    val x = minOf(maxWidth * (a / escala), maxWidth - 90.dp)
                    // não deixa um nome em cima do outro quando as seções ficam muito próximas
                    if (x < livreAPartir) return@forEach
                    livreAPartir = x + 92.dp
                    Text(
                        nomeSecaoTemaA(tipo),
                        color = if (tipo == TipoMinutagem.RECAP) TemaATokens.SemanticWarning else Color.White,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1,
                        modifier = Modifier.offset(x = x)
                    )
                }
            }
        }
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(if (c) 26.dp else 10.dp)
                .clip(RoundedCornerShape(if (c) 8.dp else 5.dp))
                .background(TemaATokens.Neutro)
        ) {
            if (c) for (i in 1..9) Box(Modifier.offset(x = maxWidth * (i / 10f)).padding(vertical = 8.dp).width(1.dp).fillMaxHeight().background(Color.White.copy(alpha = 0.14f)))
            trechos.forEach { (tipo, a, b) ->
                Box(
                    Modifier
                        .offset(x = maxWidth * (a / escala))
                        .width(maxOf(maxWidth * ((b - a) / escala), 3.dp))
                        .fillMaxHeight()
                        .background(cores.getValue(tipo))
                )
            }
            // cursor branco no fim do último trecho marcado
            if (c) trechos.maxByOrNull { it.third }?.let { (_, _, b) ->
                Box(Modifier.offset(x = maxWidth * (b / escala) - 1.dp).width(3.dp).fillMaxHeight().background(Color.White))
            }
        }
        Row {
            Text("00:00", color = TemaATokens.TextGray, fontFamily = TemaATokens.FontMono, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text(MinutagemSecao.formatSecondsToMmSs(escala.toInt()), color = TemaATokens.TextGray, fontFamily = TemaATokens.FontMono, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CartaoSecaoTemaA(
    s: MinutagemSecao,
    copiarRotulo: String? = null,
    onCopiar: () -> Unit = {},
    onClick: () -> Unit
) {
    val d = TemaVisual.d
    val cor = corSecaoTemaA(s.tipo)
    val nome = nomeSecaoTemaA(s.tipo)
    val forma = if (d) RoundedCornerShape(50) else RoundedCornerShape(0.dp)
    val bolha: @Composable (Boolean) -> Unit = { feita ->
        if (d) Box(
            Modifier.size(34.dp).clip(CircleShape)
                .then(if (feita) Modifier.background(cor) else Modifier.border(2.dp, cor, CircleShape)),
            contentAlignment = Alignment.Center
        ) { Icon(if (feita) Icons.Filled.Check else Icons.Filled.Add, null, tint = if (feita) Color(0xFF0B0918) else cor, modifier = Modifier.size(16.dp)) }
    }
    if (s.estado == EstadoMinutagem.PREENCHIDO) {
        // Preenchida: tudo numa linha só, da mesma altura das pendentes
        Row(
            Modifier
                .fillMaxWidth()
                .height(if (d) 68.dp else 60.dp)
                .clip(forma)
                .background(if (d) TemaATokens.DarkGraphiteSurface else Color.Transparent)
                .then(if (d) Modifier.border(1.dp, TemaATokens.DarkBorder, forma) else Modifier)
                .clickable(onClick = onClick)
                .padding(start = if (d) 12.dp else 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (d) bolha(true) else Box(Modifier.size(10.dp).clip(CircleShape).background(cor))
            Spacer(Modifier.width(10.dp))
            Text(nome, color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(
                "${s.tempoInicio}  →  ${s.tempoFim}",
                color = TemaATokens.TextWhite,
                fontFamily = TemaATokens.FontMono,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else {
        Row(
            Modifier
                .fillMaxWidth()
                .height(if (d) 68.dp else 60.dp)
                .clip(forma)
                .background(if (d) TemaATokens.DarkGraphiteSurface else Color.Transparent)
                .then(if (d) Modifier.border(1.dp, TemaATokens.DarkBorder, forma) else Modifier)
                .clickable(onClick = onClick)
                .padding(start = if (d) 12.dp else 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val naoPossui = s.estado == EstadoMinutagem.NAO_POSSUI
            if (d) bolha(naoPossui) else Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .then(if (naoPossui) Modifier.background(cor) else Modifier.border(2.dp, TemaATokens.TextMuted, CircleShape))
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(nome, color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                if (!naoPossui) Text("Toque para marcar", color = TemaATokens.TextGray, fontSize = 12.sp)
            }
            if (naoPossui) {
                Text("Não possui", color = TemaATokens.TextGray, fontSize = 13.sp)
                Spacer(Modifier.width(8.dp))
            } else if (copiarRotulo != null) {
                // O bloco inteiro abre o teclado; aqui fica só o botão de copiar do episódio anterior
                Box(Modifier.width(1.dp).height(36.dp).background(TemaATokens.DarkBorder))
                Spacer(Modifier.width(10.dp))
                Column(
                    Modifier
                        .size(width = 54.dp, height = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TemaATokens.ErroFundo)
                        .clickable(onClick = onCopiar),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.ContentCopy, "Igual ao $copiarRotulo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text(copiarRotulo, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BotaoSetaEpTemaA(icone: androidx.compose.ui.graphics.vector.ImageVector, descricao: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(width = 54.dp, height = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(TemaATokens.DarkCardSurface)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icone, descricao, tint = if (enabled) TemaATokens.TextWhite else TemaATokens.Desativado, modifier = Modifier.size(22.dp)) }
}

/** Pop-up "Editar abertura/recap/encerramento" com teclado numérico próprio. */
@Composable
fun ColumnScope.EditarSecaoSheetContentTemaA(
    contexto: String,
    secao: MinutagemSecao,
    duracaoFixa: String,
    duracaoTipica: Int? = null,
    outrasSecoes: List<MinutagemSecao> = emptyList(),
    duracaoEpisodioSeg: Int = 0,
    onClose: () -> Unit,
    onSave: (MinutagemSecao) -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    // Aviso de tempo estranho: aparece ao tocar em Salvar; tocar de novo salva assim mesmo.
    var avisoMostrado by remember(secao) { mutableStateOf<String?>(null) }
    var ini by remember(secao) { mutableStateOf(digitosTempoTemaA(secao.tempoInicio)) }
    var fim by remember(secao) { mutableStateOf(digitosTempoTemaA(secao.tempoFim)) }
    var naoPossui by remember(secao) { mutableStateOf(secao.estado == EstadoMinutagem.NAO_POSSUI) }
    var campo by remember(secao) { mutableStateOf(if (secao.tempoInicio.isBlank()) "ini" else "fim") }
    val temFixa = duracaoFixa.isNotBlank() && !duracaoFixa.equals("Não possui", true)

    fun fmt(d: String) = formatarTempoTemaA(d)
    val a = segundosTempoTemaA(ini)
    val b = segundosTempoTemaA(fim)
    val segundosValidos = segundosValidosTemaA(ini) && segundosValidosTemaA(fim)
    val dur = if (a != null && b != null && b > a) MinutagemSecao.formatSecondsToMmSs(b - a) else "--:--"
    val podeSalvar = naoPossui || (a != null && b != null && b >= a && segundosValidos)
    // Apagou início e fim de um trecho que tinha tempo: Confirmar (cinza) deixa só esse trecho vazio
    val limpando = !naoPossui && ini.isEmpty() && fim.isEmpty() && secao.estado != EstadoMinutagem.PENDENTE

    fun tecla(k: String) {
        if (naoPossui) return
        avisoMostrado = null
        if (campo == "ini") {
            val novo = teclaTempoTemaA(ini, k)
            ini = novo
            // Limpar no início com minutagem fixa apaga o fim calculado junto
            if (temFixa && k == "Limpar") fim = ""
            // Com minutagem fixa, o fim é calculado sozinho a partir do início.
            if (temFixa && novo.isNotEmpty() && segundosValidosTemaA(novo)) {
                MinutagemSecao.somarTempos(fmt(novo), duracaoFixa)?.let { fim = digitosTempoTemaA(it) }
            }
        } else {
            fim = teclaTempoTemaA(fim, k)
        }
    }

    TemaASheetHeader(contexto, "Editar ${nomeSecaoTemaA(secao.tipo).lowercase()}", onClose)

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf("ini" to "Início", "fim" to "Fim").forEach { (id, rot) ->
            val on = campo == id && !naoPossui
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(TemaASheetColors.Group)
                    .border(2.dp, if (on) accent else Color.Transparent, RoundedCornerShape(18.dp))
                    .clickable(enabled = !naoPossui) { campo = id }
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(rot, color = TemaATokens.TextGray, fontSize = 12.sp)
                Text(
                    fmt(if (id == "ini") ini else fim),
                    color = TemaATokens.TextWhite.copy(alpha = if (naoPossui) 0.35f else 1f),
                    fontFamily = TemaATokens.FontMono,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(TemaASheetColors.Group)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Duração", color = TemaATokens.TextGray, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text(if (naoPossui) "--:--" else dur, color = TemaATokens.TextWhite, fontFamily = TemaATokens.FontMono, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Row(
            Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(TemaASheetColors.Group)
                .clickable { naoPossui = !naoPossui },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (naoPossui) accent else Color.Transparent)
                    .border(2.dp, if (naoPossui) accent else TemaATokens.Desativado, RoundedCornerShape(5.dp)),
                contentAlignment = Alignment.Center
            ) { if (naoPossui) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(12.dp)) }
            Spacer(Modifier.width(8.dp))
            Text("Não possui", color = if (naoPossui) accent else TemaATokens.TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (!segundosValidos) {
        Text("Os segundos vão até 59.", color = TemaATokens.ErroTexto, fontSize = 12.sp)
    } else if (a != null && b != null && b < a) {
        Text("O fim não pode ser antes do início.", color = TemaATokens.ErroTexto, fontSize = 12.sp)
    }

    avisoMostrado?.let { AvisoTempoTemaA(it) }

    TecladoTempoTemaA(enabled = !naoPossui) { tecla(it) }

    // Sem minutagem fixa: primeiro confirma o início ("Continuar"), depois o fim ("Salvar").
    // Com minutagem fixa o fim já vem calculado, então vai direto para "Salvar".
    val etapaInicio = !naoPossui && campo == "ini" && !(temFixa && fim.isNotEmpty())
    if (limpando) {
        Text("Início e fim vazios: confirmar deixa este trecho sem tempo.", color = TemaATokens.TextGray, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASecondaryButton("Cancelar", onClose, Modifier.weight(1f))
            com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASecondaryButton("Confirmar", {
                onSave(secao.copy(
                    estado = EstadoMinutagem.PENDENTE, tempoInicio = "", tempoFim = "",
                    isFilled = false, hasNot = false, confirmadaIndividualmente = false
                ))
            }, Modifier.weight(1f))
        }
        return
    }
    BarraAcaoTemaA(
        confirmar = when {
            etapaInicio -> "Continuar"
            avisoMostrado != null -> "Salvar assim"
            else -> "Salvar"
        },
        enabled = if (etapaInicio) ini.isNotEmpty() && segundosValidosTemaA(ini) else podeSalvar,
        onCancelar = onClose,
        onConfirmar = confirmar@{
            if (etapaInicio) {
                campo = "fim"
                return@confirmar
            }
            if (!naoPossui && avisoMostrado == null) {
                val aviso = com.kalidorabr.bancodeminutagens.model.AvisoTempo.verificar(secao.tipo, a, b, duracaoTipica, outrasSecoes, duracaoEpisodioSeg)
                if (aviso != null) {
                    avisoMostrado = aviso
                    return@confirmar
                }
            }
            onSave(
                if (naoPossui) secao.copy(
                    estado = EstadoMinutagem.NAO_POSSUI, tempoInicio = "", tempoFim = "",
                    isFilled = true, hasNot = true, confirmadaIndividualmente = true
                ) else secao.copy(
                    estado = EstadoMinutagem.PREENCHIDO, tempoInicio = fmt(ini), tempoFim = fmt(fim),
                    isFilled = true, hasNot = false, confirmadaIndividualmente = true
                )
            )
        }
    )
}

/**
 * Cartão "Disponível no IntroDB": oferece os tempos de lá para as seções vazias.
 * [resumo]: uma linha por seção, "Nome<TAB>tempo" (ex.: "Abertura\t00:01 → 01:34").
 */
@Composable
private fun CartaoIntroDbTemaA(resumo: String, onUsar: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Column(
        Modifier
            .fillMaxWidth()
            .clip(TemaATokens.ShapeCardCommon)
            .background(TemaATokens.DarkGraphiteSurface)
            .border(1.dp, accent.copy(alpha = 0.35f), TemaATokens.ShapeCardCommon)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CloudDownload, null, tint = accent, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text("Disponível no IntroDB", color = TemaATokens.TextWhite, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            Text(
                "Usar", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clip(CircleShape).background(accent).clickable(onClick = onUsar).padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(TemaATokens.DarkBackground)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            resumo.lines().filter { it.isNotBlank() }.forEach { linha ->
                val nome = linha.substringBefore('\t')
                val tempo = linha.substringAfter('\t', "")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(nome, color = TemaATokens.TextGray, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text(
                        tempo,
                        color = if (tempo == "Não possui") TemaATokens.TextMuted else TemaATokens.TextWhite,
                        fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        fontFamily = if (tempo == "Não possui") null else TemaATokens.FontMono
                    )
                }
            }
        }
    }
}

/** Faixa amarela do aviso de tempo estranho (não impede salvar). */
@Composable
private fun AvisoTempoTemaA(texto: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TemaATokens.WarningBg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Warning, null, tint = TemaATokens.SemanticWarning, modifier = Modifier.size(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Confira este tempo", color = TemaATokens.SemanticWarning, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            Text(texto, color = TemaATokens.TextWhite, fontSize = 12.sp, lineHeight = 17.sp)
            Text("Toque em \"Salvar assim\" se estiver certo.", color = TemaATokens.TextGray, fontSize = 11.sp)
        }
    }
}
