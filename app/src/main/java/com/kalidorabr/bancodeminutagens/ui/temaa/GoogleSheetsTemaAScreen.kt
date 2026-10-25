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
    onBackClick: () -> Unit = {}
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

    var showEditar by remember { mutableStateOf(!initialConfig.isConnectionValidated) }
    val accent = MaterialTheme.colorScheme.primary
    val config = remember(isSyncing, lastSyncStatus, isConnectionValidated, isTestingConnection) { prefs.getConfig() }
    var autoSync by remember(config) { mutableStateOf(config.isAutoSyncEnabled) }

    Box(modifier.fillMaxSize()) {
        SubtelaTemaA("Google Sheets", onBackClick, acaoTopo = {
            CircleIconButtonTemaA(onClick = { testErrorMessage = null; showEditar = true }) {
                Icon(Icons.Outlined.Edit, "Editar conexão", tint = TemaATokens.TextWhite, modifier = Modifier.size(18.dp))
            }
        }) {
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
                            if (isConnectionValidated) "Planilha $masked · Script OK" else "Toque no lápis para configurar",
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
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = accent, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Sincronizando com a planilha…", color = TemaATokens.TextGray, fontSize = 13.sp)
                }
            }
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

        TemaABottomSheet(visible = showEditar, onDismiss = { if (isConnectionValidated) showEditar = false else onBackClick() }) {
            TemaASheetHeader("Google Sheets", if (isConnectionValidated) "Editar conexão" else "Conectar planilha", onClose = {
                testErrorMessage = null
                if (isConnectionValidated) showEditar = false else onBackClick()
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
