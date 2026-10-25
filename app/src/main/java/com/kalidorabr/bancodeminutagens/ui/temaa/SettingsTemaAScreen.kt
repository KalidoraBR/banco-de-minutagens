package com.kalidorabr.bancodeminutagens.ui.temaa

import com.kalidorabr.bancodeminutagens.ui.temab.sangrarB
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.WarningAmber
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kalidorabr.bancodeminutagens.data.VisualMode
import com.kalidorabr.bancodeminutagens.model.MinutagemInconsistente
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.model.SerieProgressoUtils
import com.kalidorabr.bancodeminutagens.model.StatusEpisodio
import com.kalidorabr.bancodeminutagens.ui.settings.historicoVersoesApp
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaABottomSheet
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAGroup
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAGroupDivider
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAPrimaryButton
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASheetColors
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASheetHeader
import com.kalidorabr.bancodeminutagens.ui.theme.AppThemeOpcao
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------- Ajustes ----------

@Composable
fun SettingsTemaAScreen(
    modifier: Modifier = Modifier,
    selectedTheme: AppThemeOpcao = AppThemeOpcao.VERMELHO,
    onThemeSelected: (AppThemeOpcao) -> Unit = {},
    visualMode: VisualMode = VisualMode.TEMA_A,
    onVisualModeSelected: (VisualMode) -> Unit = {},
    onGoogleSheetsClick: () -> Unit = {},
    onLixeiraClick: () -> Unit = {},
    onDadosBackupClick: () -> Unit = {},
    onSobreClick: () -> Unit = {},
    onEstatisticasClick: () -> Unit = {},
    chaveIntroDb: String = "",
    onSalvarChaveIntroDb: (String) -> Unit = {},
    autoIntroDb: Boolean = true,
    onAutoIntroDbChange: (Boolean) -> Unit = {},
    maratona: Boolean = false,
    onMaratonaChange: (Boolean) -> Unit = {},
    avisoEstreia: Boolean = false,
    onAvisoEstreiaChange: (Boolean) -> Unit = {},
    backupAutoPasta: String? = null,
    onBackupAutoChange: (Boolean) -> Unit = {}
) {
    var showInterface by remember { mutableStateOf(false) }
    var showIntroDb by remember { mutableStateOf(false) }
    val accent = MaterialTheme.colorScheme.primary

    Box(modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = TemaATokens.SideMargin)
                .padding(top = if (TemaVisual.b) 0.dp else 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            CabecalhoPaginaTemaA("Ajustes", "Aparência, dados e backup")

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OverlineTemaA("Aparência")
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(TemaATokens.ShapeCardCommon)
                        .background(TemaATokens.DarkGraphiteSurface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row {
                        Text("Cor de destaque", color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(selectedTheme.nome, color = TemaATokens.TextGray, fontSize = 13.sp)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        AppThemeOpcao.entries.forEach { op ->
                            val on = op == selectedTheme
                            val cor = TemaATokens.accentFor(op)
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, if (on) TemaATokens.TextWhite else Color.Transparent, CircleShape)
                                    .padding(5.dp)
                                    .clip(CircleShape)
                                    .background(cor)
                                    .clickable { onThemeSelected(op) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (on) Icon(Icons.Filled.Check, op.nome, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(TemaATokens.ShapeButtonField)
                            .background(accent),
                        contentAlignment = Alignment.Center
                    ) { Text("Prévia do botão", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp) }
                }
                TemaAGroupCard {
                    LinhaAjusteTemaA(Icons.Outlined.Palette, "Interface", "Escolher o tema do aplicativo", visualMode.titulo) { showInterface = true }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OverlineTemaA("Minutagem")
                TemaAGroupCard {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onMaratonaChange(!maratona) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(TemaATokens.DarkCardSurface),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Outlined.FastForward, null, tint = TemaATokens.TextSoft, modifier = Modifier.size(20.dp)) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Modo maratona", color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Concluir abre o próximo episódio", color = TemaATokens.TextGray, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                        androidx.compose.material3.Switch(checked = maratona, onCheckedChange = onMaratonaChange)
                    }
                    TemaAGroupDivider()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onAvisoEstreiaChange(!avisoEstreia) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(TemaATokens.DarkCardSurface),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Outlined.NotificationsActive, null, tint = TemaATokens.TextSoft, modifier = Modifier.size(20.dp)) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Aviso de estreia", color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Avisa no dia da estreia", color = TemaATokens.TextGray, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                        androidx.compose.material3.Switch(checked = avisoEstreia, onCheckedChange = onAvisoEstreiaChange)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OverlineTemaA("Dados")
                TemaAGroupCard {
                    LinhaAjusteTemaA(Icons.Outlined.TableChart, "Google Sheets", "Importar, exportar e sincronizar", onClick = onGoogleSheetsClick)
                    TemaAGroupDivider()
                    LinhaAjusteTemaA(Icons.Outlined.CloudUpload, "IntroDB", "Envio ao introdb.app", if (chaveIntroDb.isBlank()) "Configurar" else "Conectado") { showIntroDb = true }
                    TemaAGroupDivider()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onBackupAutoChange(backupAutoPasta == null) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(TemaATokens.DarkCardSurface),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Outlined.Schedule, null, tint = TemaATokens.TextSoft, modifier = Modifier.size(20.dp)) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Backup automático", color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (backupAutoPasta == null) "Toda semana numa pasta" else "Toda semana em \"$backupAutoPasta\"",
                                color = TemaATokens.TextGray, fontSize = 12.sp,
                                maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        androidx.compose.material3.Switch(checked = backupAutoPasta != null, onCheckedChange = onBackupAutoChange)
                    }
                    TemaAGroupDivider()
                    LinhaAjusteTemaA(Icons.Outlined.Storage, "Dados e backup", "Exportar ou restaurar tudo", onClick = onDadosBackupClick)
                    TemaAGroupDivider()
                    LinhaAjusteTemaA(Icons.Outlined.Delete, "Lixeira", "Séries removidas e restauração", onClick = onLixeiraClick)
                    TemaAGroupDivider()
                    LinhaAjusteTemaA(Icons.Outlined.BarChart, "Estatísticas", "Seu progresso e quanto falta", onClick = onEstatisticasClick)
                    TemaAGroupDivider()
                    LinhaAjusteTemaA(Icons.Outlined.Info, "Sobre", "Versão e histórico de atualizações", onClick = onSobreClick)
                }
            }
        }

        TemaABottomSheet(visible = showIntroDb, onDismiss = { showIntroDb = false }) {
            var chave by remember(showIntroDb) { mutableStateOf(chaveIntroDb) }
            TemaASheetHeader("Ajustes", "IntroDB", onClose = { showIntroDb = false })
            Text(
                "Crie uma chave no site introdb.app, em Conta, e cole aqui. Ela fica guardada só neste aparelho. Para enviar, use \"Enviar ao IntroDB\" no menu ⋮ da temporada.",
                color = TemaATokens.TextGray, fontSize = 13.sp, lineHeight = 19.sp
            )
            CampoTemaA("Chave da API", chave, { chave = it.trim() }, placeholder = "idb_…", secreto = true)
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TemaATokens.DarkBackground)
                    .clickable { onAutoIntroDbChange(!autoIntroDb) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Enviar ao tocar em Concluir", color = TemaATokens.TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Só quando as três seções estiverem preenchidas", color = TemaATokens.TextGray, fontSize = 12.sp)
                }
                androidx.compose.material3.Switch(checked = autoIntroDb, onCheckedChange = onAutoIntroDbChange)
            }
            TemaAPrimaryButton("Salvar", onClick = {
                onSalvarChaveIntroDb(chave)
                showIntroDb = false
            })
        }

        TemaABottomSheet(visible = showInterface, onDismiss = { showInterface = false }) {
            var escolhido by remember(visualMode) { mutableStateOf(visualMode) }
            TemaASheetHeader("Ajustes", "Interface", onClose = { showInterface = false })
            TemaAGroup {
                VisualMode.disponiveis.forEachIndexed { i, m ->
                    if (i > 0) TemaAGroupDivider()
                    val on = m == escolhido
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { escolhido = m }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(m.titulo, color = if (on) accent else TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(m.descricao, color = TemaATokens.TextGray, fontSize = 12.sp)
                        }
                        RadioTemaA(on)
                    }
                }
            }
            TemaAPrimaryButton("Aplicar", onClick = {
                showInterface = false
                if (escolhido != visualMode) onVisualModeSelected(escolhido)
            })
        }
    }
}

@Composable
private fun TemaAGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(TemaATokens.ShapeCardCommon)
            .background(TemaATokens.DarkGraphiteSurface),
        content = content
    )
}

@Composable
private fun LinhaAjusteTemaA(
    icon: ImageVector,
    titulo: String,
    sub: String,
    valor: String? = null,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TemaATokens.DarkCardSurface),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = TemaATokens.TextSoft, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titulo, color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(sub, color = TemaATokens.TextGray, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
        if (valor != null) {
            Text(valor, color = TemaATokens.TextGray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(4.dp))
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TemaATokens.TextMuted)
    }
}

// ---------- Cabeçalho das subtelas ----------

@Composable
fun SubtelaTemaA(
    titulo: String,
    onBackClick: () -> Unit,
    acaoTopo: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(TemaATokens.DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TemaATokens.SideMargin)
            .padding(top = if (TemaVisual.b) 0.dp else 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        if (TemaVisual.b) {
            com.kalidorabr.bancodeminutagens.ui.temab.CabecalhoCurvoB(
                titulo = titulo,
                modifier = Modifier.sangrarB(TemaATokens.SideMargin),
                esquerda = { com.kalidorabr.bancodeminutagens.ui.temab.BotaoTopoB(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", onBackClick) },
                direita = acaoTopo?.let { a -> { androidx.compose.runtime.CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides Color.White) { a() } } }
            )
        } else Row(verticalAlignment = Alignment.CenterVertically) {
            CircleIconButtonTemaA(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = TemaATokens.TextWhite, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(titulo, color = TemaATokens.TextWhite, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            acaoTopo?.invoke()
        }
        content()
    }
}

// ---------- Sobre ----------

@Composable
fun SobreTemaAScreen(onBackClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    var aberta by remember { mutableStateOf(historicoVersoesApp.firstOrNull { it.isAtual }?.versao) }
    val atual = historicoVersoesApp.firstOrNull { it.isAtual }
    SubtelaTemaA("", onBackClick) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(TemaATokens.DarkGraphiteSurface),
                contentAlignment = Alignment.Center
            ) {
                // Mesmo desenho do ícone do app
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(com.kalidorabr.bancodeminutagens.R.drawable.ic_launcher_bm_foreground),
                    contentDescription = "Ícone do Banco de Minutagens",
                    modifier = Modifier.size(132.dp)
                )
            }
            Text("Banco de Minutagens", color = TemaATokens.TextWhite, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text("Seu catálogo de séries, do seu jeito.", color = TemaATokens.TextGray, fontSize = 14.sp)
            if (atual != null) {
                Text(
                    "Versão ${atual.versao} · Atualizada",
                    color = TemaATokens.ErroTexto,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(CircleShape).background(TemaATokens.ErroFundo).padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Histórico de atualizações", color = TemaATokens.TextWhite, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            historicoVersoesApp.forEach { v ->
                val open = aberta == v.versao
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(TemaATokens.ShapeCardCommon)
                        .background(TemaATokens.DarkGraphiteSurface)
                        .clickable { aberta = if (open) null else v.versao }
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Versão ${v.versao}", color = TemaATokens.TextWhite, fontSize = if (open) 16.sp else 15.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                        if (v.isAtual) {
                            Text("Atual", color = TemaATokens.SuccessText, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.clip(CircleShape).background(TemaATokens.SuccessBg).padding(horizontal = 8.dp, vertical = 3.dp))
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(v.data, color = TemaATokens.TextGray, fontFamily = TemaATokens.FontMono, fontSize = 12.sp)
                    }
                    if (open) {
                        Text(
                            v.alteracoes.joinToString("\n") { "• $it" },
                            color = TemaATokens.TextoCorpo,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

// ---------- Dados e backup ----------

@Composable
fun DadosBackupTemaAScreen(
    series: List<SerieItem>,
    isOnline: Boolean,
    hasPendingSync: Boolean,
    lastSyncDate: String?,
    isSyncConfigured: Boolean,
    isAutoSyncEnabled: Boolean,
    lastSyncStatus: String?,
    onBackClick: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onOpenIssue: (MinutagemInconsistente) -> Unit,
    onSyncClick: () -> Unit = {}
) {
    var confirmarRestaurar by remember { mutableStateOf(false) }
    var verProblemas by remember { mutableStateOf(false) }
    val ativas = series.filterNot { it.isDeleted }
    val eps = ativas.flatMap { it.temporadas }.flatMap { it.episodios }
    val feitos = eps.count { it.status == StatusEpisodio.CONCLUIDO }
    val problemas = remember(ativas) { SerieProgressoUtils.listarInconsistencias(ativas) }
    val accent = MaterialTheme.colorScheme.primary

    Box(Modifier.fillMaxSize()) {
        SubtelaTemaA("Dados e backup", onBackClick) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OverlineTemaA("Resumo do catálogo")
                Column(
                    Modifier.fillMaxWidth().clip(TemaATokens.ShapeCardCommon).background(TemaATokens.DarkGraphiteSurface).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row {
                        NumeroTemaA(ativas.size.toString(), "Séries", Modifier.weight(1f))
                        NumeroTemaA(feitos.toString(), "Concluídos", Modifier.weight(1f))
                        NumeroTemaA((eps.size - feitos).toString(), "Pendentes", Modifier.weight(1f))
                    }
                    ProgressLineTemaA(SerieProgressoUtils.progresso(feitos, eps.size), height = 6)
                }
            }

            TemaAGroupCard {
                val ok = isOnline && isSyncConfigured && isAutoSyncEnabled && !hasPendingSync && lastSyncStatus == "Sucesso"
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconeBolhaTemaA(if (ok) Icons.Outlined.CloudDone else Icons.Outlined.CloudOff, ok)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            when {
                                !isSyncConfigured -> "Sincronização não configurada"
                                !isAutoSyncEnabled -> "Sincronização automática desativada"
                                hasPendingSync -> "Sincronização pendente"
                                !isOnline -> "Salvamento local ativo"
                                lastSyncStatus == "Sucesso" -> "Dados sincronizados"
                                else -> "Aguardando primeira sincronização"
                            },
                            color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold
                        )
                        Text(
                            when {
                                !isSyncConfigured -> "Configure o Google Sheets para usar a nuvem"
                                !isAutoSyncEnabled -> "Os dados continuam salvos neste aparelho"
                                hasPendingSync && !isOnline -> "O envio continuará quando a internet voltar"
                                hasPendingSync -> "Alterações aguardando envio"
                                !isOnline -> "A sincronização continuará quando a internet voltar"
                                lastSyncDate != null -> "Última sincronização: $lastSyncDate"
                                else -> "A conexão está pronta para sincronizar"
                            },
                            color = TemaATokens.TextGray, fontSize = 12.sp
                        )
                    }
                    if (hasPendingSync && isSyncConfigured) {
                        Text("Enviar", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.clip(CircleShape).background(accent).clickable(onClick = onSyncClick).padding(horizontal = 14.dp, vertical = 8.dp))
                    }
                }
                TemaAGroupDivider()
                Row(
                    Modifier.fillMaxWidth().clickable(enabled = problemas.isNotEmpty()) { verProblemas = !verProblemas }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconeBolhaTemaA(Icons.Outlined.WarningAmber, problemas.isEmpty())
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (problemas.isEmpty()) "Minutagens verificadas" else "${problemas.size} inconsistências encontradas",
                            color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (problemas.isEmpty()) "Nenhum intervalo inválido" else "Toque para ${if (verProblemas) "recolher" else "ver os episódios"}",
                            color = TemaATokens.TextGray, fontSize = 12.sp
                        )
                    }
                }
                if (verProblemas) problemas.forEach { p ->
                    TemaAGroupDivider()
                    Row(Modifier.fillMaxWidth().clickable { onOpenIssue(p) }.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(p.serieTitulo, color = TemaATokens.TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("${p.temporadaNumero} · E${p.episodioNumero.toString().padStart(2, '0')} · ${nomeSecaoTemaA(p.tipo)}", color = accent, fontSize = 12.sp)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TemaATokens.TextMuted)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OverlineTemaA("Backup local")
                TemaAGroupCard {
                    LinhaAjusteTemaA(Icons.Outlined.Upload, "Exportar backup", "Cópia completa dos dados", onClick = onExportBackup)
                    TemaAGroupDivider()
                    LinhaAjusteTemaA(Icons.Outlined.Download, "Restaurar backup", "Escolher um arquivo salvo") { confirmarRestaurar = true }
                }
            }
        }

        TemaABottomSheet(visible = confirmarRestaurar, onDismiss = { confirmarRestaurar = false }) {
            TemaASheetHeader("Dados e backup", "Restaurar backup?", onClose = { confirmarRestaurar = false })
            Text("Os dados atuais serão substituídos pelo conteúdo do arquivo escolhido.", color = TemaATokens.TextGray, fontSize = 13.sp, lineHeight = 19.sp)
            BarraAcaoTemaA("Escolher arquivo", onCancelar = { confirmarRestaurar = false }, onConfirmar = {
                confirmarRestaurar = false
                onImportBackup()
            })
        }
    }
}

@Composable
private fun NumeroTemaA(valor: String, rotulo: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, color = TemaATokens.TextWhite, fontFamily = TemaATokens.FontMono, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Text(rotulo, color = TemaATokens.TextGray, fontSize = 11.sp)
    }
}

@Composable
private fun IconeBolhaTemaA(icon: ImageVector, ok: Boolean) {
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(if (ok) TemaATokens.SuccessBg else TemaATokens.WarningBg),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = if (ok) TemaATokens.SuccessText else TemaATokens.SemanticWarning, modifier = Modifier.size(20.dp)) }
}

// ---------- Lixeira ----------

@Composable
fun LixeiraTemaAScreen(
    seriesLixeira: List<SerieItem>,
    permanentlyDeletedIds: Set<String>,
    onBackClick: () -> Unit,
    onRestaurarSerie: (SerieItem) -> Unit,
    onExcluirDefinitivamente: (SerieItem) -> Unit,
    onPermitirImportacao: (String) -> Unit
) {
    var excluir by remember { mutableStateOf<SerieItem?>(null) }
    var esvaziar by remember { mutableStateOf(false) }
    var ultimaExcluir by remember { mutableStateOf<SerieItem?>(null) }
    if (excluir != null) ultimaExcluir = excluir
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")) }

    Box(Modifier.fillMaxSize()) {
        SubtelaTemaA("Lixeira", onBackClick, acaoTopo = if (seriesLixeira.isNotEmpty()) ({
            Text("Esvaziar", color = TemaATokens.Perigo, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(CircleShape).background(TemaASheetColors.MenuDangerBg).clickable { esvaziar = true }.padding(horizontal = 14.dp, vertical = 9.dp))
        }) else null) {
            if (seriesLixeira.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.size(64.dp).clip(CircleShape).background(TemaATokens.DarkCardSurface), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Delete, null, tint = TemaATokens.TextGray, modifier = Modifier.size(28.dp))
                    }
                    Text("Sua lixeira está vazia", color = TemaATokens.TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Séries movidas para a lixeira aparecem aqui e podem ser restauradas.",
                        color = TemaATokens.TextGray, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                OverlineTemaA("Séries na lixeira · ${seriesLixeira.size}")
                seriesLixeira.forEach { s ->
                    Row(
                        Modifier.fillMaxWidth().clip(TemaATokens.ShapeCardCommon).background(TemaATokens.DarkGraphiteSurface).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.width(44.dp).height(62.dp).clip(RoundedCornerShape(8.dp)).background(TemaATokens.DarkCardSurface)) {
                            val p = s.posterUrl.ifBlank { s.imageUrl }
                            if (p.isNotBlank()) AsyncImage(p, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(s.titulo, color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                s.deletedAt?.takeIf { it > 0 }?.let { "Removida em ${sdf.format(Date(it))}" } ?: "Na lixeira",
                                color = TemaATokens.TextGray, fontSize = 12.sp,
                                maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Text("Restaurar", color = TemaATokens.TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.clip(CircleShape).border(1.dp, TemaATokens.DarkBorder, CircleShape).clickable { onRestaurarSerie(s) }.padding(horizontal = 12.dp, vertical = 8.dp))
                        Spacer(Modifier.width(6.dp))
                        Box(
                            Modifier.size(36.dp).clip(CircleShape).background(TemaASheetColors.MenuDangerBg).clickable { excluir = s },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Outlined.Delete, "Excluir de vez", tint = TemaATokens.Perigo, modifier = Modifier.size(18.dp)) }
                    }
                }
            }

            if (permanentlyDeletedIds.isNotEmpty()) {
                OverlineTemaA("Bloqueadas para importação")
                Text(
                    "Excluídas de vez do app. A aba na planilha continua lá, mas o app não recria a série. Permita para importar de novo.",
                    color = TemaATokens.TextGray, fontSize = 12.sp, lineHeight = 18.sp
                )
                TemaAGroupCard {
                    permanentlyDeletedIds.forEachIndexed { i, id ->
                        if (i > 0) TemaAGroupDivider()
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("TMDB $id", color = TemaATokens.TextWhite, fontFamily = TemaATokens.FontMono, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Text("Permitir importação", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.clip(CircleShape).clickable { onPermitirImportacao(id) }.padding(horizontal = 8.dp, vertical = 6.dp))
                        }
                    }
                }
            }
        }

        TemaABottomSheet(visible = excluir != null, onDismiss = { excluir = null }) {
            val s = ultimaExcluir
            TemaASheetHeader(s?.titulo.orEmpty(), "Excluir de vez?", onClose = { excluir = null })
            Text(
                "A série será apagada deste aparelho e não poderá ser restaurada. Se ela estiver no Google Sheets, a aba continua lá, mas não será importada de novo.",
                color = TemaATokens.TextGray, fontSize = 13.sp, lineHeight = 19.sp
            )
            BarraAcaoTemaA("Excluir de vez", onCancelar = { excluir = null }, onConfirmar = {
                excluir = null
                s?.let(onExcluirDefinitivamente)
            })
        }
        TemaABottomSheet(visible = esvaziar, onDismiss = { esvaziar = false }) {
            TemaASheetHeader("Lixeira", "Esvaziar lixeira?", onClose = { esvaziar = false })
            Text(
                "${seriesLixeira.size} ${if (seriesLixeira.size == 1) "série será apagada" else "séries serão apagadas"} de vez. Essa ação não pode ser desfeita.",
                color = TemaATokens.TextGray, fontSize = 13.sp, lineHeight = 19.sp
            )
            BarraAcaoTemaA("Esvaziar", onCancelar = { esvaziar = false }, onConfirmar = {
                esvaziar = false
                seriesLixeira.toList().forEach(onExcluirDefinitivamente)
            })
        }
    }
}
