package com.kalidorabr.bancodeminutagens

import com.kalidorabr.bancodeminutagens.ui.temac.ambienteC
import com.kalidorabr.bancodeminutagens.ui.temab.faixaAcimaB
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.kalidorabr.bancodeminutagens.ui.temab.topoEscuroB
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalidorabr.bancodeminutagens.data.AppThemePreferences
import com.kalidorabr.bancodeminutagens.data.GoogleSheetsExportService
import com.kalidorabr.bancodeminutagens.data.GoogleSheetsPreferences
import com.kalidorabr.bancodeminutagens.data.IntroJsonExporter
import com.kalidorabr.bancodeminutagens.data.PendingClearEpisode
import com.kalidorabr.bancodeminutagens.data.PendingClearItem
import com.kalidorabr.bancodeminutagens.data.SeriesRepository
import com.kalidorabr.bancodeminutagens.data.TmdbSerieResultado
import com.kalidorabr.bancodeminutagens.data.TmdbService
import com.kalidorabr.bancodeminutagens.model.DestinoEpisodio
import com.kalidorabr.bancodeminutagens.model.EpisodioItem
import com.kalidorabr.bancodeminutagens.model.EstadoMinutagem
import com.kalidorabr.bancodeminutagens.model.MinutagemSecao
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.model.SerieProgressoUtils
import com.kalidorabr.bancodeminutagens.model.StatusEpisodio
import com.kalidorabr.bancodeminutagens.model.TemporadaItem
import com.kalidorabr.bancodeminutagens.model.TipoMinutagem
import com.kalidorabr.bancodeminutagens.data.VisualMode
import com.kalidorabr.bancodeminutagens.ui.temaa.EmBreveHeaderTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.EpisodioDetailTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.FilterChipsRowTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaATokens
import com.kalidorabr.bancodeminutagens.ui.temas.TelaAviso
import com.kalidorabr.bancodeminutagens.ui.temas.TelaBuscar
import com.kalidorabr.bancodeminutagens.ui.temas.TelaContinuar
import com.kalidorabr.bancodeminutagens.ui.temas.TelaDadosBackup
import com.kalidorabr.bancodeminutagens.ui.temas.TelaEmBreveCabecalho
import com.kalidorabr.bancodeminutagens.ui.temas.TelaEmBreveLista
import com.kalidorabr.bancodeminutagens.ui.temas.TelaEpisodio
import com.kalidorabr.bancodeminutagens.ui.temas.TelaEstadoVazio
import com.kalidorabr.bancodeminutagens.ui.temas.TelaFiltros
import com.kalidorabr.bancodeminutagens.ui.temas.TelaGoogleSheets
import com.kalidorabr.bancodeminutagens.ui.temas.TelaBarraAbas
import com.kalidorabr.bancodeminutagens.ui.temas.TelaInicioCabecalho
import com.kalidorabr.bancodeminutagens.ui.temas.TelaLixeira
import com.kalidorabr.bancodeminutagens.ui.temas.TelaSerie
import com.kalidorabr.bancodeminutagens.ui.temas.TelaLinhaSerie
import com.kalidorabr.bancodeminutagens.ui.temas.TelaAjustes
import com.kalidorabr.bancodeminutagens.ui.temas.TelaSobre
import com.kalidorabr.bancodeminutagens.ui.temas.TelaTemporada
import com.kalidorabr.bancodeminutagens.ui.temas.LocalModoVisual
import com.kalidorabr.bancodeminutagens.ui.temas.Paletas
import com.kalidorabr.bancodeminutagens.ui.temas.ProvedorTemaNovo
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaATypographyProvider
import com.kalidorabr.bancodeminutagens.ui.temaa.HomeHeaderTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.ContinueCardTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.encontrarContinuarTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.SerieDetailTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.SerieTemaACardItem
import com.kalidorabr.bancodeminutagens.ui.temaa.SettingsTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.EmBreveListaTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.EstadoVazioTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.IconeColecaoVaziaTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.IconeFiltroVazioTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.GoogleSheetsTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.BuscarTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.DadosBackupTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.SobreTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.LixeiraTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.TemporadaDetailTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.components.HomeBottomNavigationTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.transicaoTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.CampoNavTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.NavDestinoTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.transicaoNavTemaA
import com.kalidorabr.bancodeminutagens.ui.temab.transicaoNavTemaB
import com.kalidorabr.bancodeminutagens.ui.temac.transicaoNavTemaC
import com.kalidorabr.bancodeminutagens.ui.temad.transicaoNavTemaD
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.CompositionLocalProvider
import com.kalidorabr.bancodeminutagens.ui.temaa.components.LocalSharedTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.LocalAnimScopeTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.AvisoTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TipoAvisoTemaA
import com.kalidorabr.bancodeminutagens.ui.temas.EstadoItemIntroDb
import com.kalidorabr.bancodeminutagens.ui.temas.ItemProgressoIntroDb
import com.kalidorabr.bancodeminutagens.ui.temas.ProgressoIntroDb
import com.kalidorabr.bancodeminutagens.ui.season.BatchFillRequest
import com.kalidorabr.bancodeminutagens.ui.theme.AppThemeOpcao
import com.kalidorabr.bancodeminutagens.ui.theme.BancoDeMinutagensTheme
import com.kalidorabr.bancodeminutagens.ui.theme.BorderColor
import com.kalidorabr.bancodeminutagens.ui.theme.DarkBackground
import com.kalidorabr.bancodeminutagens.ui.theme.RedPrimary
import com.kalidorabr.bancodeminutagens.ui.theme.StatusGreen
import com.kalidorabr.bancodeminutagens.ui.theme.SurfaceDark
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.kalidorabr.bancodeminutagens.ui.splash.AberturaBM
import com.kalidorabr.bancodeminutagens.ui.splash.ProntidaoInicial
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Atalhos do ícone do app ("Continuar minutagem" e "Buscar série")
        val acaoAtalho = intent?.action

        setContent {
            val context = LocalContext.current
            val themePrefs = remember(context) { AppThemePreferences(context) }
            var selectedTheme by remember { mutableStateOf(themePrefs.getSelectedTheme()) }
            var visualMode by remember {
                mutableStateOf(themePrefs.getVisualMode().also {
                    com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual.modo = com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual.de(it)
                })
            }

            var showAnimatedSplash by remember { mutableStateOf(true) }

            BancoDeMinutagensTheme(selectedTheme = selectedTheme) {
                val conteudoApp: @Composable () -> Unit = {
                Box(modifier = Modifier.fillMaxSize()) {
                    BancoDeMinutagensApp(
                        selectedTheme = selectedTheme,
                        onThemeSelected = { newTheme ->
                            selectedTheme = newTheme
                            themePrefs.saveSelectedTheme(newTheme)
                        },
                        visualMode = visualMode,
                        onVisualModeSelected = { newMode ->
                            // Troca as cores antes de redesenhar, para nenhuma tela pegar a cor do tema anterior
                            com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual.modo = com.kalidorabr.bancodeminutagens.ui.temaa.TemaVisual.de(newMode)
                            visualMode = newMode
                            themePrefs.saveVisualMode(newMode)
                        },
                        acaoInicial = acaoAtalho
                    )

                    if (showAnimatedSplash) {
                        AberturaBM(onFinished = { showAnimatedSplash = false })
                    }
                }
                }
                ProvedorTemaNovo(visualMode, selectedTheme, conteudoApp)
            }
        }
    }
}

data class PendingUndoClear(
    val serieId: String,
    val backupSeriesList: List<SerieItem>,
    val clearedEpisodioIds: Set<String>
)

data class PendingUndoCopy(
    val serieId: String,
    val temporadaId: String,
    val episodioId: String,
    val backupMinutagens: List<MinutagemSecao>,
    val sourceLabel: String
)

data class PendingUndoBatch(val backupSeriesList: List<SerieItem>, val affectedCount: Int)

enum class SyncOperation {
    SAVE_EPISODE,
    CLEAR_EPISODES
}

sealed interface EpisodeFeedback {
    data object None : EpisodeFeedback
    data object EpisodeSaveSuccess : EpisodeFeedback
    data object EpisodeSavedAndSynced : EpisodeFeedback
    data class EpisodesClearedSuccess(val count: Int) : EpisodeFeedback
    data class EpisodesClearedAndSynced(val count: Int) : EpisodeFeedback
    data object EpisodeSavePending : EpisodeFeedback
    data class EpisodesClearPending(val count: Int) : EpisodeFeedback
    data class SyncUnconfirmed(val message: String) : EpisodeFeedback
    data class Error(val message: String) : EpisodeFeedback
}

fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@Composable
fun BancoDeMinutagensApp(
    selectedTheme: AppThemeOpcao = AppThemeOpcao.VERMELHO,
    onThemeSelected: (AppThemeOpcao) -> Unit = {},
    visualMode: VisualMode = VisualMode.TEMA_A,
    onVisualModeSelected: (VisualMode) -> Unit = {},
    acaoInicial: String? = null
) {
    val context = LocalContext.current
    val repository = remember(context) { SeriesRepository(context) }
    val prefs = remember(context) { GoogleSheetsPreferences(context) }
    val coroutineScope = rememberCoroutineScope()
    var pendingJsonExport by remember { mutableStateOf("") }
    val jsonExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                    writer.write(pendingJsonExport)
                } ?: error("Não foi possível abrir o arquivo")
            }.onSuccess {
                Toast.makeText(context, "JSON exportado com sucesso", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Não foi possível exportar o JSON", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun exportarTemporadaJson(serie: SerieItem, temporada: TemporadaItem) {
        pendingJsonExport = IntroJsonExporter.exportar(serie, temporada)
        val nomeSeguro = serie.titulo
            .replace(Regex("[^A-Za-z0-9._-]+"), "-")
            .trim('-')
            .ifBlank { "serie" }
        val numeroTemporada = SerieProgressoUtils.numeroTemporada(temporada.numero) ?: 1
        jsonExportLauncher.launch("$nomeSeguro-temporada-$numeroTemporada.json")
    }

    var seriesListState by remember { mutableStateOf<List<SerieItem>>(emptyList()) }
    var pendingBackupExport by remember { mutableStateOf("") }
    val novidadesPrefs = remember(context) { com.kalidorabr.bancodeminutagens.data.NovidadesPreferences(context) }
    // muda a cada backup feito, para a Início esconder o lembrete na hora
    var marcaBackup by remember { mutableStateOf(novidadesPrefs.ultimoBackup()) }
    var serieComemorada by remember { mutableStateOf<SerieItem?>(null) }
    var maratona by remember { mutableStateOf(novidadesPrefs.maratona()) }
    // Versão nova publicada no GitHub (o app só avisa; quem instala é o Android, com a confirmação do usuário)
    var versaoNova by remember { mutableStateOf<com.kalidorabr.bancodeminutagens.data.VersaoNova?>(null) }
    // Atualização dentro do app: baixa o arquivo (com porcentagem) e entrega ao instalador do Android.
    // Se o Android ainda não permitir, abre a permissão e instala sozinho quando o usuário voltar.
    var baixandoAtualizacao by remember { mutableStateOf<Int?>(null) }
    var apkPronto by remember { mutableStateOf<java.io.File?>(null) }
    var avisoAtualizacao by remember { mutableStateOf<String?>(null) }
    fun entregarAoInstalador(arquivo: java.io.File) {
        val at = com.kalidorabr.bancodeminutagens.data.Atualizacao
        if (at.podeInstalar(context)) {
            apkPronto = null
            runCatching { at.instalar(context, arquivo) }
                .onFailure { avisoAtualizacao = "Não deu para abrir o instalador." }
        } else {
            apkPronto = arquivo
            avisoAtualizacao = "Permita \"Instalar apps\" para o Banco de Minutagens e volte: a instalação começa sozinha."
            runCatching { at.pedirPermissao(context) }
        }
    }
    fun iniciarAtualizacao() {
        val v = versaoNova ?: return
        if (baixandoAtualizacao != null) return
        apkPronto?.let { if (it.exists()) { entregarAoInstalador(it); return } }
        if (!com.kalidorabr.bancodeminutagens.data.Atualizacao.linkDeArquivo(v.link)) {
            // versão publicada sem o arquivo do app: abre a página dela
            runCatching { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(v.link))) }
            return
        }
        avisoAtualizacao = null
        baixandoAtualizacao = 0
        coroutineScope.launch {
            runCatching {
                com.kalidorabr.bancodeminutagens.data.Atualizacao.baixar(context, v.link) { baixandoAtualizacao = it }
            }.onSuccess { entregarAoInstalador(it) }
                .onFailure { avisoAtualizacao = "Não deu para baixar a versão ${v.versao}. Veja a internet e tente de novo." }
            baixandoAtualizacao = null
        }
    }

    // Confere ao abrir e também toda vez que o usuário volta para o app (no máximo a cada 30 min)
    var ultimaConferencia by remember { mutableStateOf(0L) }
    val donoCiclo = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(donoCiclo) {
        val observador = androidx.lifecycle.LifecycleEventObserver { _, evento ->
            if (evento == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                // voltou da tela de permissão: segue com a instalação
                apkPronto?.let { if (com.kalidorabr.bancodeminutagens.data.Atualizacao.podeInstalar(context)) { avisoAtualizacao = null; entregarAoInstalador(it) } }
                val agora = System.currentTimeMillis()
                if (agora - ultimaConferencia >= 30 * 60 * 1000L && isNetworkAvailable(context)) {
                    ultimaConferencia = agora
                    coroutineScope.launch {
                        com.kalidorabr.bancodeminutagens.data.Atualizacao.verificar(com.kalidorabr.bancodeminutagens.BuildConfig.VERSION_NAME)
                            ?.let { versaoNova = it }
                    }
                }
            }
        }
        donoCiclo.lifecycle.addObserver(observador)
        onDispose { donoCiclo.lifecycle.removeObserver(observador) }
    }
    var avisoEstreia by remember { mutableStateOf(novidadesPrefs.avisoEstreia()) }
    var backupAutoPasta by remember { mutableStateOf(com.kalidorabr.bancodeminutagens.data.BackupAutomatico.nomePasta(context)) }
    val escolherPastaBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            runCatching { com.kalidorabr.bancodeminutagens.data.BackupAutomatico.ligar(context, uri) }
                .onSuccess {
                    backupAutoPasta = com.kalidorabr.bancodeminutagens.data.BackupAutomatico.nomePasta(context)
                    Toast.makeText(context, "Backup automático ligado. O primeiro já está sendo feito.", Toast.LENGTH_LONG).show()
                }
                .onFailure { Toast.makeText(context, "Não foi possível usar essa pasta", Toast.LENGTH_LONG).show() }
        }
    }
    fun aplicarAvisoEstreia(ligado: Boolean) {
        novidadesPrefs.salvarAvisoEstreia(ligado)
        avisoEstreia = ligado
        if (ligado) com.kalidorabr.bancodeminutagens.data.EstreiaAgenda.ligar(context)
        else com.kalidorabr.bancodeminutagens.data.EstreiaAgenda.desligar(context)
    }
    // Android 13+: pede a permissão de notificação ao ligar o aviso de estreia
    val permissaoNotificacao = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        aplicarAvisoEstreia(ok)
        if (!ok) Toast.makeText(context, "Sem permissão de notificação, o aviso de estreia fica desligado", Toast.LENGTH_LONG).show()
    }
    // IntroDB: envio manual, separado da sincronização com o Google Sheets
    val introDbPrefs = remember(context) { com.kalidorabr.bancodeminutagens.data.IntroDbPreferences(context).also { it.refazerConferenciasUmaVez() } }
    var chaveIntroDb by remember { mutableStateOf(introDbPrefs.chave()) }
    var avisoIntroDb by remember { mutableStateOf<Triple<String, String, TipoAvisoTemaA>?>(null) }
    // Pop-up "esse trecho já foi para o IntroDB, quer enviar de novo?"
    var perguntaReenvio by remember { mutableStateOf<PerguntaReenvio?>(null) }
    var enviandoIntroDb by remember { mutableStateOf(false) }
    var progressoIntroDb by remember { mutableStateOf<ProgressoIntroDb?>(null) }
    var autoIntroDb by remember { mutableStateOf(introDbPrefs.automatico()) }
    // muda quando a conferência ou um envio termina, para as telas relerem o que está no site
    var marcaIntroDb by remember { mutableIntStateOf(0) }
    // Tempos que o IntroDB tem do episódio aberto (para importar nas seções vazias)
    var temposIntroDb by remember { mutableStateOf<Pair<String, com.kalidorabr.bancodeminutagens.model.TemposIntroDb>?>(null) }
    var episodiosNovos by remember { mutableStateOf<List<com.kalidorabr.bancodeminutagens.model.EpisodiosNovosSerie>>(emptyList()) }
    val backupExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(pendingBackupExport) }
                ?: error("Arquivo indisponível")
        }.onSuccess {
            novidadesPrefs.registrarBackup()
            marcaBackup = novidadesPrefs.ultimoBackup()
            Toast.makeText(context, "Backup exportado", Toast.LENGTH_SHORT).show()
        }
            .onFailure { Toast.makeText(context, "Falha ao exportar backup", Toast.LENGTH_LONG).show() }
    }
    val backupImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: error("Arquivo indisponível")
            novidadesPrefs.mesclarConclusoes(repository.lerConclusoesBackup(content))
            repository.lerBackup(content)
        }.onSuccess { restored ->
            episodiosNovos = emptyList() // eram da coleção antiga
            seriesListState = restored
            coroutineScope.launch { repository.salvarSeries(restored) }
            Toast.makeText(context, "Backup restaurado com sucesso", Toast.LENGTH_SHORT).show()
        }.onFailure { Toast.makeText(context, "Backup inválido ou corrompido", Toast.LENGTH_LONG).show() }
    }

    var currentFeedback by remember { mutableStateOf<EpisodeFeedback>(EpisodeFeedback.None) }
    var autoSyncJob by remember { mutableStateOf<Job?>(null) }
    var isAutoSyncingNow by remember { mutableStateOf(false) }
    var pendingChangesWhileSyncing by remember { mutableStateOf(false) }

    LaunchedEffect(currentFeedback) {
        if (currentFeedback !is EpisodeFeedback.None) {
            val timeout = if (currentFeedback is EpisodeFeedback.EpisodeSavePending ||
                currentFeedback is EpisodeFeedback.EpisodesClearPending ||
                currentFeedback is EpisodeFeedback.SyncUnconfirmed ||
                currentFeedback is EpisodeFeedback.Error) 4000L else 2500L
            // Tema A: avisos de erro ficam até a pessoa tocar em OK.
            if (currentFeedback is EpisodeFeedback.Error) return@LaunchedEffect
            delay(timeout)
            currentFeedback = EpisodeFeedback.None
        }
    }

    suspend fun executarAutoExportacao(
        operation: SyncOperation = SyncOperation.SAVE_EPISODE,
        clearedCount: Int = 1
    ) {
        val config = prefs.getConfig()
        if (!config.isAutoSyncUnlocked || !config.isAutoSyncEnabled) return
        if (config.scriptUrl.isBlank() || config.spreadsheetId.isBlank()) return

        if (isAutoSyncingNow) {
            pendingChangesWhileSyncing = true
            prefs.setPendingExport(true)
            return
        }

        if (!isNetworkAvailable(context)) {
            prefs.setPendingExport(true)
            currentFeedback = if (operation == SyncOperation.CLEAR_EPISODES) {
                EpisodeFeedback.EpisodesClearPending(clearedCount)
            } else {
                EpisodeFeedback.EpisodeSavePending
            }
            return
        }

        isAutoSyncingNow = true

        try {
            val pendingClears = prefs.getPendingClearOperations()
            if (pendingClears.isNotEmpty()) {
                var allClearsSuccessful = true
                for (item in pendingClears) {
                    val resultClear = GoogleSheetsExportService.executarClearEpisodes(
                        scriptUrl = config.scriptUrl,
                        spreadsheetId = config.spreadsheetId,
                        pendingClearItem = item
                    )
                    if (resultClear.success) {
                        prefs.removePendingClearOperation(item.tmdbId)
                    } else {
                        allClearsSuccessful = false
                    }
                }

                if (!allClearsSuccessful) {
                    prefs.setPendingExport(true)
                    currentFeedback = EpisodeFeedback.EpisodesClearPending(clearedCount)
                    return
                } else if (operation == SyncOperation.CLEAR_EPISODES) {
                    prefs.setPendingExport(false)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
                    prefs.saveLastSync(dateFormat.format(Date()), "Sucesso")
                    currentFeedback = EpisodeFeedback.EpisodesClearedAndSynced(clearedCount)
                    return
                }
            }

            val seriesParaExportar = seriesListState.filter { !it.isDeleted }
            val resultado = GoogleSheetsExportService.executarExportacao(
                scriptUrl = config.scriptUrl,
                spreadsheetId = config.spreadsheetId,
                seriesLocais = seriesParaExportar
            )

            if (resultado.success) {
                prefs.setPendingExport(false)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
                prefs.saveLastSync(dateFormat.format(Date()), "Sucesso")
                currentFeedback = if (operation == SyncOperation.CLEAR_EPISODES) {
                    EpisodeFeedback.EpisodesClearedAndSynced(clearedCount)
                } else {
                    EpisodeFeedback.EpisodeSavedAndSynced
                }
            } else {
                prefs.setPendingExport(true)
                currentFeedback = if (operation == SyncOperation.CLEAR_EPISODES) {
                    EpisodeFeedback.EpisodesClearPending(clearedCount)
                } else {
                    EpisodeFeedback.SyncUnconfirmed(
                        resultado.errorMessage
                            ?: "Dados enviados, mas a planilha não confirmou a sincronização."
                    )
                }
            }
        } catch (_: Exception) {
            prefs.setPendingExport(true)
            currentFeedback = EpisodeFeedback.Error("Erro ao sincronizar")
        } finally {
            isAutoSyncingNow = false
            if (pendingChangesWhileSyncing) {
                pendingChangesWhileSyncing = false
                coroutineScope.launch {
                    executarAutoExportacao(operation, clearedCount)
                }
            }
        }
    }

    val notificarAlteracaoUsuario = { operation: SyncOperation ->
        val config = prefs.getConfig()
        if (config.isAutoSyncUnlocked && config.isAutoSyncEnabled) {
            prefs.setPendingExport(true)
            currentFeedback = if (operation == SyncOperation.CLEAR_EPISODES) {
                EpisodeFeedback.EpisodesClearPending(1)
            } else {
                EpisodeFeedback.EpisodeSavePending
            }

            autoSyncJob?.cancel()
            autoSyncJob = coroutineScope.launch {
                delay(1000) // Debounce de 1 segundo
                executarAutoExportacao(operation)
            }
        } else {
            prefs.setPendingExport(false)
            currentFeedback = if (operation == SyncOperation.CLEAR_EPISODES) {
                EpisodeFeedback.EpisodesClearedSuccess(1)
            } else {
                EpisodeFeedback.EpisodeSaveSuccess
            }
        }
    }

    LaunchedEffect(Unit) {
        var seriesSalvas = repository.carregarSeries().map { SerieProgressoUtils.recalcularProgressoSerie(it) }
        // Instalação nova começa vazia (a Início mostra "Sua coleção está vazia · Buscar séries")
        seriesListState = seriesSalvas
        coroutineScope.launch {
            repository.salvarSeries(seriesSalvas)
        }

        // Enquanto a abertura roda: baixa as imagens que a Início mostra primeiro
        // (cartão "Continuar" e os primeiros pôsteres) e só então libera a abertura.
        coroutineScope.launch {
            val urls = seriesSalvas.filterNot { it.isDeleted }
                .sortedByDescending { it.lastUpdatedAt ?: 0L }
                .take(8)
                .flatMap { listOf(it.imageUrl.ifBlank { it.posterUrl }, it.posterUrl.ifBlank { it.imageUrl }) }
                .filter { it.isNotBlank() }
                .distinct()
            val carregador = coil.Coil.imageLoader(context)
            kotlinx.coroutines.withTimeoutOrNull(1500) {
                urls.map { url ->
                    async { carregador.execute(coil.request.ImageRequest.Builder(context).data(url).build()) }
                }.awaitAll()
            }
            ProntidaoInicial.pronto = true
        }

        // Tenta exportar alterações pendentes no startup se houver internet
        val config = prefs.getConfig()
        if (config.isAutoSyncUnlocked && config.isAutoSyncEnabled && config.hasPendingExport && isNetworkAvailable(context)) {
            coroutineScope.launch {
                executarAutoExportacao()
            }
        }

        // Enriquecimento automático de metadados, pôsteres e datas de episódios se ausentes
        val seriesComDadosAusentes = seriesSalvas.filter { serie ->
            // duração dos episódios: busca uma vez por série
            !novidadesPrefs.codigosConferidos(serie.id) ||
            (serie.tvdbId.isBlank() && !novidadesPrefs.wikidataConferido(serie.id)) ||
            (!novidadesPrefs.duracaoBuscada(serie.id) && serie.temporadas.any { t -> t.episodios.any { it.duracaoMin == 0 } }) ||
            serie.posterUrl.isBlank() ||
            serie.sinopse.startsWith("Série importada") ||
            serie.temporadas.any { temp ->
                temp.imageUrl.isBlank() || temp.episodios.any { ep -> ep.dataExibicao.isBlank() }
            }
        }

        if (seriesComDadosAusentes.isNotEmpty()) {
            coroutineScope.launch {
                var listAtualizada = seriesSalvas
                for (serie in seriesComDadosAusentes) {
                    try {
                        val serieEnriquecida = TmdbService.enriquecerSerieComTmdb(serie)
                        listAtualizada = listAtualizada.map { s ->
                            if (s.id == serie.id) serieEnriquecida else s
                        }
                        if (serieEnriquecida !== serie) {
                            novidadesPrefs.marcarDuracaoBuscada(serie.id)
                            novidadesPrefs.marcarCodigosConferidos(serie.id)
                            if (serieEnriquecida.imdbId != serie.imdbId) {
                                introDbPrefs.esquecerConferencias(serie.temporadas.flatMap { t -> t.episodios.map { it.id } })
                            }
                            // códigos novos ou corrigidos: vão para as colunas J e K na próxima sincronização
                            if (serieEnriquecida.imdbId != serie.imdbId || serieEnriquecida.tvdbId != serie.tvdbId) {
                                prefs.setPendingExport(true)
                            }
                        }
                    } catch (_: Exception) {}
                }
                if (listAtualizada != seriesSalvas) {
                    seriesListState = listAtualizada
                    repository.salvarSeries(listAtualizada)
                }
            }
        }

        // Episódios novos: consulta o TMDB (só leitura) e mostra um cartão na Início.
        // Nada é adicionado sem o usuário tocar em "Adicionar".
        if (isNetworkAvailable(context)) {
            coroutineScope.launch {
                val hojeIso = java.time.LocalDate.now().toString()
                val dispensados = novidadesPrefs.episodiosDispensados()
                val achados = seriesSalvas.filterNot { it.isDeleted }.mapNotNull { serie ->
                    TmdbService.buscarEpisodiosNovos(serie, hojeIso)
                        ?.takeIf { (dispensados[it.serieId] ?: -1) < it.totalTmdb }
                }
                // ignora séries que mudaram enquanto a busca rodava (por exemplo, já atualizadas)
                episodiosNovos = achados.filter { n ->
                    val atual = seriesListState.firstOrNull { it.id == n.serieId }
                    atual != null && n.temporadas.any { t ->
                        val local = atual.temporadas.firstOrNull { it.id == t.id }
                        local == null || t.episodios.any { e -> local.episodios.none { it.numero == e.numero } }
                    }
                }
            }
        }
    }

    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                coroutineScope.launch {
                    val config = prefs.getConfig()
                    if (config.isAutoSyncUnlocked && config.isAutoSyncEnabled && config.hasPendingExport) {
                        executarAutoExportacao()
                    }
                }
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            cm?.registerNetworkCallback(request, callback)
        } catch (_: Exception) {}

        onDispose {
            try {
                cm?.unregisterNetworkCallback(callback)
            } catch (_: Exception) {}
        }
    }

    val atualizarESalvarSeries = { novasSeries0: List<SerieItem> ->
        // Status automático (Finalizada / Incompleta) só para séries cujas minutagens mudaram;
        // trocas manuais de status no selo não são sobrescritas.
        val anteriores = seriesListState.associateBy { it.id }
        val novasSeries = novasSeries0.map { nova ->
            val antiga = anteriores[nova.id]
            fun marcas(s: SerieItem) = s.temporadas.map { t -> t.episodios.map { it.id to it.minutagens } }
            if (antiga != null && marcas(antiga) != marcas(nova)) {
                SerieProgressoUtils.aplicarStatusAutomatico(nova)
            } else nova
        }
        seriesListState = novasSeries
        coroutineScope.launch {
            repository.salvarSeries(novasSeries)
        }
    }

    // Código do IMDb da série: o da série, ou buscado no TMDB (guardado só no aparelho, sem sincronizar)
    suspend fun imdbDaSerie(serieId: String): String? {
        val serie = seriesListState.firstOrNull { it.id == serieId } ?: return null
        serie.imdbId.trim().takeIf { it.startsWith("tt") }?.let { return it }
        val achado = TmdbService.buscarImdbId(serie.id) ?: return null
        atualizarESalvarSeries(seriesListState.map { if (it.id == serie.id) it.copy(imdbId = achado) else it })
        return achado
    }

    // Confere no IntroDB (sem chave) o que já existe de cada episódio, para não repetir trabalho.
    // Roda com calma, um episódio por vez; para se o site pedir pausa.
    var conferindoIntroDb by remember { mutableStateOf(false) }
    // lote da conferência automática em andamento: a manual tem preferência e interrompe ele
    var loteAutomatico by remember { mutableStateOf<Job?>(null) }
    // número do lote automático atual: só o lote "dono" libera a conferência ao terminar
    val loteAtual = remember { intArrayOf(0) }
    fun conferirNoIntroDb(serieId: String, soNaoConferidos: Boolean = true, temporadaId: String? = null, aoProgresso: (Int, Int) -> Unit = { _, _ -> }, aoSemImdb: () -> Unit = {}, aoTerminar: (Int) -> Unit = {}) {
        loteAutomatico?.let { loteAtual[0]++; it.cancel(); loteAutomatico = null; conferindoIntroDb = false }
        if (conferindoIntroDb) return
        conferindoIntroDb = true
        coroutineScope.launch {
            try {
                val imdb = imdbDaSerie(serieId) ?: run { aoSemImdb(); return@launch }
                val serie = seriesListState.firstOrNull { it.id == serieId } ?: return@launch
                var comDados = 0
                val alvo = serie.temporadas.filter { temporadaId == null || it.id == temporadaId }
                val total = alvo.sumOf { t ->
                    if (SerieProgressoUtils.numeroTemporada(t.numero) == null) 0
                    else t.episodios.count { !(soNaoConferidos && introDbPrefs.conferido(it.id)) }
                }
                var feitos = 0
                aoProgresso(0, total)
                loop@ for (temporada in alvo) {
                    val numero = SerieProgressoUtils.numeroTemporada(temporada.numero) ?: continue
                    for (ep in temporada.episodios.sortedBy { it.numero }) {
                        if (soNaoConferidos && introDbPrefs.conferido(ep.id)) continue
                        feitos++
                        var tipos = com.kalidorabr.bancodeminutagens.data.IntroDbService.consultar(imdb, numero, ep.numero)
                        // o site pediu pausa: espera e tenta o mesmo episódio de novo (até 3 vezes)
                        var tentativas = 0
                        while (tipos != null && com.kalidorabr.bancodeminutagens.data.IntroDbService.LIMITE in tipos && tentativas < 3) {
                            tentativas++
                            delay(20_000L * tentativas)
                            tipos = com.kalidorabr.bancodeminutagens.data.IntroDbService.consultar(imdb, numero, ep.numero)
                        }
                        aoProgresso(feitos, total)
                        if (tipos == null) continue
                        if (com.kalidorabr.bancodeminutagens.data.IntroDbService.LIMITE in tipos) break@loop
                        introDbPrefs.salvarNoSite(ep.id, tipos)
                        if (tipos.isNotEmpty()) comDados++
                        marcaIntroDb++
                        delay(600)
                    }
                }
                aoTerminar(comDados)
            } finally {
                conferindoIntroDb = false
                marcaIntroDb++
            }
        }
    }

    // Conferência automática de todas as séries no IntroDB: começa assim que o app abre e repete a cada 10 min
    // enquanto ele está aberto; com o app fechado, o Android confere sozinho (ConferenciaIntroDbWorker).
    // A conferência manual de uma série tem preferência e interrompe o lote.
    fun conferirTodasNoIntroDb(limite: Int = 60, forcar: Boolean = false) {
        if (conferindoIntroDb) {
            // o lote automático já mostra a própria barra; outra conferência (de uma temporada ou envio) ganha um aviso
            if (forcar && loteAutomatico == null) {
                Toast.makeText(context, "Já tem uma conferência do IntroDB em andamento. Tente de novo em instantes.", Toast.LENGTH_SHORT).show()
            }
            return
        }
        conferindoIntroDb = true
        val meuLote = ++loteAtual[0]
        loteAutomatico = coroutineScope.launch {
            try {
                com.kalidorabr.bancodeminutagens.data.ConferenciaIntroDb.rodar(
                    context, seriesListState, limite,
                    imdbDe = { s -> imdbDaSerie(s.id) },
                    aoConferir = { marcaIntroDb++ },
                    forcar = forcar
                )
            } finally {
                if (loteAtual[0] == meuLote) { conferindoIntroDb = false; loteAutomatico = null }
            }
        }
    }
    LaunchedEffect(Unit) {
        com.kalidorabr.bancodeminutagens.data.ConferenciaIntroDb.estado.collect { marcaIntroDb++ }
    }
    LaunchedEffect(Unit) {
        com.kalidorabr.bancodeminutagens.data.ConferenciaIntroDb.agendar(context)
        delay(3_000L) // deixa a tela abrir primeiro
        while (true) {
            if (introDbPrefs.conferenciaAutomatica() && isNetworkAvailable(context)) conferirTodasNoIntroDb()
            delay(10 * 60_000L)
        }
    }

    // Envia ao IntroDB as seções com tempo dos episódios escolhidos (um por vez, com calma).
    // comPopup = envio manual (mostra o pop-up); sem = envio automático do Concluir (aviso discreto).
    fun enviarAoIntroDb(serieId: String, temporadaId: String, episodioIds: Set<String>, comPopup: Boolean = true, reenviar: Set<String> = emptySet()) {
        if (enviandoIntroDb || chaveIntroDb.isBlank()) return
        val serie = seriesListState.firstOrNull { it.id == serieId } ?: return
        val temporada = serie.temporadas.firstOrNull { it.id == temporadaId } ?: return
        val numero = SerieProgressoUtils.numeroTemporada(temporada.numero) ?: return
        val plano = com.kalidorabr.bancodeminutagens.model.IntroDbEnvio.planejar(numero, temporada.episodios.filter { it.id in episodioIds })
        // O que já está no site ou já foi enviado com os mesmos tempos não vai de novo
        // ([reenviar]: rótulos que o usuário pediu para mandar de novo com o tempo corrigido)
        val (repetidos, novos) = plano.envios.partition {
            it.rotulo !in reenviar && (introDbPrefs.jaEnviado(it) || it.segmentType in introDbPrefs.noSite(it.episodioId))
        }
        if (!comPopup && novos.isEmpty()) return
        var itens = novos.map { ItemProgressoIntroDb(it.rotulo, EstadoItemIntroDb.ESPERANDO) } +
            repetidos.map { ItemProgressoIntroDb(it.rotulo, EstadoItemIntroDb.JA_NO_SITE) } +
            plano.pulados.map { ItemProgressoIntroDb(it.rotulo, EstadoItemIntroDb.PULADO, it.motivo) }
        // mostra na ordem dos episódios e das seções (Abertura, Recap, Encerramento)
        val ordemSecao = listOf("Abertura", "Recap", "Encerramento")
        itens = itens.sortedWith(compareBy({ it.rotulo.substringBefore(" ·") }, { ordemSecao.indexOf(it.rotulo.substringAfter("· ")) }))
        fun mostrar(etapa: String = "", terminado: Boolean = false, aviso: String? = null, abrir: Boolean = false) {
            // fechou o pop-up ("segundo plano")? continua enviando sem reabrir
            if (comPopup && (abrir || progressoIntroDb != null)) progressoIntroDb = ProgressoIntroDb(serie.titulo, itens, etapa, terminado, aviso)
        }
        if (novos.isEmpty()) {
            mostrar("Nada novo para enviar", terminado = true, abrir = true)
            return
        }
        enviandoIntroDb = true
        mostrar("Preparando…", abrir = true)
        coroutineScope.launch {
            var aviso: String? = null
            try {
                val imdb = imdbDaSerie(serie.id)
                if (imdb == null) {
                    aviso = "A série não tem código do IMDb. Preencha em Identificadores e tente de novo."
                    return@launch
                }
                for ((i, item) in novos.withIndex()) {
                    itens = itens.map { if (it.rotulo == item.rotulo) it.copy(estado = EstadoItemIntroDb.ENVIANDO) else it }
                    mostrar("Enviando ${i + 1} de ${novos.size}…")
                    val estado = when (val r = com.kalidorabr.bancodeminutagens.data.IntroDbService.enviar(chaveIntroDb, imdb, item)) {
                        com.kalidorabr.bancodeminutagens.data.RespostaIntroDb.Ok -> {
                            introDbPrefs.marcarEnviado(item)
                            ItemProgressoIntroDb(item.rotulo, EstadoItemIntroDb.ENVIADO)
                        }
                        com.kalidorabr.bancodeminutagens.data.RespostaIntroDb.ChaveInvalida -> {
                            aviso = "O IntroDB recusou a chave. Confira em Ajustes › IntroDB."
                            ItemProgressoIntroDb(item.rotulo, EstadoItemIntroDb.ERRO, "chave recusada")
                        }
                        com.kalidorabr.bancodeminutagens.data.RespostaIntroDb.Limite -> {
                            aviso = "O IntroDB pediu uma pausa. Tente de novo daqui a pouco; o que já foi não vai repetir."
                            ItemProgressoIntroDb(item.rotulo, EstadoItemIntroDb.ERRO, "limite de envios")
                        }
                        is com.kalidorabr.bancodeminutagens.data.RespostaIntroDb.Erro ->
                            ItemProgressoIntroDb(item.rotulo, EstadoItemIntroDb.ERRO, r.mensagem)
                    }
                    itens = itens.map { if (it.rotulo == item.rotulo) estado else it }
                    if (aviso != null) break
                    delay(600)
                }
            } finally {
                enviandoIntroDb = false
                marcaIntroDb++
                mostrar(terminado = true, aviso = aviso)
                val enviados = novos.filter { n -> itens.any { it.rotulo == n.rotulo && it.estado == EstadoItemIntroDb.ENVIADO } }
                if (enviados.isNotEmpty()) coroutineScope.launch {
                    delay(5_000L)
                    val imdb = imdbDaSerie(serie.id) ?: return@launch
                    enviados.distinctBy { it.episodioId }.forEach { e ->
                        val tipos = com.kalidorabr.bancodeminutagens.data.IntroDbService.consultar(imdb, e.temporada, e.episodio)
                        if (tipos != null && com.kalidorabr.bancodeminutagens.data.IntroDbService.LIMITE !in tipos) {
                            introDbPrefs.salvarNoSite(e.episodioId, tipos)
                            marcaIntroDb++
                        }
                        delay(1_200L)
                    }
                }
                if (!comPopup || progressoIntroDb == null) {
                    val ok = itens.count { it.estado == EstadoItemIntroDb.ENVIADO }
                    val problema = aviso
                    avisoIntroDb = when {
                        problema != null -> Triple("IntroDB: não enviado", problema, TipoAvisoTemaA.ERRO)
                        itens.any { it.estado == EstadoItemIntroDb.ERRO } -> Triple("IntroDB: $ok enviada(s), com erro", "Tente pelo menu ⋮ da temporada.", TipoAvisoTemaA.ATENCAO)
                        ok > 0 -> Triple("Enviado ao IntroDB", novos.first().rotulo.substringBefore(" ·"), TipoAvisoTemaA.SUCESSO)
                        else -> null
                    }
                }
            }
        }
    }

    // Depois de salvar minutagens: guarda o dia de cada episódio concluído (estatísticas)
    // e comemora quando uma série fica com todos os episódios feitos.
    fun registrarProgresso(antes: List<SerieItem>) {
        val anteriores = antes.associateBy { it.id }
        val hoje = java.time.LocalDate.now().toEpochDay()
        seriesListState.forEach { depois ->
            val antiga = anteriores[depois.id] ?: return@forEach
            if (antiga.temporadas == depois.temporadas) return@forEach
            val jaConcluidos = antiga.temporadas.flatMap { it.episodios }
                .filter { it.status == StatusEpisodio.CONCLUIDO }.map { it.id }.toSet()
            depois.temporadas.flatMap { it.episodios }
                .filter { it.status == StatusEpisodio.CONCLUIDO && it.id !in jaConcluidos }
                .forEach { novidadesPrefs.registrarConclusao(it.id, hoje) }
            if (com.kalidorabr.bancodeminutagens.model.Novidades.acabouDeCompletar(antiga, depois)) {
                serieComemorada = depois
            }
        }
    }

    var selectedHomeFilter by remember { mutableStateOf("Em andamento") }
    var selectedSerieId by remember { mutableStateOf<String?>(null) }
    var selectedTemporadaId by remember { mutableStateOf<String?>(null) }
    var selectedEpisodioId by remember { mutableStateOf<String?>(null) }
    var selectedSecaoTipo by remember { mutableStateOf<TipoMinutagem?>(null) }
    var isSyncingTmdbTemaA by remember { mutableStateOf(false) }
    var pendingUndoCopyState by remember { mutableStateOf<PendingUndoCopy?>(null) }
    // "Salvo · Desfazer": guarda como a seção estava antes de salvar, por alguns segundos
    var pendingUndoSaveState by remember { mutableStateOf<PendingUndoCopy?>(null) }
    var saveUndoSincronizou by remember { mutableStateOf(false) }
    var saveUndoJob by remember { mutableStateOf<Job?>(null) }
    var pendingUndoBatchState by remember { mutableStateOf<PendingUndoBatch?>(null) }
    var copyUndoJob by remember { mutableStateOf<Job?>(null) }
    var batchUndoJob by remember { mutableStateOf<Job?>(null) }

    BackHandler(
        enabled = selectedSecaoTipo != null || selectedEpisodioId != null ||
            selectedTemporadaId != null || selectedSerieId != null
    ) {
        when {
            selectedSecaoTipo != null -> selectedSecaoTipo = null
            selectedEpisodioId != null -> selectedEpisodioId = null
            selectedTemporadaId != null -> selectedTemporadaId = null
            selectedSerieId != null -> selectedSerieId = null
        }
    }

    // Ao abrir um episódio com seção vazia, procura no IntroDB (só leitura, sem chave)
    LaunchedEffect(selectedEpisodioId) {
        val epId = selectedEpisodioId ?: return@LaunchedEffect
        if (temposIntroDb?.first == epId) return@LaunchedEffect
        temposIntroDb = null
        val serie = seriesListState.firstOrNull { it.id == selectedSerieId } ?: return@LaunchedEffect
        val temporada = serie.temporadas.firstOrNull { t -> t.episodios.any { it.id == epId } } ?: return@LaunchedEffect
        val ep = temporada.episodios.first { it.id == epId }
        if (ep.minutagens.none { it.estado == EstadoMinutagem.PENDENTE } && ep.minutagens.size >= 3) return@LaunchedEffect
        val numero = SerieProgressoUtils.numeroTemporada(temporada.numero) ?: return@LaunchedEffect
        val imdb = imdbDaSerie(serie.id) ?: return@LaunchedEffect
        val tempos = com.kalidorabr.bancodeminutagens.data.IntroDbService.buscarTempos(imdb, numero, ep.numero) ?: return@LaunchedEffect
        // guarda que essas seções já estão no site (assim não são reenviadas)
        val tipos: Set<String> = buildSet {
            tempos.secoes.keys.forEach { add(when (it) { TipoMinutagem.ABERTURA -> "intro"; TipoMinutagem.RECAP -> "recap"; TipoMinutagem.ENCERRAMENTO -> "outro" }) }
            tempos.naoPossui.forEach { add(when (it) { TipoMinutagem.ABERTURA -> "intro"; TipoMinutagem.RECAP -> "recap"; TipoMinutagem.ENCERRAMENTO -> "outro" }) }
        }
        introDbPrefs.salvarNoSite(ep.id, tipos)
        if (!tempos.vazio) temposIntroDb = epId to tempos
    }

    // Atalho "Continuar minutagem": abre direto no episódio assim que as séries carregarem
    var acaoPendente by remember { mutableStateOf(acaoInicial) }
    LaunchedEffect(seriesListState.isNotEmpty(), acaoPendente) {
        if (acaoPendente != "com.kalidorabr.bancodeminutagens.CONTINUAR" || seriesListState.isEmpty()) return@LaunchedEffect
        acaoPendente = null
        encontrarContinuarTemaA(seriesListState.filterNot { it.isDeleted })?.let { (_, destino) ->
            SerieProgressoUtils.localizarEpisodio(seriesListState, destino)?.let { (s, t, e) ->
                selectedSerieId = s.id
                selectedTemporadaId = t.id
                selectedEpisodioId = e.id
            }
        }
    }

    val activeSerie = remember(selectedSerieId, seriesListState) {
        seriesListState.firstOrNull { it.id == selectedSerieId }
    }

    val activeTemporada = remember(selectedTemporadaId, activeSerie) {
        activeSerie?.temporadas?.firstOrNull { it.id == selectedTemporadaId }
    }

    val activeEpisodio = remember(selectedEpisodioId, activeTemporada) {
        activeTemporada?.episodios?.firstOrNull { it.id == selectedEpisodioId }
    }

    // Salva minutagem da seção em memória e disco local SEM disparar auto-sync
    fun salvarMinutagem(
        serieId: String,
        temporadaId: String,
        episodioId: String,
        novaSecao: MinutagemSecao
    ) {
        var episodioCompleto = false
        val minutagensAntes = seriesListState.firstOrNull { it.id == serieId }
            ?.temporadas?.firstOrNull { it.id == temporadaId }
            ?.episodios?.firstOrNull { it.id == episodioId }?.minutagens
        val novasSeries = seriesListState.map { serie0 ->
            // Ao salvar a primeira minutagem, a série sai de "Em breve" e vai para "Em andamento"
            val serie = if (serie0.id == serieId && serie0.status.equals("Em breve", ignoreCase = true)) {
                serie0.copy(status = "Em andamento")
            } else serie0
            if (serie.id != serieId) serie else {
                val novasTemporadas = serie.temporadas.map { temporada ->
                    if (temporada.id != temporadaId) temporada else {
                        val novosEpisodios = temporada.episodios.map { ep ->
                            if (ep.id != episodioId) ep else {
                                val novasMinutagens = ep.minutagens.map { min ->
                                    if (min.tipo == novaSecao.tipo) novaSecao else min
                                }
                                episodioCompleto = novasMinutagens.count {
                                    it.estado == EstadoMinutagem.PREENCHIDO ||
                                        it.estado == EstadoMinutagem.NAO_POSSUI
                                } == 3
                                ep.copy(minutagens = novasMinutagens)
                            }
                        }
                        temporada.copy(episodios = novosEpisodios)
                    }
                }

                val serieAtualizada = serie.copy(
                    lastEditedEpisodeId = episodioId,
                    lastUpdatedAt = if (episodioCompleto) System.currentTimeMillis() else serie.lastUpdatedAt,
                    temporadas = novasTemporadas
                )
                SerieProgressoUtils.recalcularProgressoSerie(serieAtualizada)
            }
        }
        val antesDeSalvar = seriesListState
        atualizarESalvarSeries(novasSeries)
        registrarProgresso(antesDeSalvar)
        if (episodioCompleto) {
            notificarAlteracaoUsuario(SyncOperation.SAVE_EPISODE)
        }
        if (minutagensAntes != null) {
            saveUndoJob?.cancel()
            pendingUndoSaveState = PendingUndoCopy(
                serieId = serieId,
                temporadaId = temporadaId,
                episodioId = episodioId,
                backupMinutagens = minutagensAntes,
                sourceLabel = if (novaSecao.estado == EstadoMinutagem.PENDENTE) when (novaSecao.tipo) {
                    TipoMinutagem.ABERTURA -> "Abertura apagada"
                    TipoMinutagem.RECAP -> "Recap apagado"
                    TipoMinutagem.ENCERRAMENTO -> "Encerramento apagado"
                } else when (novaSecao.tipo) {
                    TipoMinutagem.ABERTURA -> "Abertura salva"
                    TipoMinutagem.RECAP -> "Recap salvo"
                    TipoMinutagem.ENCERRAMENTO -> "Encerramento salvo"
                }
            )
            saveUndoSincronizou = episodioCompleto
            saveUndoJob = coroutineScope.launch {
                delay(5000)
                pendingUndoSaveState = null
            }
        }
    }

    // Volta a seção para como estava antes do último "Salvar"
    fun desfazerSalvamento() {
        val pending = pendingUndoSaveState ?: return
        saveUndoJob?.cancel()
        saveUndoJob = null
        pendingUndoSaveState = null
        val novasSeries = seriesListState.map { serie ->
            if (serie.id != pending.serieId) serie else {
                val temporadas = serie.temporadas.map { temporada ->
                    if (temporada.id != pending.temporadaId) temporada else {
                        temporada.copy(
                            episodios = temporada.episodios.map { episodio ->
                                if (episodio.id == pending.episodioId) episodio.copy(minutagens = pending.backupMinutagens) else episodio
                            }
                        )
                    }
                }
                SerieProgressoUtils.recalcularProgressoSerie(serie.copy(temporadas = temporadas))
            }
        }
        atualizarESalvarSeries(novasSeries)
        // só avisa a sincronização se o salvamento desfeito tinha sido enviado
        if (saveUndoSincronizou) notificarAlteracaoUsuario(SyncOperation.SAVE_EPISODE)
    }

    fun copiarMinutagensDoEpisodioAnterior(
        serieId: String,
        temporadaId: String,
        episodioId: String,
        episodioAnterior: EpisodioItem,
        tipo: TipoMinutagem?,
        sourceLabel: String
    ) {
        val episodioAtual = seriesListState
            .firstOrNull { it.id == serieId }
            ?.temporadas?.firstOrNull { it.id == temporadaId }
            ?.episodios?.firstOrNull { it.id == episodioId }
            ?: return
        val backup = episodioAtual.minutagens.map { it.copy() }
        val minutagensCopiadas = episodioAtual.minutagens.map { atual ->
            if (tipo == null || atual.tipo == tipo) {
                episodioAnterior.minutagens.firstOrNull { it.tipo == atual.tipo }?.copy() ?: atual
            } else {
                atual
            }
        }
        val novasSeries = seriesListState.map { serie ->
            if (serie.id != serieId) serie else {
                val novasTemporadas = serie.temporadas.map { temporada ->
                    if (temporada.id != temporadaId) temporada else {
                        temporada.copy(
                            episodios = temporada.episodios.map { episodio ->
                                if (episodio.id == episodioId) {
                                    episodio.copy(minutagens = minutagensCopiadas)
                                } else {
                                    episodio
                                }
                            }
                        )
                    }
                }
                SerieProgressoUtils.recalcularProgressoSerie(
                    serie.copy(
                        lastEditedEpisodeId = episodioId,
                        lastUpdatedAt = System.currentTimeMillis(),
                        temporadas = novasTemporadas
                    )
                )
            }
        }
        val antesDaCopia = seriesListState
        atualizarESalvarSeries(novasSeries)
        registrarProgresso(antesDaCopia)
        val episodioCompleto = minutagensCopiadas.count {
            it.estado == EstadoMinutagem.PREENCHIDO || it.estado == EstadoMinutagem.NAO_POSSUI
        } == 3
        if (episodioCompleto) {
            notificarAlteracaoUsuario(SyncOperation.SAVE_EPISODE)
        }
        pendingUndoSaveState = null // a cópia substitui o "desfazer" do último salvamento
        copyUndoJob?.cancel()
        pendingUndoCopyState = PendingUndoCopy(
            serieId = serieId,
            temporadaId = temporadaId,
            episodioId = episodioId,
            backupMinutagens = backup,
            sourceLabel = sourceLabel
        )
        copyUndoJob = coroutineScope.launch {
            delay(5000)
            pendingUndoCopyState = null
        }
    }

    fun salvarStatusSerie(serieId: String, novoStatus: String) {
        val novasSeries = seriesListState.map { serie ->
            if (serie.id == serieId) {
                serie.copy(status = novoStatus)
            } else {
                serie
            }
        }
        atualizarESalvarSeries(novasSeries)
        notificarAlteracaoUsuario(SyncOperation.SAVE_EPISODE)
    }

    fun salvarIdentificadoresSerie(
        serieId: String,
        novoImdbId: String,
        novoTvdbId: String,
        novoResumo: String
    ) {
        val novasSeries = seriesListState.map { serie ->
            if (serie.id == serieId) {
                serie.copy(
                    imdbId = novoImdbId,
                    tvdbId = novoTvdbId,
                    identificadores = novoResumo
                )
            } else {
                serie
            }
        }
        atualizarESalvarSeries(novasSeries)
        // leva os códigos para as colunas J e K da planilha
        notificarAlteracaoUsuario(SyncOperation.SAVE_EPISODE)
    }

    fun salvarMinutagemFixa(
        serieId: String,
        abertura: Boolean,
        recap: Boolean,
        encerramento: Boolean,
        duracaoAbertura: String,
        duracaoRecap: String,
        duracaoEncerramento: String
    ) {
        val novasSeries = seriesListState.map { serie ->
            if (serie.id == serieId) {
                val ativasCount = (if (abertura) 1 else 0) + (if (recap) 1 else 0) + (if (encerramento) 1 else 0)
                val serieAtualizada = serie.copy(
                    aberturaAtiva = abertura,
                    recapAtivo = recap,
                    encerramentoAtivo = encerramento,
                    duracaoFixaAbertura = duracaoAbertura,
                    duracaoFixaRecap = duracaoRecap,
                    duracaoFixaEncerramento = duracaoEncerramento,
                    minutagensAtivas = "$ativasCount de 3 ativas"
                )
                SerieProgressoUtils.recalcularProgressoSerie(serieAtualizada)
            } else {
                serie
            }
        }
        atualizarESalvarSeries(novasSeries)
        notificarAlteracaoUsuario(SyncOperation.SAVE_EPISODE)
    }

    var pendingUndoClearState by remember { mutableStateOf<PendingUndoClear?>(null) }
    // Série movida para a lixeira: (id, título) enquanto o "Desfazer" estiver visível
    var pendingUndoLixeiraState by remember { mutableStateOf<Pair<String, String>?>(null) }
    var lixeiraUndoJob by remember { mutableStateOf<Job?>(null) }
    var undoJob by remember { mutableStateOf<Job?>(null) }
    fun desfazerCopia() {
        val pending = pendingUndoCopyState ?: return
        copyUndoJob?.cancel()
        copyUndoJob = null
        pendingUndoCopyState = null
        val novasSeries = seriesListState.map { serie ->
            if (serie.id != pending.serieId) serie else {
                val temporadas = serie.temporadas.map { temporada ->
                    if (temporada.id != pending.temporadaId) temporada else {
                        temporada.copy(
                            episodios = temporada.episodios.map { episodio ->
                                if (episodio.id == pending.episodioId) {
                                    episodio.copy(minutagens = pending.backupMinutagens)
                                } else episodio
                            }
                        )
                    }
                }
                SerieProgressoUtils.recalcularProgressoSerie(serie.copy(temporadas = temporadas))
            }
        }
        atualizarESalvarSeries(novasSeries)
        notificarAlteracaoUsuario(SyncOperation.SAVE_EPISODE)
    }

    fun desfazerLimpeza() {
        val pending = pendingUndoClearState ?: return
        undoJob?.cancel()
        undoJob = null
        pendingUndoClearState = null

        atualizarESalvarSeries(pending.backupSeriesList)
        currentFeedback = EpisodeFeedback.None
    }

    fun limparEpisodiosSelecionados(
        serieId: String,
        temporadaId: String,
        episodioIds: Set<String>
    ) {
        if (episodioIds.isEmpty()) return

        val backupList = seriesListState
        val agora = System.currentTimeMillis()

        val novasSeries = seriesListState.map { serie ->
            if (serie.id != serieId) serie else {
                val novasTemporadas = serie.temporadas.map { temporada ->
                    if (temporada.id != temporadaId) temporada else {
                        val novosEpisodios = temporada.episodios.map { ep ->
                            if (!episodioIds.contains(ep.id)) ep else {
                                val minutagensLimpas = ep.minutagens.map { min ->
                                    MinutagemSecao(
                                        tipo = min.tipo,
                                        estado = EstadoMinutagem.PENDENTE,
                                        tempoInicio = "",
                                        tempoFim = ""
                                    )
                                }
                                ep.copy(
                                    status = StatusEpisodio.PENDENTE,
                                    minutagens = minutagensLimpas
                                )
                            }
                        }
                        temporada.copy(episodios = novosEpisodios)
                    }
                }

                val serieAtualizada = serie.copy(
                    lastUpdatedAt = agora,
                    temporadas = novasTemporadas
                )
                SerieProgressoUtils.recalcularProgressoSerie(serieAtualizada)
            }
        }

        atualizarESalvarSeries(novasSeries)

        pendingUndoClearState = PendingUndoClear(
            serieId = serieId,
            backupSeriesList = backupList,
            clearedEpisodioIds = episodioIds
        )

        undoJob?.cancel()
        undoJob = coroutineScope.launch {
            delay(5000)
            pendingUndoClearState = null

            val targetSerie = seriesListState.firstOrNull { it.id == serieId }
            val targetTemp = targetSerie?.temporadas?.firstOrNull { it.id == temporadaId }

            if (targetSerie != null && targetTemp != null) {
                val seasonNum = parseSeasonNumber(targetTemp.numero)
                val tempStr = if (seasonNum < 10) "0$seasonNum" else "$seasonNum"

                val clearedEpsList = targetTemp.episodios.filter { episodioIds.contains(it.id) }.map { ep ->
                    val epStr = if (ep.numero < 10) "0${ep.numero}" else "${ep.numero}"
                    PendingClearEpisode(
                        season = "T$tempStr",
                        seasonNumber = seasonNum,
                        episode = "E$epStr",
                        episodeNumber = ep.numero,
                        title = ep.titulo
                    )
                }

                val pendingItem = PendingClearItem(
                    tmdbId = targetSerie.id,
                    seriesTitle = targetSerie.titulo,
                    episodes = clearedEpsList
                )

                prefs.addPendingClearOperation(pendingItem)
                executarAutoExportacao(
                    operation = SyncOperation.CLEAR_EPISODES,
                    clearedCount = episodioIds.size
                )
            }
        }
    }

    fun aplicarPreenchimentoEmLote(
        serieId: String,
        temporadaId: String,
        request: BatchFillRequest
    ) {
        if (request.targetEpisodeIds.isEmpty() || request.sectionTypes.isEmpty()) return
        val backupList = seriesListState
        val temporadaAtual = seriesListState.firstOrNull { it.id == serieId }
            ?.temporadas?.firstOrNull { it.id == temporadaId } ?: return
        val origem = temporadaAtual.episodios.firstOrNull { it.id == request.sourceEpisodeId } ?: return
        val secoesOrigem = origem.minutagens.associateBy { it.tipo }
        val agora = System.currentTimeMillis()

        val novasSeries = seriesListState.map { serie ->
            if (serie.id != serieId) serie else {
                val novasTemporadas = serie.temporadas.map { temporada ->
                    if (temporada.id != temporadaId) temporada else {
                        temporada.copy(
                            episodios = temporada.episodios.map { episodio ->
                                if (episodio.id !in request.targetEpisodeIds) episodio else {
                                    val atuais = episodio.minutagens.associateBy { it.tipo }
                                    val novasMinutagens = TipoMinutagem.entries.map { tipo ->
                                        val atual = atuais[tipo] ?: MinutagemSecao(tipo = tipo)
                                        val origemSecao = secoesOrigem[tipo]
                                        val campoVazio = atual.estado == EstadoMinutagem.PENDENTE &&
                                            atual.tempoInicio.isBlank() && atual.tempoFim.isBlank()
                                        if (tipo in request.sectionTypes && origemSecao != null &&
                                            origemSecao.estado != EstadoMinutagem.PENDENTE &&
                                            (request.overwriteExisting || campoVazio)) origemSecao.copy() else atual
                                    }
                                    episodio.copy(minutagens = novasMinutagens)
                                }
                            }
                        )
                    }
                }
                SerieProgressoUtils.recalcularProgressoSerie(
                    serie.copy(lastUpdatedAt = agora, temporadas = novasTemporadas)
                )
            }
        }
        val antesDoLote = seriesListState
        atualizarESalvarSeries(novasSeries)
        registrarProgresso(antesDoLote)
        notificarAlteracaoUsuario(SyncOperation.SAVE_EPISODE)
        batchUndoJob?.cancel()
        pendingUndoBatchState = PendingUndoBatch(backupList, request.targetEpisodeIds.size)
        batchUndoJob = coroutineScope.launch {
            delay(8000)
            pendingUndoBatchState = null
            batchUndoJob = null
        }
    }

    fun desfazerPreenchimentoEmLote() {
        val pending = pendingUndoBatchState ?: return
        batchUndoJob?.cancel()
        batchUndoJob = null
        pendingUndoBatchState = null
        atualizarESalvarSeries(pending.backupSeriesList)
        notificarAlteracaoUsuario(SyncOperation.SAVE_EPISODE)
    }

    fun desfazerLixeira() {
        val pendente = pendingUndoLixeiraState ?: return
        lixeiraUndoJob?.cancel()
        lixeiraUndoJob = null
        pendingUndoLixeiraState = null
        atualizarESalvarSeries(seriesListState.map { s ->
            if (s.id == pendente.first) s.copy(isDeleted = false, deletedAt = null) else s
        })
    }

    fun moverSerieParaLixeira(serieId: String) {
        val titulo = seriesListState.firstOrNull { it.id == serieId }?.titulo.orEmpty()
        pendingUndoLixeiraState = serieId to titulo
        lixeiraUndoJob?.cancel()
        lixeiraUndoJob = coroutineScope.launch {
            delay(5000)
            pendingUndoLixeiraState = null
        }
        val novasSeries = seriesListState.map { serie ->
            if (serie.id == serieId) {
                serie.copy(
                    isDeleted = true,
                    deletedAt = System.currentTimeMillis()
                )
            } else {
                serie
            }
        }
        atualizarESalvarSeries(novasSeries)
        selectedSerieId = null
    }

    fun restaurarSerieDaLixeira(serie: SerieItem) {
        prefs.removePermanentlyDeletedTmdbId(serie.id)
        val novasSeries = seriesListState.map { s ->
            if (s.id == serie.id) {
                s.copy(
                    isDeleted = false,
                    deletedAt = null
                )
            } else {
                s
            }
        }
        atualizarESalvarSeries(novasSeries)
        notificarAlteracaoUsuario(SyncOperation.SAVE_EPISODE)
    }

    fun excluirSerieDefinitivamente(serie: SerieItem) {
        prefs.addPermanentlyDeletedTmdbId(serie.id)
        val novasSeries = seriesListState.filter { it.id != serie.id }
        atualizarESalvarSeries(novasSeries)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        val setSerieReal: (String?) -> Unit = { selectedSerieId = it }
        val setTemporadaReal: (String?) -> Unit = { selectedTemporadaId = it }
        val setEpisodioReal: (String?) -> Unit = { selectedEpisodioId = it }
        val seriesParaTela = seriesListState

        // Desenha uma tela da navegação. Recebe os ids "congelados" da tela, para que,
        // durante a transição, a tela que sai continue mostrando o próprio conteúdo.
        val telaNavegacao: @Composable (NavDestinoTemaA) -> Unit = { alvo ->
            var selectedSerieId: String? by CampoNavTemaA(alvo.serieId, setSerieReal)
            var selectedTemporadaId: String? by CampoNavTemaA(alvo.temporadaId, setTemporadaReal)
            var selectedEpisodioId: String? by CampoNavTemaA(alvo.episodioId, setEpisodioReal)
            val activeSerie = seriesParaTela.firstOrNull { it.id == alvo.serieId }
            val activeTemporada = activeSerie?.temporadas?.firstOrNull { it.id == alvo.temporadaId }
            val activeEpisodio = activeTemporada?.episodios?.firstOrNull { it.id == alvo.episodioId }
            if (selectedEpisodioId != null && activeSerie != null && activeTemporada != null && activeEpisodio != null) {
                val statusLabel = when (activeEpisodio.status) {
                    StatusEpisodio.CONCLUIDO -> "Concluído"
                    StatusEpisodio.PROXIMO -> "Próximo"
                    StatusEpisodio.PARCIAL -> "Parcial"
                    StatusEpisodio.PENDENTE -> "Pendente"
                }

                val episodiosOrdenados = remember(activeTemporada) {
                    activeTemporada.episodios.sortedBy { it.numero }
                }
                val currentIndex = remember(selectedEpisodioId, episodiosOrdenados) {
                    episodiosOrdenados.indexOfFirst { it.id == selectedEpisodioId }
                }

                val hasAnterior = currentIndex > 0
                val hasProximo = currentIndex >= 0 && currentIndex < episodiosOrdenados.size - 1
                val episodioAnterior = episodiosOrdenados.getOrNull(currentIndex - 1)
                val podeCopiarAnterior = episodioAnterior?.minutagens?.any { secao ->
                    secao.estado != EstadoMinutagem.PENDENTE ||
                        secao.tempoInicio.isNotBlank() || secao.tempoFim.isNotBlank()
                } == true
                val episodioAnteriorLabel = episodioAnterior?.let { anterior ->
                    val temporadaNumero = SerieProgressoUtils.numeroTemporada(activeTemporada.numero) ?: 1
                    "T${temporadaNumero.toString().padStart(2, '0')}E${anterior.numero.toString().padStart(2, '0')}"
                }.orEmpty()

                TelaEpisodio(
                    nomeSerie = activeSerie.titulo,
                    temporadaLabel = activeTemporada.numero,
                    numeroEpisodio = activeEpisodio.numero,
                    tituloEpisodio = activeEpisodio.titulo,
                    dataExibicao = activeEpisodio.dataExibicao,
                    statusEpisodioLabel = statusLabel,
                    minutagens = activeEpisodio.minutagens,
                    isAnteriorEnabled = hasAnterior,
                    isProximoEnabled = hasProximo,
                    isCopyEnabled = podeCopiarAnterior,
                    copySourceLabel = episodioAnteriorLabel,
                    onBackClick = { selectedEpisodioId = null },
                    onCopyClick = { tipo ->
                        episodioAnterior?.let { anterior ->
                            copiarMinutagensDoEpisodioAnterior(
                                serieId = activeSerie.id,
                                temporadaId = activeTemporada.id,
                                episodioId = activeEpisodio.id,
                                episodioAnterior = anterior,
                                tipo = tipo,
                                sourceLabel = episodioAnteriorLabel
                            )
                        }
                    },
                    onAnteriorClick = {
                        if (hasAnterior) {
                            selectedEpisodioId = episodiosOrdenados[currentIndex - 1].id
                        }
                    },
                    onProximoClick = {
                        if (hasProximo) {
                            val seguintes = episodiosOrdenados.drop(currentIndex + 1)
                            val pendente = seguintes.firstOrNull { ep ->
                                ep.minutagens.count { it.estado != EstadoMinutagem.PENDENTE } < 3
                            }
                            selectedEpisodioId = (pendente ?: seguintes.first()).id
                        }
                    },
                    onSecaoClick = { secao ->
                        selectedSecaoTipo = secao.tipo
                    },
                    secaoEditando = selectedSecaoTipo?.let { tipo ->
                        activeEpisodio.minutagens.firstOrNull { it.tipo == tipo } ?: MinutagemSecao(tipo = tipo)
                    },
                    duracaoFixaEditando = when (selectedSecaoTipo) {
                        TipoMinutagem.ABERTURA -> if (activeSerie.aberturaAtiva) activeSerie.duracaoFixaAbertura else ""
                        TipoMinutagem.RECAP -> if (activeSerie.recapAtivo) activeSerie.duracaoFixaRecap else ""
                        TipoMinutagem.ENCERRAMENTO -> if (activeSerie.encerramentoAtivo) activeSerie.duracaoFixaEncerramento else ""
                        else -> ""
                    },
                    onSaveSecao = { novaSecao ->
                        salvarMinutagem(
                            serieId = activeSerie.id,
                            temporadaId = activeTemporada.id,
                            episodioId = activeEpisodio.id,
                            novaSecao = novaSecao
                        )
                        selectedSecaoTipo = null
                    },
                    onDismissEdit = { selectedSecaoTipo = null },
                    minutagensAnterior = episodioAnterior?.minutagens.orEmpty(),
                    rotuloAnterior = episodioAnterior?.let { "E" + it.numero.toString().padStart(2, '0') }.orEmpty(),
                    rotuloProximo = episodiosOrdenados.getOrNull(currentIndex + 1)
                        ?.let { "E" + it.numero.toString().padStart(2, '0') }.orEmpty(),
                    duracaoEpisodioMin = activeEpisodio.duracaoMin,
                    introDbResumo = temposIntroDb?.takeIf { it.first == activeEpisodio.id }?.second
                        ?.takeIf { com.kalidorabr.bancodeminutagens.model.ImportarIntroDb.ajuda(activeEpisodio.minutagens, it) }
                        ?.let { t ->
                            listOf(TipoMinutagem.ABERTURA to "Abertura", TipoMinutagem.RECAP to "Recap", TipoMinutagem.ENCERRAMENTO to "Encerramento")
                                .filter { (tipo, _) -> activeEpisodio.minutagens.firstOrNull { it.tipo == tipo }?.estado != EstadoMinutagem.PREENCHIDO && activeEpisodio.minutagens.firstOrNull { it.tipo == tipo }?.estado != EstadoMinutagem.NAO_POSSUI }
                                .mapNotNull { (tipo, nome) ->
                                    t.secoes[tipo]?.let { (a, b) -> "$nome\t${MinutagemSecao.formatSecondsToMmSs(a)} → ${MinutagemSecao.formatSecondsToMmSs(b)}" }
                                        ?: if (tipo in t.naoPossui) "$nome\tNão possui" else null
                                }
                                .joinToString("\n")
                        },
                    onUsarIntroDb = {
                        temposIntroDb?.takeIf { it.first == activeEpisodio.id }?.second?.let { t ->
                            copiarMinutagensDoEpisodioAnterior(
                                serieId = activeSerie.id,
                                temporadaId = activeTemporada.id,
                                episodioId = activeEpisodio.id,
                                episodioAnterior = activeEpisodio.copy(minutagens = com.kalidorabr.bancodeminutagens.model.ImportarIntroDb.aplicar(activeEpisodio.minutagens, t)),
                                tipo = null,
                                sourceLabel = "Tempos do IntroDB"
                            )
                        }
                    },
                    onConcluirClick = {
                        val completo = activeEpisodio.minutagens.count {
                            it.estado == EstadoMinutagem.PREENCHIDO || it.estado == EstadoMinutagem.NAO_POSSUI
                        } == 3
                        // Trechos que já foram ao IntroDB e mudaram de tempo: pergunta se reenvia.
                        // Trechos já enviados que foram apagados: o IntroDB não apaga, então só avisa.
                        val numeroTemp = SerieProgressoUtils.numeroTemporada(activeTemporada.numero)
                        val trocados = if (chaveIntroDb.isBlank() || numeroTemp == null) emptyList() else
                            com.kalidorabr.bancodeminutagens.model.IntroDbEnvio.planejar(numeroTemp, listOf(activeEpisodio)).envios
                                .filter { e -> introDbPrefs.enviadoAntes(e.episodioId, e.segmentType).let { it != null && it != "${e.inicioSeg}-${e.fimSeg}" } }
                        val apagados = if (numeroTemp == null) emptyList() else activeEpisodio.minutagens
                            .filter { it.estado == EstadoMinutagem.PENDENTE }
                            .map { com.kalidorabr.bancodeminutagens.model.EnvioIntroDb(activeEpisodio.id, numeroTemp, activeEpisodio.numero, it.tipo, 0, 0) }
                            .filter { introDbPrefs.enviadoAntes(it.episodioId, it.segmentType) != null }
                        when {
                            trocados.isNotEmpty() -> perguntaReenvio = PerguntaReenvio(activeSerie.id, activeTemporada.id, activeEpisodio.id, trocados.map { it.rotulo }.toSet())
                            autoIntroDb && completo -> enviarAoIntroDb(activeSerie.id, activeTemporada.id, setOf(activeEpisodio.id), comPopup = false)
                        }
                        if (apagados.isNotEmpty()) {
                            val nomes = apagados.joinToString(" e ") { it.rotulo.substringAfter("· ") }
                            avisoIntroDb = Triple("$nomes continua no IntroDB", "O app não apaga lá. Para remover, corrija direto pelo site.", TipoAvisoTemaA.ATENCAO)
                        }
                        // Modo maratona: segue para o próximo episódio; senão volta para a temporada
                        selectedEpisodioId = if (maratona && hasProximo) episodiosOrdenados[currentIndex + 1].id else null
                    },
                    duracaoTipicaEditando = selectedSecaoTipo?.let { tipo ->
                        com.kalidorabr.bancodeminutagens.model.AvisoTempo.duracaoTipica(activeSerie, tipo, activeEpisodio.id)
                    }
                )
            } else if (selectedTemporadaId != null && activeSerie != null && activeTemporada != null) {
                TelaTemporada(
                    nomeSerie = activeSerie.titulo,
                    nomeTemporada = activeTemporada.nome,
                    episodios = activeTemporada.episodios,
                    onBackClick = { selectedTemporadaId = null },
                    onExportJsonClick = {
                        exportarTemporadaJson(activeSerie, activeTemporada)
                    },
                    onBatchFillConfirmed = { request ->
                        aplicarPreenchimentoEmLote(activeSerie.id, activeTemporada.id, request)
                    },
                    onEpisodioClick = { episodio ->
                        selectedEpisodioId = episodio.id
                    },
                    onLimparEpisodiosConfirmed = { episodioIds ->
                        limparEpisodiosSelecionados(
                            serieId = activeSerie.id,
                            temporadaId = activeTemporada.id,
                            episodioIds = episodioIds
                        )
                    },
                    temChaveIntroDb = chaveIntroDb.isNotBlank(),
                    noIntroDb = remember(marcaIntroDb, activeTemporada.episodios) {
                        activeTemporada.episodios.filter { introDbPrefs.noSite(it.id).isNotEmpty() }.map { it.id }.toSet()
                    },
                    conferirAoAbrir = {
                        conferirNoIntroDb(activeSerie.id, soNaoConferidos = true, temporadaId = activeTemporada.id)
                    },
                    temporadaAnterior = activeSerie.temporadas.getOrNull(activeSerie.temporadas.indexOf(activeTemporada) - 1)?.numero,
                    temporadaProxima = activeSerie.temporadas.getOrNull(activeSerie.temporadas.indexOf(activeTemporada) + 1)?.numero,
                    onTrocarTemporada = { passo ->
                        activeSerie.temporadas.getOrNull(activeSerie.temporadas.indexOf(activeTemporada) + passo)?.let { selectedTemporadaId = it.id }
                    },
                    onConferirIntroDb = {
                        avisoIntroDb = Triple("Conferindo no IntroDB…", activeSerie.titulo, TipoAvisoTemaA.ATENCAO)
                        val titulo = activeSerie.titulo
                        conferirNoIntroDb(activeSerie.id, soNaoConferidos = false, aoProgresso = { feitos, total ->
                            val pct = if (total == 0) 0 else feitos * 100 / total
                            avisoIntroDb = Triple("Conferindo no IntroDB… $pct%", "$titulo · $feitos de $total episódios", TipoAvisoTemaA.ATENCAO)
                        }, aoSemImdb = {
                            avisoIntroDb = Triple(
                                "Esta série não tem código do IMDb",
                                "O TMDB também não tem. Preencha em IMDb · TVDB na série e tente de novo.",
                                TipoAvisoTemaA.ERRO
                            )
                        }) { n ->
                            avisoIntroDb = Triple(
                                if (n == 0) "Nada desta série no IntroDB ainda" else "$n episódio(s) já têm minutagem no IntroDB",
                                "Eles aparecem com uma nuvem na temporada.", TipoAvisoTemaA.SUCESSO
                            )
                        }
                    },
                    onEnviarIntroDbConfirmed = { episodioIds ->
                        enviarAoIntroDb(activeSerie.id, activeTemporada.id, episodioIds)
                    }
                )
            } else if (selectedSerieId != null && activeSerie != null) {
                TelaSerie(
                    serie = activeSerie,
                    onBackClick = {
                        selectedSerieId = null
                    },
                    onMoverParaLixeiraClick = {
                        moverSerieParaLixeira(activeSerie.id)
                    },
                    onTemporadaClick = { temporada ->
                        selectedTemporadaId = temporada.id
                    },
                    onSaveStatus = { novoStatus -> salvarStatusSerie(activeSerie.id, novoStatus) },
                    onSaveIdentificadores = { imdb, tvdb, resumo ->
                        salvarIdentificadoresSerie(activeSerie.id, imdb, tvdb, resumo)
                    },
                    onSaveMinutagemFixa = { ab, rec, enc, durAb, durRec, durEnc ->
                        salvarMinutagemFixa(activeSerie.id, ab, rec, enc, durAb, durRec, durEnc)
                    },
                    onContinueClick = { destino ->
                        val result = SerieProgressoUtils.localizarEpisodio(seriesListState, destino)
                        if (result != null) {
                            selectedTemporadaId = result.second.id
                            selectedEpisodioId = result.third.id
                        }
                    },
                    isSyncingTmdb = isSyncingTmdbTemaA,
                    onSyncTmdbClick = {
                        isSyncingTmdbTemaA = true
                        coroutineScope.launch {
                            try {
                                val atualizada = TmdbService.enriquecerSerieComTmdb(activeSerie)
                                if (atualizada.imdbId != activeSerie.imdbId) {
                                    introDbPrefs.esquecerConferencias(activeSerie.temporadas.flatMap { t -> t.episodios.map { it.id } })
                                    marcaIntroDb++
                                }
                                atualizarESalvarSeries(seriesListState.map { if (it.id == atualizada.id) atualizada else it })
                            } catch (_: Exception) {
                                currentFeedback = EpisodeFeedback.Error("Não foi possível sincronizar com o TMDB.")
                            } finally {
                                isSyncingTmdbTemaA = false
                            }
                        }
                    }
                )
            
            } else {
                // Lembrete de backup: conta a partir do último backup em arquivo ou do último envio ao Sheets
                var adiadoAte by remember { mutableStateOf(novidadesPrefs.lembreteBackupAdiadoAte()) }
                val diasSemBackup = remember(marcaBackup, adiadoAte, seriesListState.isNotEmpty()) {
                    val sync = prefs.getConfig().takeIf { it.lastSyncStatus == "Sucesso" }?.lastSyncDate?.let { data ->
                        runCatching { SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault()).parse(data)?.time }.getOrNull()
                    } ?: 0L
                    val referencia = maxOf(marcaBackup, sync).takeIf { it > 0L } ?: novidadesPrefs.inicioContagem()
                    com.kalidorabr.bancodeminutagens.model.Novidades.diasParaLembrete(
                        System.currentTimeMillis(), referencia, adiadoAte, seriesListState.any { !it.isDeleted }
                    )
                }

                HomeScreen(
                    seriesList = seriesListState,
                    abaInicial = if (acaoInicial == "com.kalidorabr.bancodeminutagens.BUSCAR") 1 else 0,
                    episodiosNovos = episodiosNovos,
                    versaoNova = versaoNova?.versao,
                    onAtualizar = { iniciarAtualizacao() },
                    // "Conferir agora" da tela Sincronização: todas as séries, sem limite de episódios
                    onConferirIntroDbAgora = { conferirTodasNoIntroDb(limite = Int.MAX_VALUE, forcar = true) },
                    onAtualizarDepois = { versaoNova = null },
                    onProcurarAtualizacao = {
                        com.kalidorabr.bancodeminutagens.data.Atualizacao.procurar(com.kalidorabr.bancodeminutagens.BuildConfig.VERSION_NAME)
                            .onSuccess { if (it != null) versaoNova = it }
                            .map { it?.versao }
                    },
                    onAdicionarEpisodios = { novidade ->
                        val novas = seriesListState.map { s ->
                            if (s.id == novidade.serieId) {
                                com.kalidorabr.bancodeminutagens.model.Novidades.adicionarEpisodios(s, novidade.temporadas)
                            } else s
                        }
                        atualizarESalvarSeries(novas)
                        episodiosNovos = episodiosNovos.filterNot { it.serieId == novidade.serieId }
                        Toast.makeText(context, "${novidade.quantidade} episódio(s) adicionado(s) em ${novidade.titulo}", Toast.LENGTH_SHORT).show()
                    },
                    onDispensarEpisodios = {
                        episodiosNovos.forEach { novidadesPrefs.dispensarEpisodios(it.serieId, it.totalTmdb) }
                        episodiosNovos = emptyList()
                    },
                    diasSemBackup = diasSemBackup,
                    backupNuncaFeito = marcaBackup == 0L,
                    onAdiarBackup = {
                        val ate = System.currentTimeMillis() + 3 * com.kalidorabr.bancodeminutagens.model.Novidades.DIA_MS
                        novidadesPrefs.adiarLembreteBackup(ate)
                        adiadoAte = ate
                    },
                    chaveIntroDb = chaveIntroDb,
                    onSalvarChaveIntroDb = { nova ->
                        introDbPrefs.salvarChave(nova)
                        chaveIntroDb = introDbPrefs.chave()
                    },
                    backupAutoPasta = backupAutoPasta,
                    onBackupAutoChange = { ligado ->
                        if (ligado) escolherPastaBackup.launch(null)
                        else {
                            com.kalidorabr.bancodeminutagens.data.BackupAutomatico.desligar(context)
                            backupAutoPasta = null
                        }
                    },
                    avisoEstreia = avisoEstreia,
                    onAvisoEstreiaChange = { ligado ->
                        if (ligado && !com.kalidorabr.bancodeminutagens.data.EstreiaAgenda.podeNotificar(context)) {
                            permissaoNotificacao.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else aplicarAvisoEstreia(ligado)
                    },
                    maratona = maratona,
                    onMaratonaChange = { ligado ->
                        novidadesPrefs.salvarMaratona(ligado)
                        maratona = ligado
                    },
                    autoIntroDb = autoIntroDb,
                    onAutoIntroDbChange = { ligado ->
                        introDbPrefs.salvarAutomatico(ligado)
                        autoIntroDb = ligado
                    },
                    calcularEstatisticas = {
                        com.kalidorabr.bancodeminutagens.model.Estatisticas.calcular(
                            seriesListState, novidadesPrefs.conclusoes(), java.time.LocalDate.now().toEpochDay()
                        )
                    },
                    selectedFilter = selectedHomeFilter,
                    onFilterSelected = { selectedHomeFilter = it },
                    selectedTheme = selectedTheme,
                    onThemeSelected = onThemeSelected,
                    visualMode = visualMode,
                    onVisualModeSelected = onVisualModeSelected,
                    onExportBackup = {
                        pendingBackupExport = repository.criarBackup(seriesListState, novidadesPrefs.conclusoes())
                        backupExportLauncher.launch("banco-de-minutagens-backup.json")
                    },
                    onImportBackup = { backupImportLauncher.launch(arrayOf("application/json")) },
                    onOpenIssue = { issue ->
                        selectedSerieId = issue.serieId
                        selectedTemporadaId = issue.temporadaId
                        selectedEpisodioId = issue.episodioId
                    },
                    onOpenSerieDetail = { serie ->
                        selectedSerieId = serie.id
                    },
                    onContinueClick = { destino ->
                        val result = SerieProgressoUtils.localizarEpisodio(seriesListState, destino)
                        if (result != null) {
                            selectedSerieId = result.first.id
                            selectedTemporadaId = result.second.id
                            selectedEpisodioId = result.third.id
                        } else {
                            currentFeedback = EpisodeFeedback.Error("Episódio não encontrado.")
                        }
                    },
                    onAdicionarSerie = { tmdbResultado ->
                        try {
                            val serieCompleta = TmdbService
                                .buscarDetalhesSerieCompleta(tmdbResultado.id)
                                .copy(status = "Em breve")
                            if (seriesListState.none { it.id == serieCompleta.id }) {
                                val novasSeries = seriesListState + serieCompleta
                                atualizarESalvarSeries(novasSeries)
                                conferirNoIntroDb(serieCompleta.id)
                            }
                            Result.success(Unit)
                        } catch (e: Exception) {
                            Result.failure(e)
                        }
                    },
                    onImportSuccess = { novasSeries ->
                        episodiosNovos = emptyList()
                        seriesListState = novasSeries
                        coroutineScope.launch {
                            repository.salvarSeries(novasSeries)

                            var listAposTmdb = novasSeries
                            for (serie in novasSeries) {
                                if (serie.posterUrl.isBlank() || serie.sinopse.startsWith("Série importada")) {
                                    try {
                                        val tmdbDetails = TmdbService.buscarDetalhesSerieCompleta(serie.id)
                                        listAposTmdb = listAposTmdb.map { s ->
                                            if (s.id == serie.id) {
                                                s.copy(
                                                    posterUrl = tmdbDetails.posterUrl.ifBlank { s.posterUrl },
                                                    imageUrl = tmdbDetails.imageUrl.ifBlank { s.imageUrl },
                                                    sinopse = tmdbDetails.sinopse.ifBlank { s.sinopse },
                                                    generos = if (s.generos == "Importada") tmdbDetails.generos else s.generos,
                                                    ano = if (s.ano == 0) tmdbDetails.ano else s.ano
                                                )
                                            } else {
                                                s
                                            }
                                        }
                                    } catch (_: Exception) {}
                                }
                            }
                            if (listAposTmdb != novasSeries) {
                                seriesListState = listAposTmdb
                                repository.salvarSeries(listAposTmdb)
                            }
                        }
                    },
                    onRestaurarSerie = { serie ->
                        restaurarSerieDaLixeira(serie)
                    },
                    onComecarSerie = { serie ->
                        salvarStatusSerie(serie.id, "Em andamento")
                    },
                    onExcluirDefinitivamente = { serie ->
                        excluirSerieDefinitivamente(serie)
                    }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val destinoAtual = NavDestinoTemaA(selectedSerieId, selectedTemporadaId, selectedEpisodioId)
            SharedTransitionLayout {
                CompositionLocalProvider(LocalSharedTemaA provides this) {
                    AnimatedContent(
                        targetState = destinoAtual,
                        transitionSpec = {
                            when (visualMode) {
                                VisualMode.TEMA_B -> transicaoNavTemaB(initialState, targetState)
                                VisualMode.TEMA_C -> transicaoNavTemaC(initialState, targetState)
                                VisualMode.TEMA_D -> transicaoNavTemaD(initialState, targetState)
                                else -> transicaoNavTemaA(initialState, targetState)
                            }
                        },
                        label = "navegacaoTemaA"
                    ) { alvo ->
                        CompositionLocalProvider(LocalAnimScopeTemaA provides this) {
                            telaNavegacao(alvo)
                        }
                    }
                }
            }

            // Indicador Discreto de Desfazer ou Auto-Sincronização na parte inferior centralizada
            val hasUndoBanner = pendingUndoBatchState != null ||
                pendingUndoCopyState != null || pendingUndoClearState != null || pendingUndoSaveState != null

            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val fb = currentFeedback
                if (fb !is EpisodeFeedback.None) {
                    val (titulo, sub, tipo) = when (fb) {
                        is EpisodeFeedback.EpisodeSaveSuccess -> Triple("Episódio salvo", "", TipoAvisoTemaA.SUCESSO)
                        is EpisodeFeedback.EpisodeSavedAndSynced -> Triple("Salvo e sincronizado", "", TipoAvisoTemaA.SUCESSO)
                        is EpisodeFeedback.EpisodesClearedSuccess -> Triple(if (fb.count == 1) "Episódio limpo" else "${fb.count} episódios limpos", "", TipoAvisoTemaA.SUCESSO)
                        is EpisodeFeedback.EpisodesClearedAndSynced -> Triple(if (fb.count == 1) "Limpo e sincronizado" else "${fb.count} episódios limpos e sincronizados", "", TipoAvisoTemaA.SUCESSO)
                        is EpisodeFeedback.EpisodeSavePending -> Triple("Salvo no aparelho", "Sincroniza depois", TipoAvisoTemaA.ATENCAO)
                        is EpisodeFeedback.EpisodesClearPending -> Triple("Limpeza salva no aparelho", "Sincroniza depois", TipoAvisoTemaA.ATENCAO)
                        is EpisodeFeedback.SyncUnconfirmed -> Triple("Sincronização não confirmada", fb.message, TipoAvisoTemaA.ATENCAO)
                        is EpisodeFeedback.Error -> Triple(fb.message, "", TipoAvisoTemaA.ERRO)
                        else -> Triple("", "", TipoAvisoTemaA.SUCESSO)
                    }
                    TelaAviso(
                        titulo = titulo,
                        subtitulo = sub,
                        tipo = tipo,
                        acao = if (tipo == TipoAvisoTemaA.ERRO) "OK" else null,
                        onAcao = { currentFeedback = EpisodeFeedback.None }
                    )
                }
                baixandoAtualizacao?.let { pct ->
                    TelaAviso(titulo = "Baixando atualização… $pct%", subtitulo = "Versão ${versaoNova?.versao.orEmpty()}", tipo = TipoAvisoTemaA.ATENCAO)
                }
                if (baixandoAtualizacao == null) avisoAtualizacao?.let { texto ->
                    TelaAviso(titulo = "Atualização", subtitulo = texto, tipo = TipoAvisoTemaA.ATENCAO, acao = "OK", onAcao = { avisoAtualizacao = null })
                }
                perguntaReenvio?.let { p ->
                    com.kalidorabr.bancodeminutagens.ui.temas.TelaPergunta(
                        titulo = "Enviar de novo ao IntroDB?",
                        texto = "${p.rotulos.joinToString(", ") { it.substringAfter("· ") }} já foi enviado com outro tempo. Quer mandar o tempo corrigido?",
                        nao = "Não",
                        sim = "Enviar",
                        onNao = { perguntaReenvio = null },
                        onSim = {
                            perguntaReenvio = null
                            enviarAoIntroDb(p.serieId, p.temporadaId, setOf(p.episodioId), comPopup = false, reenviar = p.rotulos)
                        }
                    )
                }
                avisoIntroDb?.let { (titulo, sub, tipo) ->
                    TelaAviso(
                        titulo = titulo,
                        subtitulo = sub,
                        tipo = tipo,
                        acao = if (enviandoIntroDb) null else "OK",
                        onAcao = { avisoIntroDb = null }
                    )
                }
                when {
                    pendingUndoSaveState != null -> TelaAviso(
                        pendingUndoSaveState?.sourceLabel.orEmpty(), TipoAvisoTemaA.DESFAZER,
                        acao = "Desfazer", onAcao = { desfazerSalvamento() }
                    )
                    pendingUndoBatchState != null -> TelaAviso(
                        "${pendingUndoBatchState?.affectedCount ?: 0} episódios atualizados", TipoAvisoTemaA.DESFAZER,
                        acao = "Desfazer", onAcao = { desfazerPreenchimentoEmLote() }
                    )
                    pendingUndoCopyState != null -> TelaAviso(
                        pendingUndoCopyState?.sourceLabel.orEmpty().let { if (it.startsWith("Tempos")) "$it copiados" else "$it copiado" }, TipoAvisoTemaA.DESFAZER,
                        acao = "Desfazer", onAcao = { desfazerCopia() }
                    )
                    pendingUndoClearState != null -> TelaAviso(
                        "Episódios limpos", TipoAvisoTemaA.DESFAZER,
                        acao = "Desfazer", onAcao = { desfazerLimpeza() }
                    )
                    pendingUndoLixeiraState != null -> TelaAviso(
                        "Série movida para a lixeira", TipoAvisoTemaA.DESFAZER,
                        subtitulo = pendingUndoLixeiraState?.second.orEmpty(),
                        acao = "Desfazer", onAcao = { desfazerLixeira() }
                    )
                }
            }


            com.kalidorabr.bancodeminutagens.ui.temas.DialogoIntroDb(progressoIntroDb, onFechar = { progressoIntroDb = null })

            com.kalidorabr.bancodeminutagens.ui.temas.DialogoSerieFinalizada(
                serie = serieComemorada,
                onVerSerie = {
                    val id = serieComemorada?.id
                    serieComemorada = null
                    selectedSecaoTipo = null
                    selectedEpisodioId = null
                    selectedTemporadaId = null
                    selectedSerieId = id
                },
                onFechar = { serieComemorada = null }
            )
        }
    }
}

@Composable
fun UndoDiscreteBanner(
    modifier: Modifier = Modifier,
    message: String = "Episódios limpos",
    onUndoClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .background(Color(0xFF222222), RoundedCornerShape(20.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Desfazer",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onUndoClick)
            )
        }
    }
}

@Composable
fun AutoSyncDiscreteIndicator(
    feedback: EpisodeFeedback,
    modifier: Modifier = Modifier
) {
    if (feedback is EpisodeFeedback.None) return

    val text = when (feedback) {
        is EpisodeFeedback.EpisodeSaveSuccess -> "Episódio salvo com sucesso"
        is EpisodeFeedback.EpisodeSavedAndSynced -> "Salvo e sincronizado na nuvem"
        is EpisodeFeedback.EpisodesClearedSuccess -> if (feedback.count == 1) "Episódio limpo com sucesso" else "${feedback.count} episódios limpos"
        is EpisodeFeedback.EpisodesClearedAndSynced -> if (feedback.count == 1) "Limpo e sincronizado na nuvem" else "${feedback.count} episódios limpos e sincronizados"
        is EpisodeFeedback.EpisodeSavePending -> "Salvo localmente • Sincronização pendente"
        is EpisodeFeedback.EpisodesClearPending -> "Limpeza salva • Sincronização pendente"
        is EpisodeFeedback.SyncUnconfirmed -> feedback.message
        is EpisodeFeedback.Error -> feedback.message
        is EpisodeFeedback.None -> ""
    }

    val isSuccess = feedback is EpisodeFeedback.EpisodeSaveSuccess ||
            feedback is EpisodeFeedback.EpisodeSavedAndSynced ||
            feedback is EpisodeFeedback.EpisodesClearedSuccess ||
            feedback is EpisodeFeedback.EpisodesClearedAndSynced

    val isPending = feedback is EpisodeFeedback.EpisodeSavePending ||
            feedback is EpisodeFeedback.EpisodesClearPending ||
            feedback is EpisodeFeedback.SyncUnconfirmed

    val isError = feedback is EpisodeFeedback.Error

    val bgColor = when {
        isSuccess -> Color(0xFF0D2818).copy(alpha = 0.95f)
        isPending -> Color(0xFF2B220B).copy(alpha = 0.95f)
        isError -> Color(0xFF381014).copy(alpha = 0.95f)
        else -> SurfaceDark
    }

    val borderColor = when {
        isSuccess -> Color(0xFF22C55E)
        isPending -> Color(0xFFEAB308)
        isError -> Color(0xFFE92035)
        else -> BorderColor
    }

    val textColor = when {
        isSuccess -> Color(0xFF22C55E)
        isPending -> Color(0xFFEAB308)
        isError -> Color(0xFFFF4D4D)
        else -> Color.White
    }

    Box(
        modifier = modifier
            .background(bgColor, CircleShape)
            .border(1.dp, borderColor.copy(alpha = 0.8f), CircleShape)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val iconVector = when {
                isSuccess -> Icons.Outlined.CheckCircle
                isPending -> Icons.Outlined.Schedule
                else -> Icons.Outlined.ErrorOutline
            }
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HomeScreen(
    seriesList: List<SerieItem>,
    selectedFilter: String = "Em andamento",
    onFilterSelected: (String) -> Unit = {},
    selectedTheme: AppThemeOpcao = AppThemeOpcao.VERMELHO,
    onThemeSelected: (AppThemeOpcao) -> Unit = {},
    visualMode: VisualMode = VisualMode.TEMA_A,
    onVisualModeSelected: (VisualMode) -> Unit = {},
    onOpenSerieDetail: (SerieItem) -> Unit = {},
    onContinueClick: (DestinoEpisodio) -> Unit = {},
    onAdicionarSerie: suspend (TmdbSerieResultado) -> Result<Unit> = { Result.success(Unit) },
    onGoogleSheetsClick: () -> Unit = {},
    onExportBackup: () -> Unit = {},
    onImportBackup: () -> Unit = {},
    onOpenIssue: (com.kalidorabr.bancodeminutagens.model.MinutagemInconsistente) -> Unit = {},
    onImportSuccess: (List<SerieItem>) -> Unit = {},
    onRestaurarSerie: (SerieItem) -> Unit = {},
    onExcluirDefinitivamente: (SerieItem) -> Unit = {},
    onComecarSerie: (SerieItem) -> Unit = {},
    abaInicial: Int = 0,
    versaoNova: String? = null,
    onProcurarAtualizacao: suspend () -> Result<String?> = { Result.success(null) },
    onConferirIntroDbAgora: () -> Unit = {},
    onAtualizar: () -> Unit = {},
    onAtualizarDepois: () -> Unit = {},
    episodiosNovos: List<com.kalidorabr.bancodeminutagens.model.EpisodiosNovosSerie> = emptyList(),
    onAdicionarEpisodios: (com.kalidorabr.bancodeminutagens.model.EpisodiosNovosSerie) -> Unit = {},
    onDispensarEpisodios: () -> Unit = {},
    diasSemBackup: Int? = null,
    backupNuncaFeito: Boolean = false,
    onAdiarBackup: () -> Unit = {},
    calcularEstatisticas: () -> com.kalidorabr.bancodeminutagens.model.Estatisticas? = { null },
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
    val context = LocalContext.current
    val prefs = remember(context) { GoogleSheetsPreferences(context) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(abaInicial) }
    var showGoogleSheets by remember { mutableStateOf(false) }
    var showLixeira by remember { mutableStateOf(false) }
    var showSobre by remember { mutableStateOf(false) }
    var showDadosBackup by remember { mutableStateOf(false) }
    var showEstatisticas by remember { mutableStateOf(false) }

    val voltarParaHome = {
        if (showEstatisticas) {
            showEstatisticas = false
        } else if (showGoogleSheets) {
            showGoogleSheets = false
        } else if (showLixeira) {
            showLixeira = false
        } else if (showSobre) {
            showSobre = false
        } else if (showDadosBackup) {
            showDadosBackup = false
        } else {
            selectedTab = 0
        }
    }
    // Voltar na Início (temas novos): pergunta se quer sair, num pop-up central.
    // Fica antes dos outros BackHandler, então só age quando nada mais está aberto.
    var confirmarSaida by remember { mutableStateOf(false) }
    val atividade = LocalContext.current as? android.app.Activity
    BackHandler(enabled = selectedTab == 0) { confirmarSaida = true }
    com.kalidorabr.bancodeminutagens.ui.temas.TelaConfirmarSaida(
        visivel = confirmarSaida,
        onFicar = { confirmarSaida = false },
        onSair = {
            confirmarSaida = false
            atividade?.finish()
        }
    )
    BackHandler(enabled = selectedTab != 0 || showGoogleSheets || showLixeira || showSobre || showDadosBackup || showEstatisticas, onBack = voltarParaHome)

    val seriesAtivas = remember(seriesList) {
        seriesList.filter { !it.isDeleted }
            .sortedWith(
                Comparator { s1, s2 ->
                    val t1 = s1.lastUpdatedAt ?: 0L
                    val t2 = s2.lastUpdatedAt ?: 0L
                    if (t1 != t2) {
                        t2.compareTo(t1)
                    } else {
                        s1.titulo.compareTo(s2.titulo, ignoreCase = true)
                    }
                }
            )
    }
    val seriesLixeira = remember(seriesList) {
        seriesList.filter { it.isDeleted }
    }

    if (selectedTab == 1) {
        val seriesSalvasIds = remember(seriesAtivas) {
            seriesAtivas.map { it.id }.toSet()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when (visualMode) {
                        else -> Paletas.de(visualMode).fundo
                    }
                )
                .topoEscuroB()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            TelaBuscar(
                modifier = Modifier
                    .weight(1f)
                    .transicaoTemaA(chave = selectedTab, profundidade = 0, animarEntrada = true)
                    .faixaAcimaB(),
                seriesSalvasIds = seriesSalvasIds,
                onAdicionarSerie = onAdicionarSerie
            )
        }
        return
    }

    // Tema B: a barra de abas some ao descer a lista e volta ao subir
    var barraAbasVisivel by remember { mutableStateOf(true) }
    val rolagemBarraB = remember(visualMode) {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                if (visualMode == VisualMode.TEMA_B) {
                    if (available.y < -6f) barraAbasVisivel = false
                    else if (available.y > 6f) barraAbasVisivel = true
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }
    LaunchedEffect(selectedTab, visualMode) { barraAbasVisivel = true }

    val filteredSeries = remember(selectedFilter, searchQuery, seriesAtivas, selectedTab) {
        val effectiveFilter = if (selectedTab == 2) "Em breve" else selectedFilter
        seriesAtivas.filter { serie ->
            val matchesFilter = when {
                effectiveFilter.equals("Todas", ignoreCase = true) -> true
                effectiveFilter.equals("Concluídas", ignoreCase = true) || effectiveFilter.equals("Finalizadas", ignoreCase = true) -> serie.isFinalizada || serie.isIncompleta
                effectiveFilter.equals("Em andamento", ignoreCase = true) -> !serie.isFinalizada && serie.status.equals("Em andamento", ignoreCase = true)
                else -> serie.status.equals(effectiveFilter, ignoreCase = true)
            }
            val matchesSearch = searchQuery.isBlank() ||
                    serie.titulo.contains(searchQuery, ignoreCase = true) ||
                    serie.generos.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    val continuarInicio = remember(seriesAtivas) { encontrarContinuarTemaA(seriesAtivas) }
    val cabecalhoInicio: @Composable () -> Unit = {
        val cabecalho: @Composable () -> Unit = {
            TelaInicioCabecalho(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onRefreshClick = {},
                onSearchClick = { selectedTab = 1 },
                subtitulo = when {
                    // no Tema C a imagem grande já diz "Continuar de onde parou"
                    visualMode == VisualMode.TEMA_C && continuarInicio != null && searchQuery.isBlank() -> ""
                    continuarInicio != null -> "Continuar de onde parou"
                    else -> "Suas séries"
                }
            )
        }
        val continuar = if (searchQuery.isBlank()) continuarInicio else null
        if (visualMode == VisualMode.TEMA_C && continuar != null) {
            // Tema C: a imagem grande começa atrás do relógio e o título fica por cima dela
            Box {
                TelaContinuar(serie = continuar.first, destino = continuar.second, onOpenSerieClick = { onOpenSerieDetail(continuar.first) }, onContinueClick = onContinueClick)
                Box(Modifier.statusBarsPadding()) { cabecalho() }
            }
        } else {
            if (visualMode == VisualMode.TEMA_C) Spacer(Modifier.statusBarsPadding())
            cabecalho()
            continuar?.let { (serie, destino) ->
                TelaContinuar(serie = serie, destino = destino, onOpenSerieClick = { onOpenSerieDetail(serie) }, onContinueClick = onContinueClick)
            }
        }
        if (searchQuery.isBlank() && versaoNova != null) {
            com.kalidorabr.bancodeminutagens.ui.temas.CartaoAtualizacao(versaoNova, onAtualizar, onAtualizarDepois)
        }
        if (searchQuery.isBlank() && episodiosNovos.isNotEmpty()) {
            com.kalidorabr.bancodeminutagens.ui.temas.CartaoEpisodiosNovos(
                novidades = episodiosNovos,
                onAdicionar = onAdicionarEpisodios,
                onDispensar = onDispensarEpisodios
            )
        }
        if (searchQuery.isBlank() && diasSemBackup != null) {
            com.kalidorabr.bancodeminutagens.ui.temas.FaixaLembreteBackup(
                dias = diasSemBackup,
                nuncaFeito = backupNuncaFeito,
                onFazer = onExportBackup,
                onAdiar = onAdiarBackup
            )
        }
        TelaFiltros(
            filters = listOf("Todas", "Em andamento", "Finalizadas"),
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected,
            totalCount = seriesAtivas.size
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                when (visualMode) {
                    else -> Paletas.de(visualMode).fundo
                }
            )
            .then(if (visualMode == VisualMode.TEMA_D) Modifier.ambienteC() else Modifier)
            .topoEscuroB()
            .then(if (visualMode == VisualMode.TEMA_C && selectedTab == 0) Modifier else Modifier.statusBarsPadding())
            .navigationBarsPadding()
            .nestedScroll(rolagemBarraB)
    ) {
        if (selectedTab == 3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .faixaAcimaB()
                    .transicaoTemaA(
                        chave = listOf(showGoogleSheets, showLixeira, showSobre, showDadosBackup, showEstatisticas),
                        profundidade = if (showGoogleSheets || showLixeira || showSobre || showDadosBackup || showEstatisticas) 1 else 0,
                        ativo = true,
                        animarEntrada = true
                    )
            ) {
                val estatisticas = if (showEstatisticas) calcularEstatisticas() else null
                if (estatisticas != null) {
                    com.kalidorabr.bancodeminutagens.ui.temas.TelaEstatisticas(estatisticas, onBackClick = { showEstatisticas = false })
                } else if (showGoogleSheets) {
                    TelaGoogleSheets(
                        seriesLocaisAtuais = seriesAtivas,
                        onImportSuccess = onImportSuccess,
                        onBackClick = { showGoogleSheets = false },
                        chaveIntroDb = chaveIntroDb,
                        onSalvarChaveIntroDb = onSalvarChaveIntroDb,
                        autoIntroDb = autoIntroDb,
                        onAutoIntroDbChange = onAutoIntroDbChange,
                        onConferirIntroDbAgora = onConferirIntroDbAgora
                    )
                } else if (showLixeira) {
                    var idsBloqueadosTemaA by remember { mutableStateOf(prefs.getPermanentlyDeletedTmdbIds()) }
                    TelaLixeira(
                        seriesLixeira = seriesLixeira,
                        permanentlyDeletedIds = idsBloqueadosTemaA,
                        onBackClick = { showLixeira = false },
                        onRestaurarSerie = onRestaurarSerie,
                        onExcluirDefinitivamente = { serie ->
                            onExcluirDefinitivamente(serie)
                            idsBloqueadosTemaA = prefs.getPermanentlyDeletedTmdbIds()
                        },
                        onPermitirImportacao = { tmdbId ->
                            prefs.removePermanentlyDeletedTmdbId(tmdbId)
                            idsBloqueadosTemaA = prefs.getPermanentlyDeletedTmdbIds()
                        }
                    )
                } else if (showSobre) {
                    TelaSobre(onBackClick = { showSobre = false }, versaoNova = versaoNova, onProcurarAtualizacao = onProcurarAtualizacao, onAtualizar = onAtualizar)
                } else if (showDadosBackup) {
                    val syncConfigTemaA = prefs.getConfig()
                    TelaDadosBackup(
                        series = seriesList,
                        isOnline = isNetworkAvailable(context),
                        hasPendingSync = syncConfigTemaA.hasPendingExport,
                        lastSyncDate = syncConfigTemaA.lastSyncDate,
                        isSyncConfigured = syncConfigTemaA.isConnectionValidated && syncConfigTemaA.scriptUrl.isNotBlank() && syncConfigTemaA.spreadsheetId.isNotBlank(),
                        isAutoSyncEnabled = syncConfigTemaA.isAutoSyncUnlocked && syncConfigTemaA.isAutoSyncEnabled,
                        lastSyncStatus = syncConfigTemaA.lastSyncStatus,
                        onBackClick = { showDadosBackup = false },
                        onExportBackup = onExportBackup,
                        onImportBackup = onImportBackup,
                        onOpenIssue = onOpenIssue,
                        onSyncClick = {
                            showDadosBackup = false
                            showGoogleSheets = true
                        }
                    )
                } else {
                    TelaAjustes(
                        selectedTheme = selectedTheme,
                        onThemeSelected = onThemeSelected,
                        visualMode = visualMode,
                        onVisualModeSelected = onVisualModeSelected,
                        onGoogleSheetsClick = {
                            showGoogleSheets = true
                            onGoogleSheetsClick()
                        },
                        onLixeiraClick = {
                            showLixeira = true
                        },
                        onDadosBackupClick = { showDadosBackup = true },
                        onSobreClick = {
                            showSobre = true
                        },
                        onEstatisticasClick = { showEstatisticas = true },
                        chaveIntroDb = chaveIntroDb,
                        onSalvarChaveIntroDb = onSalvarChaveIntroDb,
                        autoIntroDb = autoIntroDb,
                        onAutoIntroDbChange = onAutoIntroDbChange,
                        maratona = maratona,
                        onMaratonaChange = onMaratonaChange,
                        avisoEstreia = avisoEstreia,
                        onAvisoEstreiaChange = onAvisoEstreiaChange,
                        backupAutoPasta = backupAutoPasta,
                        onBackupAutoChange = onBackupAutoChange
                    )
                }
            }
        } else {
            if (selectedTab == 2) {
                Box(Modifier.transicaoTemaA(chave = selectedTab, profundidade = 0, animarEntrada = true).faixaAcimaB()) {
                    TelaEmBreveCabecalho(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it }
                    )
                }
            } else {
                when (visualMode) {
                    else -> {
                        if (filteredSeries.isEmpty()) Box(Modifier.faixaAcimaB()) { Column { cabecalhoInicio() } }
                    }
                }
            }

            // sem espaço quando o cabeçalho da Início está dentro da lista (senão aparece uma linha clara no topo)
            if (!(selectedTab == 0 && filteredSeries.isNotEmpty())) Spacer(modifier = Modifier.height(4.dp))

            if (selectedTab == 2) {
                TelaEmBreveLista(
                    series = filteredSeries,
                    temSeriesNaColecao = seriesAtivas.isNotEmpty(),
                    onOpenSerie = { onOpenSerieDetail(it) },
                    onComecar = { onComecarSerie(it) },
                    onBuscarClick = { selectedTab = 1 },
                    modifier = Modifier
                        .weight(1f)
                        .transicaoTemaA(chave = selectedTab, profundidade = 0, animarEntrada = true)
                )
            } else if (filteredSeries.isEmpty()) {
                if (seriesAtivas.isEmpty()) {
                    TelaEstadoVazio(
                        icone = IconeColecaoVaziaTemaA,
                        titulo = "Sua coleção está vazia",
                        texto = "Busque uma série no TMDB para começar a guardar as minutagens.",
                        acao = "Buscar séries",
                        onAcao = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    TelaEstadoVazio(
                        icone = IconeFiltroVazioTemaA,
                        titulo = if (searchQuery.isNotBlank()) "Nada encontrado" else "Nenhuma série aqui",
                        texto = if (searchQuery.isNotBlank()) "Nenhuma série da sua coleção com \"${searchQuery.trim()}\" em \"$selectedFilter\"."
                            else "Não há séries com o filtro \"$selectedFilter\".",
                        acao = if (searchQuery.isNotBlank()) "Limpar busca" else "Ver todas",
                        onAcao = {
                            if (searchQuery.isNotBlank()) searchQuery = "" else onFilterSelected("Todas")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .transicaoTemaA(
                            chave = selectedTab,
                            profundidade = 0,
                            ativo = true,
                            animarEntrada = true
                        )
                        .faixaAcimaB()
                ) {
                    val cabecalhoNaLista = selectedTab == 0
                    if (cabecalhoNaLista) item(key = "cabecalhoInicio") { Column { cabecalhoInicio() } }
                    if (cabecalhoNaLista && visualMode == VisualMode.TEMA_C) {
                        item(key = "carrosselC") {
                            com.kalidorabr.bancodeminutagens.ui.temac.CarrosselSeriesTemaC(filteredSeries) { onOpenSerieDetail(it) }
                        }
                    } else items(
                        items = filteredSeries,
                        key = { it.id }
                    ) { serie ->
                        when (visualMode) {
                            else -> TelaLinhaSerie(
                                modifier = Modifier.animateItem(),
                                serie = serie,
                                onOpenSerieClick = { onOpenSerieDetail(serie) },
                                onContinueClick = onContinueClick
                            )
                        }
                    }
                }
            }
        }

        when (visualMode) {
            else -> androidx.compose.animation.AnimatedVisibility(
                visible = (visualMode != VisualMode.TEMA_B || barraAbasVisivel) && com.kalidorabr.bancodeminutagens.ui.temaa.components.SheetsAbertos.quantos == 0,
                enter = androidx.compose.animation.expandVertically(androidx.compose.animation.core.tween(260, easing = com.kalidorabr.bancodeminutagens.ui.temab.EasingFirmeB)) +
                    androidx.compose.animation.slideInVertically(androidx.compose.animation.core.tween(260, easing = com.kalidorabr.bancodeminutagens.ui.temab.EasingFirmeB)) { it },
                exit = androidx.compose.animation.shrinkVertically(androidx.compose.animation.core.tween(220)) +
                    androidx.compose.animation.slideOutVertically(androidx.compose.animation.core.tween(220)) { it }
            ) {
                TelaBarraAbas(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }
    }
}

@Preview(
    name = "Home Screen Full Preview",
    showBackground = true,
    backgroundColor = 0xFF050505,
    device = "spec:width=411dp,height=891dp"
)
@Composable
private fun HomeScreenPreview() {
    val sampleMinutagensPreenchidas = listOf(
        MinutagemSecao(tipo = TipoMinutagem.ABERTURA, estado = EstadoMinutagem.PREENCHIDO, tempoInicio = "01:00", tempoFim = "02:30"),
        MinutagemSecao(tipo = TipoMinutagem.RECAP, estado = EstadoMinutagem.NAO_POSSUI),
        MinutagemSecao(tipo = TipoMinutagem.ENCERRAMENTO, estado = EstadoMinutagem.PREENCHIDO, tempoInicio = "22:00", tempoFim = "23:30")
    )

    val sampleMinutagensPendentes = listOf(
        MinutagemSecao(tipo = TipoMinutagem.ABERTURA, estado = EstadoMinutagem.PENDENTE),
        MinutagemSecao(tipo = TipoMinutagem.RECAP, estado = EstadoMinutagem.PENDENTE),
        MinutagemSecao(tipo = TipoMinutagem.ENCERRAMENTO, estado = EstadoMinutagem.PENDENTE)
    )

    fun criarEpisodios(count: Int, concluidosCount: Int, temParcial: Boolean = false): List<EpisodioItem> {
        return List(count) { i ->
            val num = i + 1
            val status = when {
                num <= concluidosCount -> StatusEpisodio.CONCLUIDO
                temParcial && num == concluidosCount + 1 -> StatusEpisodio.PARCIAL
                else -> StatusEpisodio.PENDENTE
            }
            val mins = if (status == StatusEpisodio.CONCLUIDO || status == StatusEpisodio.PARCIAL) sampleMinutagensPreenchidas else sampleMinutagensPendentes
            EpisodioItem(
                id = "ep_$num",
                numero = num,
                titulo = "Episódio $num",
                status = status,
                dataExibicao = "20/05/2023",
                minutagens = mins
            )
        }
    }

    val sampleSeriesList = listOf(
        SerieItem(
            id = "1",
            titulo = "Digimon",
            ano = 1999,
            generos = "Animação • Aventura • Fantasia",
            status = "Em andamento",
            episodiosAssistidos = 2,
            totalEpisodios = 54,
            proximoEpisodio = "T01E03",
            imageUrl = "https://image.tmdb.org/t/p/w1280/1S187Iq3YQ8jQnS66n903S6S48s.jpg",
            posterUrl = "https://image.tmdb.org/t/p/w500/1S187Iq3YQ8jQnS66n903S6S48s.jpg",
            sinopse = "Sete crianças são transportadas para um mundo digital habitado por criaturas conhecidas como Digimon.",
            temporadas = listOf(
                TemporadaItem(id = "dg1", numero = "T01", nome = "Digimon Adventure", totalEpisodios = 54, episodiosConcluidos = 2, status = "Em andamento", episodios = criarEpisodios(54, 2))
            )
        ),
        SerieItem(
            id = "2",
            titulo = "Fullmetal Alchemist",
            ano = 2009,
            generos = "Ação • Drama • Fantasia",
            status = "Finalizada",
            episodiosAssistidos = 5,
            totalEpisodios = 64,
            proximoEpisodio = "T01E06",
            imageUrl = "https://image.tmdb.org/t/p/w1280/5I4i5I8582381283.jpg",
            posterUrl = "https://image.tmdb.org/t/p/w500/5I4i5I8582381283.jpg",
            sinopse = "Dois irmãos procuram a Pedra Filosofal para restaurar seus corpos após uma tentativa fracassada de alquimia.",
            temporadas = listOf(
                TemporadaItem(id = "fma1", numero = "T01", nome = "Brotherhood", totalEpisodios = 64, episodiosConcluidos = 5, status = "Em andamento", episodios = criarEpisodios(64, 5))
            )
        ),
        SerieItem(
            id = "3",
            titulo = "One Piece",
            ano = 1999,
            generos = "Ação • Aventura • Comédia",
            status = "Em andamento",
            episodiosAssistidos = 12,
            totalEpisodios = 1076,
            proximoEpisodio = "T01E13",
            imageUrl = "https://image.tmdb.org/t/p/w1280/onepiece.jpg",
            posterUrl = "https://image.tmdb.org/t/p/w500/onepiece.jpg",
            sinopse = "Monkey D. Luffy navega pelos mares com sua tripulação em busca do lendário tesouro One Piece.",
            temporadas = listOf(
                TemporadaItem(id = "op1", numero = "T01", nome = "East Blue", totalEpisodios = 1076, episodiosConcluidos = 12, status = "Em andamento", episodios = criarEpisodios(1076, 12))
            )
        )
    )

    BancoDeMinutagensTheme {
        HomeScreen(
            seriesList = sampleSeriesList,
            visualMode = VisualMode.TEMA_A
        )
    }
}

@Preview(
    name = "Auto Sync Indicator Preview",
    showBackground = true,
    backgroundColor = 0xFF050505,
    device = "spec:width=411dp,height=891dp"
)
@Composable
private fun AutoSyncIndicatorPreview() {
    BancoDeMinutagensTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            AutoSyncDiscreteIndicator(
                feedback = EpisodeFeedback.EpisodeSavedAndSynced,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 140.dp)
            )
        }
    }
}

fun parseSeasonNumber(raw: String): Int {
    val digits = raw.filter { it.isDigit() }
    return digits.toIntOrNull() ?: 1
}

fun criarSeriesIniciaisExemplo(): List<SerieItem> {
    val sampleMinutagens = listOf(
        MinutagemSecao(tipo = TipoMinutagem.ABERTURA, estado = EstadoMinutagem.PENDENTE),
        MinutagemSecao(tipo = TipoMinutagem.RECAP, estado = EstadoMinutagem.PENDENTE),
        MinutagemSecao(tipo = TipoMinutagem.ENCERRAMENTO, estado = EstadoMinutagem.PENDENTE)
    )

    fun criarEps(count: Int): List<EpisodioItem> {
        return List(count) { i ->
            EpisodioItem(
                id = "sample_ep_${i + 1}",
                numero = i + 1,
                titulo = "Episódio ${i + 1}",
                status = StatusEpisodio.PENDENTE,
                minutagens = sampleMinutagens
            )
        }
    }

    val digimon = SerieItem(
        id = "256201",
        titulo = "Digimon",
        ano = 1999,
        generos = "Animação • Aventura",
        status = "Em andamento",
        episodiosAssistidos = 0,
        totalEpisodios = 54,
        proximoEpisodio = "T01E01",
        imageUrl = "https://image.tmdb.org/t/p/w1280/1S187Iq3YQ8jQnS66n903S6S48s.jpg",
        posterUrl = "https://image.tmdb.org/t/p/w500/1S187Iq3YQ8jQnS66n903S6S48s.jpg",
        sinopse = "Sete crianças são transportadas para um mundo digital habitado por criaturas conhecidas como Digimon.",
        temporadas = listOf(
            TemporadaItem(
                id = "256201_s1",
                numero = "T01",
                nome = "Digimon Adventure",
                totalEpisodios = 54,
                episodiosConcluidos = 0,
                status = "Não iniciada",
                episodios = criarEps(54)
            )
        )
    )

    return listOf(SerieProgressoUtils.recalcularProgressoSerie(digimon))
}

/** Trechos de um episódio que já foram ao IntroDB e mudaram de tempo (pergunta de reenvio). */
data class PerguntaReenvio(val serieId: String, val temporadaId: String, val episodioId: String, val rotulos: Set<String>)
