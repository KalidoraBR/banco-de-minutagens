package com.kalidorabr.bancodeminutagens.ui.temaa

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalidorabr.bancodeminutagens.data.GoogleSheetsExportService
import com.kalidorabr.bancodeminutagens.data.GoogleSheetsImportService
import com.kalidorabr.bancodeminutagens.data.GoogleSheetsPreferences
import com.kalidorabr.bancodeminutagens.isNetworkAvailable
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.ui.settings.SyncFeedbackMessage
import com.kalidorabr.bancodeminutagens.ui.temaa.components.AvisoTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaABottomSheet
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAGroup
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAGroupDivider
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAPrimaryButton
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASecondaryButton
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASheetColors
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASheetHeader
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASwitch
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TipoAvisoTemaA
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Google Sheets no Tema A. A lógica de sincronização é a mesma da tela original
 * (ui/settings/GoogleSheetsScreen.kt); só a aparência muda.
 */
@Composable
fun GoogleSheetsTemaAScreen(
    modifier: Modifier = Modifier,
    seriesLocaisAtuais: List<SerieItem> = emptyList(),
    onImportSuccess: (List<SerieItem>) -> Unit = {},
    onBackClick: () -> Unit = {},
    chaveIntroDb: String = "",
    onSalvarChaveIntroDb: (String) -> Unit = {},
    autoIntroDb: Boolean = true,
    onAutoIntroDbChange: (Boolean) -> Unit = {},
    onConferirIntroDbAgora: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember(context) { GoogleSheetsPreferences(context) }
    val initialConfig = remember { prefs.getConfig() }

    var spreadsheetId by remember { mutableStateOf(initialConfig.spreadsheetId) }
    var scriptUrl by remember { mutableStateOf(initialConfig.scriptUrl) }
    var lastSyncDate by remember { mutableStateOf(initialConfig.lastSyncDate) }
    var lastSyncStatus by remember { mutableStateOf(initialConfig.lastSyncStatus) }

    var isConnectionValidated by remember { mutableStateOf(initialConfig.isConnectionValidated) }
    var isEditingCredentials by remember { mutableStateOf(!initialConfig.isConnectionValidated) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testErrorMessage by remember { mutableStateOf<String?>(null) }

    var isSyncing by remember { mutableStateOf(false) }
    var activeFeedback by remember { mutableStateOf<SyncFeedbackMessage?>(null) }

    LaunchedEffect(activeFeedback) {
        if (activeFeedback != null) {
            delay(4000)
            activeFeedback = null
        }
    }

    val coroutineScope = rememberCoroutineScope()

    // Teste e salvamento real das credenciais
    val testarESalvarConexao = {
        if (spreadsheetId.isBlank() || scriptUrl.isBlank()) {
            testErrorMessage = "Preencha o ID da planilha e a URL do Apps Script."
        } else if (!isNetworkAvailable(context)) {
            testErrorMessage = "Não foi possível verificar a conexão no momento. Verifique sua internet."
        } else {
            testErrorMessage = null
            isTestingConnection = true

            coroutineScope.launch {
                val resultado = GoogleSheetsImportService.executarImportacao(
                    scriptUrl = scriptUrl,
                    spreadsheetId = spreadsheetId,
                    seriesLocaisAtuais = seriesLocaisAtuais,
                    permanentlyDeletedIds = prefs.getPermanentlyDeletedTmdbIds()
                )

                isTestingConnection = false

                if (resultado.success) {
                    prefs.saveConfig(spreadsheetId, scriptUrl)
                    prefs.saveConnectionValidated(true)
                    prefs.unlockAndEnableAutoSync()
                    prefs.setPendingExport(false)

                    val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
                    val now = dateFormat.format(Date())
                    lastSyncDate = now
                    lastSyncStatus = "Sucesso"
                    prefs.saveLastSync(now, "Sucesso")

                    isConnectionValidated = true
                    isEditingCredentials = false
                    testErrorMessage = null

                    onImportSuccess(resultado.novasSeries)

                    val hasWarnings = resultado.warnings.isNotEmpty()
                    val title = if (hasWarnings) "Sincronização concluída com avisos" else "Sincronização concluída"
                    val subtitle = "${resultado.seriesAtualizadasCount} série(s) e ${resultado.episodiosAtualizadosCount} episódio(s) atualizados."
                    activeFeedback = SyncFeedbackMessage(isError = false, isWarning = hasWarnings, title = title, subtitle = subtitle)
                } else {
                    testErrorMessage = "Falha na conexão: ${resultado.errorMessage ?: "Não foi possível se conectar ao Apps Script."}"
                }
            }
        }
    }

    val executarImportacaoSheets = {
        if (spreadsheetId.isBlank() || scriptUrl.isBlank()) {
            activeFeedback = SyncFeedbackMessage(
                isError = true,
                title = "Não foi possível sincronizar",
                subtitle = "Preencha o ID da planilha e a URL primeiro."
            )
        } else {
            activeFeedback = null
            isSyncing = true

            coroutineScope.launch {
                if (prefs.getConfig().hasPendingExport && isNetworkAvailable(context)) {
                    try {
                        GoogleSheetsExportService.executarExportacao(
                            scriptUrl = scriptUrl,
                            spreadsheetId = spreadsheetId,
                            seriesLocais = seriesLocaisAtuais
                        )
                    } catch (_: Exception) {}
                }

                val resultado = GoogleSheetsImportService.executarImportacao(
                    scriptUrl = scriptUrl,
                    spreadsheetId = spreadsheetId,
                    seriesLocaisAtuais = seriesLocaisAtuais,
                    permanentlyDeletedIds = prefs.getPermanentlyDeletedTmdbIds()
                )

                isSyncing = false

                if (resultado.success) {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
                    val now = dateFormat.format(Date())
                    lastSyncDate = now
                    lastSyncStatus = "Sucesso"
                    prefs.saveLastSync(now, "Sucesso")
                    prefs.unlockAndEnableAutoSync()
                    prefs.setPendingExport(false)

                    onImportSuccess(resultado.novasSeries)

                    val hasWarnings = resultado.warnings.isNotEmpty()
                    val title = if (hasWarnings) "Sincronização concluída com avisos" else "Sincronização concluída"
                    val subtitle = "${resultado.seriesAtualizadasCount} série(s) e ${resultado.episodiosAtualizadosCount} episódio(s) atualizados."
                    activeFeedback = SyncFeedbackMessage(isError = false, isWarning = hasWarnings, title = title, subtitle = subtitle)
                } else {
                    activeFeedback = SyncFeedbackMessage(
                        isError = true,
                        title = "Não foi possível sincronizar",
                        subtitle = resultado.errorMessage
                            ?: "Não foi possível confirmar a importação."
                    )
                }
            }
        }
    }

    val executarExportacaoSheets = {
        if (spreadsheetId.isBlank() || scriptUrl.isBlank()) {
            activeFeedback = SyncFeedbackMessage(
                isError = true,
                title = "Não foi possível sincronizar",
                subtitle = "Preencha o ID da planilha e a URL primeiro."
            )
        } else if (seriesLocaisAtuais.isEmpty()) {
            activeFeedback = SyncFeedbackMessage(
                isError = true,
                title = "Não foi possível sincronizar",
                subtitle = "Nenhuma série na coleção para exportar."
            )
        } else {
            activeFeedback = null
            isSyncing = true

            coroutineScope.launch {
                val resultado = GoogleSheetsExportService.executarExportacao(
                    scriptUrl = scriptUrl,
                    spreadsheetId = spreadsheetId,
                    seriesLocais = seriesLocaisAtuais
                )

                isSyncing = false

                if (resultado.success) {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
                    val now = dateFormat.format(Date())
                    lastSyncDate = now
                    lastSyncStatus = "Sucesso"
                    prefs.saveLastSync(now, "Sucesso")
                    prefs.unlockAndEnableAutoSync()
                    prefs.setPendingExport(false)

                    val hasWarnings = resultado.warnings.isNotEmpty()
                    val title = if (hasWarnings) "Sincronização concluída com avisos" else "Sincronização concluída"
                    val subtitle = "${resultado.seriesExportadasCount} série(s) e ${resultado.episodiosExportadosCount} episódio(s) atualizados."
                    activeFeedback = SyncFeedbackMessage(isError = false, isWarning = hasWarnings, title = title, subtitle = subtitle)
                } else {
                    activeFeedback = SyncFeedbackMessage(
                        isError = true,
                        title = "Não foi possível sincronizar",
                        subtitle = resultado.errorMessage
                            ?: "Os dados foram enviados, mas a planilha não confirmou a exportação."
                    )
                }
            }
        }
    }

    val executarSincronizacaoCompletaSheets = {
        if (spreadsheetId.isBlank() || scriptUrl.isBlank()) {
            activeFeedback = SyncFeedbackMessage(
                isError = true,
                title = "Não foi possível sincronizar",
                subtitle = "Preencha o ID da planilha e a URL primeiro."
            )
        } else {
            activeFeedback = null
            isSyncing = true

            coroutineScope.launch {
                val resImport = GoogleSheetsImportService.executarImportacao(
                    scriptUrl = scriptUrl,
                    spreadsheetId = spreadsheetId,
                    seriesLocaisAtuais = seriesLocaisAtuais,
                    permanentlyDeletedIds = prefs.getPermanentlyDeletedTmdbIds()
                )

                if (!resImport.success) {
                    isSyncing = false
                    activeFeedback = SyncFeedbackMessage(
                        isError = true,
                        title = "Não foi possível sincronizar",
                        subtitle = resImport.errorMessage
                            ?: "Não foi possível confirmar a importação."
                    )
                    return@launch
                }

                onImportSuccess(resImport.novasSeries)

                val resExport = GoogleSheetsExportService.executarExportacao(
                    scriptUrl = scriptUrl,
                    spreadsheetId = spreadsheetId,
                    seriesLocais = resImport.novasSeries
                )

                isSyncing = false

                if (resExport.success) {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
                    val now = dateFormat.format(Date())
                    lastSyncDate = now
                    lastSyncStatus = "Sucesso"
                    prefs.saveLastSync(now, "Sucesso")
                    prefs.unlockAndEnableAutoSync()
                    prefs.setPendingExport(false)

                    val warnings = (resImport.warnings + resExport.warnings).distinct()
                    val hasWarnings = warnings.isNotEmpty()
                    val title = if (hasWarnings) "Sincronização concluída com avisos" else "Sincronização concluída"
                    val subtitle = "${resImport.seriesAtualizadasCount} série(s) e ${resImport.episodiosAtualizadosCount} episódio(s) atualizados."
                    activeFeedback = SyncFeedbackMessage(isError = false, isWarning = hasWarnings, title = title, subtitle = subtitle)
                } else {
                    activeFeedback = SyncFeedbackMessage(
                        isError = true,
                        title = "Não foi possível sincronizar",
                        subtitle = resExport.errorMessage
                            ?: "Os dados foram enviados, mas a planilha não confirmou a exportação."
                    )
                }
            }
        }
    }

    // o quadro de conexão só abre ao tocar em "Editar conexão" (a tela também serve só para o IntroDB)
    var showEditar by remember { mutableStateOf(false) }
    var showChaveIntroDb by remember { mutableStateOf(false) }
    val accent = MaterialTheme.colorScheme.primary
    val config = remember(isSyncing, lastSyncStatus, isConnectionValidated, isTestingConnection) { prefs.getConfig() }
    var autoSync by remember(config) { mutableStateOf(config.isAutoSyncEnabled) }

    Box(modifier.fillMaxSize()) {
        SubtelaTemaA("Sincronização", onBackClick) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OverlineTemaA("Google Sheets", Modifier.weight(1f))
                Row(
                    Modifier.clip(RoundedCornerShape(10.dp)).clickable { testErrorMessage = null; showEditar = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Edit, null, tint = TemaATokens.TextGray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Editar conexão", color = TemaATokens.TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            // Estado da conexão
            Column(
                Modifier.fillMaxWidth().clip(TemaATokens.ShapeCardCommon).background(TemaATokens.DarkGraphiteSurface).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val masked = if (spreadsheetId.length > 4) "••••" + spreadsheetId.takeLast(4) else spreadsheetId
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (isConnectionValidated) TemaATokens.SuccessBg else TemaATokens.WarningBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isConnectionValidated) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline, null,
                            tint = if (isConnectionValidated) TemaATokens.SuccessText else TemaATokens.SemanticWarning,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(if (isConnectionValidated) "Conectado" else "Não conectado", color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            if (isConnectionValidated) "Planilha $masked · Script OK" else "Toque em Editar conexão para configurar",
                            color = TemaATokens.TextGray, fontSize = 12.sp
                        )
                    }
                }
                if (isConnectionValidated) {
                    val falha = config.hasPendingExport && !isSyncing
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(if (falha) TemaATokens.WarningBg else TemaASheetColors.Group).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            when {
                                isSyncing -> "Sincronizando…"
                                config.hasPendingExport && !isNetworkAvailable(context) -> "Alterações aguardando internet"
                                config.hasPendingExport -> "Falha na última tentativa"
                                else -> "Tudo sincronizado"
                            },
                            color = if (falha) TemaATokens.SemanticWarning else TemaATokens.TextWhite,
                            fontSize = 14.sp, fontWeight = FontWeight.Bold
                        )
                        Text("Último sucesso: ${lastSyncDate ?: "nenhum ainda"}", color = TemaATokens.TextGray, fontSize = 12.sp)
                    }
                    if (falha) {
                        TemaAPrimaryButton("Tentar novamente", onClick = { executarExportacaoSheets() }, height = 46.dp)
                    }
                }
            }

            // Sincronização automática
            Row(
                Modifier.fillMaxWidth().clip(TemaATokens.ShapeCardCommon).background(TemaATokens.DarkGraphiteSurface).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Sincronização automática", color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (config.isAutoSyncUnlocked) {
                            if (config.hasPendingExport) "Envia ao salvar · alterações pendentes" else "Envia ao salvar"
                        } else "Sincronize uma vez para liberar",
                        color = if (config.hasPendingExport) TemaATokens.SemanticWarning else TemaATokens.TextGray,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Box(Modifier.then(if (config.isAutoSyncUnlocked) Modifier else Modifier.alpha(0.4f))) {
                    TemaASwitch(autoSync && config.isAutoSyncUnlocked) { on ->
                        if (config.isAutoSyncUnlocked) {
                            autoSync = on
                            prefs.setAutoSyncEnabled(on)
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OverlineTemaA("Ações")
                TemaAGroup(Modifier.clip(TemaATokens.ShapeCardCommon)) {
                    AcaoSheetsTemaA(Icons.Outlined.CloudDownload, "Importar da planilha", !isSyncing) { executarImportacaoSheets() }
                    TemaAGroupDivider()
                    AcaoSheetsTemaA(Icons.Outlined.CloudUpload, "Exportar para a planilha", !isSyncing) { executarExportacaoSheets() }
                    TemaAGroupDivider()
                    AcaoSheetsTemaA(Icons.Default.Sync, "Sincronizar agora", !isSyncing) { executarSincronizacaoCompletaSheets() }
                }
            }
            if (isSyncing) {
                // Google Sheets responde só no fim: a barra corre sem porcentagem
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    androidx.compose.material3.LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = accent, trackColor = TemaATokens.Track
                    )
                    Text("Sincronizando com a planilha…", color = TemaATokens.TextGray, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(8.dp))
            BlocoIntroDbTemaA(
                chave = chaveIntroDb,
                autoEnvio = autoIntroDb,
                onAutoEnvioChange = onAutoIntroDbChange,
                onEditarChave = { showChaveIntroDb = true },
                onConferirAgora = onConferirIntroDbAgora
            )
        }

        TemaABottomSheet(visible = showChaveIntroDb, onDismiss = { showChaveIntroDb = false }) {
            var chave by remember(showChaveIntroDb) { mutableStateOf(chaveIntroDb) }
            TemaASheetHeader("Sincronização", "Chave do IntroDB", onClose = { showChaveIntroDb = false })
            Text(
                "Crie uma chave no site introdb.app, em Conta, e cole aqui. Ela fica guardada só neste aparelho e serve para enviar minutagens. Conferir o que já está no site não precisa de chave.",
                color = TemaATokens.TextGray, fontSize = 13.sp, lineHeight = 19.sp
            )
            CampoTemaA("Chave da API", chave, { chave = it.trim() }, placeholder = "idb_…", secreto = true)
            TemaAPrimaryButton("Salvar", onClick = {
                onSalvarChaveIntroDb(chave)
                showChaveIntroDb = false
            })
        }

        activeFeedback?.let { fb ->
            AvisoTemaA(
                titulo = fb.title,
                subtitulo = fb.subtitle,
                tipo = when {
                    fb.isError -> TipoAvisoTemaA.ERRO
                    fb.isWarning -> TipoAvisoTemaA.ATENCAO
                    else -> TipoAvisoTemaA.SUCESSO
                },
                acao = if (fb.isError) "OK" else null,
                onAcao = { activeFeedback = null },
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 96.dp)
            )
        }

        // fechar sem conectar só fecha o quadro: a tela continua (o bloco do IntroDB não depende da planilha)
        TemaABottomSheet(visible = showEditar, onDismiss = { showEditar = false }) {
            TemaASheetHeader("Google Sheets", if (isConnectionValidated) "Editar conexão" else "Conectar planilha", onClose = {
                testErrorMessage = null
                showEditar = false
            })
            CampoTemaA("ID da planilha", spreadsheetId, { spreadsheetId = it.trim(); testErrorMessage = null }, placeholder = "1AbC…")
            CampoTemaA("URL do Apps Script", scriptUrl, { scriptUrl = it.trim(); testErrorMessage = null }, placeholder = "https://script.google.com/…/exec")
            when {
                isTestingConnection -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = accent, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Testando conexão…", color = TemaATokens.TextGray, fontSize = 13.sp)
                }
                testErrorMessage != null -> Text(
                    testErrorMessage!!, color = TemaATokens.ErroTextoForte, fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(TemaATokens.ErroFundo).padding(12.dp)
                )
                isConnectionValidated -> Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(TemaATokens.SuccessBg).padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Credenciais verificadas", color = TemaATokens.SuccessText, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("Testar de novo", color = TemaATokens.SuccessText, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clip(RoundedCornerShape(10.dp)).border(1.dp, Color(0xFF2E6B48), RoundedCornerShape(10.dp))
                            .clickable { testarESalvarConexao() }.padding(horizontal = 12.dp, vertical = 7.dp))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TemaASecondaryButton("Só salvar", onClick = {
                    prefs.saveConfig(spreadsheetId, scriptUrl)
                    if (isConnectionValidated) showEditar = false
                }, modifier = Modifier.weight(1f), enabled = !isTestingConnection && spreadsheetId.isNotBlank() && scriptUrl.isNotBlank())
                TemaAPrimaryButton("Salvar e sincronizar", onClick = { testarESalvarConexao() }, modifier = Modifier.weight(1.6f), enabled = !isTestingConnection)
            }
        }
    }

    // Fecha o pop-up de edição quando o teste der certo
    LaunchedEffect(isConnectionValidated, isEditingCredentials) {
        if (isConnectionValidated && !isEditingCredentials) showEditar = false
    }
}

@Composable
private fun AcaoSheetsTemaA(icon: ImageVector, titulo: String, enabled: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (enabled) TemaATokens.TextSoft else TemaATokens.TextMuted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(titulo, color = if (enabled) TemaATokens.TextWhite else TemaATokens.TextMuted, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TemaATokens.TextMuted)
    }
}

/** Bloco do IntroDB na tela Sincronização: conferência de todas as séries (com porcentagem), envio e chave. */
@Composable
private fun BlocoIntroDbTemaA(
    chave: String,
    autoEnvio: Boolean,
    onAutoEnvioChange: (Boolean) -> Unit,
    onEditarChave: () -> Unit,
    onConferirAgora: () -> Unit
) {
    val context = LocalContext.current
    val accent = MaterialTheme.colorScheme.primary
    val introPrefs = remember(context) { com.kalidorabr.bancodeminutagens.data.IntroDbPreferences(context) }
    val progresso by com.kalidorabr.bancodeminutagens.data.ConferenciaIntroDb.estado.collectAsState()
    var autoConferencia by remember { mutableStateOf(introPrefs.conferenciaAutomatica()) }
    // relê a hora da última conferência quando uma termina
    val ultima = remember(progresso.rodando) { com.kalidorabr.bancodeminutagens.data.ConferenciaIntroDb.ultima(context) }
    val fracao by androidx.compose.animation.core.animateFloatAsState(progresso.porcentagem / 100f, label = "conferenciaIntroDb")

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OverlineTemaA("IntroDB")
        Column(
            Modifier.fillMaxWidth().clip(TemaATokens.ShapeCardCommon).background(TemaATokens.DarkGraphiteSurface).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    when {
                        progresso.rodando -> "Conferindo no IntroDB… ${progresso.porcentagem}%"
                        progresso.pausaDoSite -> "O site pediu uma pausa"
                        else -> "Conferência do IntroDB"
                    },
                    color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    when {
                        progresso.rodando && progresso.seriesTotal > 0 ->
                            "Série ${progresso.serieIndice} de ${progresso.seriesTotal} · ${progresso.serieAtual}"
                        progresso.rodando -> "Preparando…"
                        progresso.pausaDoSite -> "Continua sozinha daqui a pouco"
                        progresso.nadaPendente && ultima > 0 -> "Tudo em dia · última: ${tempoAtras(ultima)}"
                        ultima > 0 -> "Última: ${tempoAtras(ultima)}"
                        else -> "Ainda não conferido"
                    },
                    color = TemaATokens.TextGray, fontSize = 12.sp, maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            if (progresso.rodando) {
                Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(TemaATokens.Track)) {
                    Box(Modifier.fillMaxWidth(fracao).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(accent))
                }
                Row {
                    Text("${progresso.feitos} de ${progresso.total} episódios", color = TemaATokens.TextGray, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text(
                        if (progresso.comDados == 1) "1 com minutagem" else "${progresso.comDados} com minutagem",
                        color = TemaATokens.SuccessText, fontSize = 12.sp, fontWeight = FontWeight.Bold
                    )
                }
            } else {
                TemaASecondaryButton("Conferir agora", onConferirAgora, Modifier.fillMaxWidth())
            }
        }

        TemaAGroup(Modifier.clip(TemaATokens.ShapeCardCommon)) {
            LinhaChaveTemaA("Conferência automática", "Ao abrir o app e em segundo plano", autoConferencia, onChange = { on ->
                autoConferencia = on
                introPrefs.salvarConferenciaAutomatica(on)
                com.kalidorabr.bancodeminutagens.data.ConferenciaIntroDb.agendar(context)
            })
            TemaAGroupDivider()
            // sem chave não há como enviar: a opção fica apagada (a chave é opcional para todo o resto)
            LinhaChaveTemaA(
                "Enviar ao tocar em Concluir",
                if (chave.isBlank()) "Configure a chave para usar" else "Só com as três seções preenchidas",
                autoEnvio && chave.isNotBlank(),
                onAutoEnvioChange,
                habilitado = chave.isNotBlank()
            )
            TemaAGroupDivider()
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onEditarChave).padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Chave do IntroDB", color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Para enviar minutagens", color = TemaATokens.TextGray, fontSize = 12.sp)
                }
                Text(
                    if (chave.isBlank()) "Configurar" else "Conectado",
                    color = if (chave.isBlank()) TemaATokens.SemanticWarning else TemaATokens.SuccessText,
                    fontSize = 13.sp, fontWeight = FontWeight.Bold
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TemaATokens.TextMuted)
            }
        }
    }
}

@Composable
private fun LinhaChaveTemaA(titulo: String, legenda: String, ligado: Boolean, onChange: (Boolean) -> Unit, habilitado: Boolean = true) {
    Row(
        Modifier.fillMaxWidth().alpha(if (habilitado) 1f else 0.45f)
            .clickable(enabled = habilitado) { onChange(!ligado) }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titulo, color = TemaATokens.TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(legenda, color = TemaATokens.TextGray, fontSize = 12.sp)
        }
        Spacer(Modifier.width(12.dp))
        TemaASwitch(ligado) { if (habilitado) onChange(it) }
    }
}

/** "agora", "há 12 min", "há 3 h", "há 2 dias". */
private fun tempoAtras(quando: Long): String {
    val min = (System.currentTimeMillis() - quando) / 60_000L
    return when {
        min < 1 -> "agora"
        min < 60 -> "há $min min"
        min < 24 * 60 -> "há ${min / 60} h"
        else -> "há ${min / (24 * 60)} dias"
    }
}
