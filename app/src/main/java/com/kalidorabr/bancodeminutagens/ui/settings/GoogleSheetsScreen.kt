package com.kalidorabr.bancodeminutagens.ui.settings

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalidorabr.bancodeminutagens.data.GoogleSheetsExportService
import com.kalidorabr.bancodeminutagens.data.GoogleSheetsImportService
import com.kalidorabr.bancodeminutagens.data.GoogleSheetsPreferences
import com.kalidorabr.bancodeminutagens.isNetworkAvailable
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.ui.theme.FullTokens
import com.kalidorabr.bancodeminutagens.ui.theme.BancoDeMinutagensTheme
import com.kalidorabr.bancodeminutagens.ui.theme.BorderColor
import com.kalidorabr.bancodeminutagens.ui.theme.DarkBackground
import com.kalidorabr.bancodeminutagens.ui.theme.RedPrimary
import com.kalidorabr.bancodeminutagens.ui.theme.StatusGreen
import com.kalidorabr.bancodeminutagens.ui.theme.SurfaceCard
import com.kalidorabr.bancodeminutagens.ui.theme.SurfaceDark
import com.kalidorabr.bancodeminutagens.ui.theme.TextPrimary
import com.kalidorabr.bancodeminutagens.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SyncFeedbackMessage(
    val isError: Boolean,
    val isWarning: Boolean = false,
    val title: String,
    val subtitle: String
)

@Composable
fun GoogleSheetsScreen(
    modifier: Modifier = Modifier,
    isFullTheme: Boolean = false,
    backgroundColor: Color = if (isFullTheme) FullTokens.DarkBackground else DarkBackground,
    seriesLocaisAtuais: List<SerieItem> = emptyList(),
    onImportSuccess: (List<SerieItem>) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val glassBackground = if (isFullTheme) {
        Brush.verticalGradient(listOf(Color(0xEE16161C), Color(0xFF111114)))
    } else {
        Brush.linearGradient(listOf(SurfaceDark, SurfaceDark))
    }
    val glassBorder = if (isFullTheme) {
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.02f)))
    } else {
        Brush.linearGradient(listOf(BorderColor, BorderColor))
    }
    val cardShape = if (isFullTheme) RoundedCornerShape(16.dp) else RoundedCornerShape(12.dp)
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Google Sheets",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Parte Superior: Estado da Conexão e Credenciais
            item {
                val currentConfig = remember(prefs, isSyncing, lastSyncStatus, isConnectionValidated) { prefs.getConfig() }

                if (!isConnectionValidated || isEditingCredentials) {
                    // Estado 1: Credenciais ainda não validadas ou em edição
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(glassBackground, RoundedCornerShape(16.dp))
                            .border(1.dp, glassBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Configurar Credenciais",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (isConnectionValidated) {
                                Text(
                                    text = "Cancelar",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable {
                                        isEditingCredentials = false
                                        testErrorMessage = null
                                    }
                                )
                            }
                        }

                        // Campo ID da Planilha
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "ID da planilha",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            OutlinedTextField(
                                value = spreadsheetId,
                                onValueChange = {
                                    spreadsheetId = it
                                    testErrorMessage = null
                                },
                                placeholder = {
                                    Text(
                                        "Ex: 1BxiMVs0XRA5nFMdKbBUI6DMB5cWN2L...",
                                        color = TextSecondary.copy(alpha = 0.6f),
                                        fontSize = 13.sp
                                    )
                                },
                                singleLine = true,
                                enabled = !isTestingConnection,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceDark,
                                    unfocusedContainerColor = SurfaceDark,
                                    disabledContainerColor = SurfaceDark.copy(alpha = 0.5f),
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = BorderColor,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Campo URL do Apps Script
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "URL do Apps Script",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            OutlinedTextField(
                                value = scriptUrl,
                                onValueChange = {
                                    scriptUrl = it
                                    testErrorMessage = null
                                },
                                placeholder = {
                                    Text(
                                        "Ex: https://script.google.com/macros/s/...",
                                        color = TextSecondary.copy(alpha = 0.6f),
                                        fontSize = 13.sp
                                    )
                                },
                                singleLine = true,
                                enabled = !isTestingConnection,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceDark,
                                    unfocusedContainerColor = SurfaceDark,
                                    disabledContainerColor = SurfaceDark.copy(alpha = 0.5f),
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = BorderColor,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (testErrorMessage != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ErrorOutline,
                                    contentDescription = null,
                                    tint = RedPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = testErrorMessage!!,
                                    color = RedPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Botão "Salvar e testar conexão"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(
                                    if (isTestingConnection) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(12.dp)
                                )
                                .then(
                                    if (!isTestingConnection) Modifier.clickable { testarESalvarConexao() } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isTestingConnection) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = if (isTestingConnection) "Testando conexão..." else "Salvar e testar conexão",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    // Estado 2: Conexão validada
                    val isOnline = isNetworkAvailable(context)
                    val statusSyncText = when {
                        isSyncing -> "Sincronizando..."
                        currentConfig.hasPendingExport && !isOnline -> "Alterações aguardando internet"
                        currentConfig.hasPendingExport -> "Falha na última tentativa"
                        else -> "Tudo sincronizado"
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(glassBackground, RoundedCornerShape(16.dp))
                            .border(1.dp, glassBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Ícone de lápis no canto superior direito para editar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(SurfaceDark, CircleShape)
                                    .border(1.dp, BorderColor, CircleShape)
                                    .clickable {
                                        isEditingCredentials = true
                                        testErrorMessage = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Editar credenciais",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Título centralizado (SEM ponto verde acima do título)
                        Text(
                            text = "Google Sheets conectado",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Subtítulo centralizado
                        Text(
                            text = "Credenciais verificadas com sucesso.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Planilha com ID mascarado
                        val maskedId = if (spreadsheetId.length > 4) "••••••••" + spreadsheetId.takeLast(4) else spreadsheetId
                        Text(
                            text = "Planilha $maskedId",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Apps Script — URL configurada
                        Text(
                            text = "Apps Script — URL configurada",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Linha divisória
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(BorderColor)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Ícone verde de confirmação
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = StatusGreen,
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Texto de status de sincronização centralizado
                        Text(
                            text = statusSyncText,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Texto secundário de última sincronização
                        Text(
                            text = "Última sincronização: ${lastSyncDate ?: "agora"}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Botão "Tentar novamente" se houver alterações pendentes
                        if (currentConfig.hasPendingExport && !isSyncing) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .background(SurfaceDark, RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(10.dp))
                                    .clickable(onClick = { executarExportacaoSheets() }),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tentar novamente",
                                        color = Color(0xFFFFB300),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card de Sincronização Automática
            item {
                val currentConfig = remember(prefs, isSyncing, lastSyncStatus) { prefs.getConfig() }
                var autoSyncChecked by remember(currentConfig) { mutableStateOf(currentConfig.isAutoSyncEnabled) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(glassBackground, RoundedCornerShape(16.dp))
                        .border(1.dp, glassBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sincronização automática",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (currentConfig.hasPendingExport) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = Color(0xFFFFB300).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFFFB300),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Alterações pendentes",
                                        color = Color(0xFFFFB300),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (currentConfig.isAutoSyncUnlocked) {
                                    "Envia alterações para a planilha automaticamente após salvar uma minutagem."
                                } else {
                                    "Realize uma sincronização manual com sucesso para ativar o modo automático."
                                },
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Switch(
                            checked = autoSyncChecked && currentConfig.isAutoSyncUnlocked,
                            onCheckedChange = { isChecked ->
                                if (currentConfig.isAutoSyncUnlocked) {
                                    autoSyncChecked = isChecked
                                    prefs.setAutoSyncEnabled(isChecked)
                                }
                            },
                            enabled = currentConfig.isAutoSyncUnlocked,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = SurfaceDark
                            )
                        )
                    }
                }
            }

            // Cards de Ação (Importar, Exportar, Sincronizar)
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Ações de Sincronização",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    ActionCard(
                        title = "Importar da planilha",
                        subtitle = "Carregar minutagens e dados da planilha",
                        icon = Icons.Outlined.CloudDownload,
                        enabled = !isSyncing,
                        isFullTheme = isFullTheme,
                        onClick = { executarImportacaoSheets() }
                    )

                    ActionCard(
                        title = "Exportar para a planilha",
                        subtitle = "Enviar dados locais para a planilha",
                        icon = Icons.Outlined.CloudUpload,
                        enabled = !isSyncing,
                        isFullTheme = isFullTheme,
                        onClick = { executarExportacaoSheets() }
                    )

                    ActionCard(
                        title = "Sincronizar agora",
                        subtitle = "Sincronização bidirecional completa (Importar → Exportar)",
                        icon = Icons.Default.Sync,
                        enabled = !isSyncing,
                        isFullTheme = isFullTheme,
                        onClick = { executarSincronizacaoCompletaSheets() }
                    )
                }
            }

            // Bloco Última Sincronização
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(glassBackground, RoundedCornerShape(16.dp))
                        .border(1.dp, glassBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Última sincronização",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Data e Horário",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = lastSyncDate ?: "Nenhuma sincronização realizada",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (lastSyncStatus == "Sucesso") StatusGreen.copy(alpha = 0.15f)
                                    else BorderColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (lastSyncStatus == "Sucesso") StatusGreen else BorderColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = lastSyncStatus ?: "Pendente",
                                color = if (lastSyncStatus == "Sucesso") StatusGreen else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        }

        activeFeedback?.let { feedback ->
            SyncFeedbackOverlay(
                feedback = feedback,
                onDismiss = { activeFeedback = null },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 72.dp)
            )
        }
    }
}

@Composable
private fun SyncFeedbackOverlay(
    feedback: SyncFeedbackMessage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = when {
        feedback.isError -> RedPrimary
        feedback.isWarning -> Color(0xFFFFB300)
        else -> StatusGreen
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = accentColor.copy(alpha = 0.96f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = accentColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (feedback.isError || feedback.isWarning) {
                    Icons.Outlined.ErrorOutline
                } else {
                    Icons.Outlined.CheckCircle
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feedback.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = feedback.subtitle,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.18f), CircleShape)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fechar mensagem",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    enabled: Boolean = true,
    isFullTheme: Boolean = false,
    onClick: () -> Unit
) {
    val glassBackground = if (isFullTheme) {
        Brush.verticalGradient(listOf(Color(0xEE16161C), Color(0xFF111114)))
    } else {
        Brush.linearGradient(listOf(SurfaceDark, SurfaceDark))
    }
    val glassBorder = if (isFullTheme) {
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.02f)))
    } else {
        Brush.linearGradient(listOf(BorderColor, BorderColor))
    }
    val cardShape = if (isFullTheme) RoundedCornerShape(16.dp) else RoundedCornerShape(12.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(glassBackground, cardShape)
            .border(1.dp, glassBorder, cardShape)
            .clip(cardShape)
            .then(
                if (enabled) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(if (enabled) SurfaceDark else SurfaceDark.copy(alpha = 0.5f), CircleShape)
                .border(1.dp, BorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = if (enabled) TextPrimary else TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(
    name = "Google Sheets Screen Preview",
    showBackground = true,
    backgroundColor = 0xFF050505
)
@Composable
private fun GoogleSheetsScreenPreview() {
    BancoDeMinutagensTheme {
        GoogleSheetsScreen()
    }
}
