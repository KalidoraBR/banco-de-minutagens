// ARQUIVO GERADO a partir das telas do Tema A. Cada função "Tela..." escolhe o desenho
// conforme o tema ativo (LocalModoVisual). A lógica (salvar, sincronizar, navegar)
// continua no MainActivity e chega aqui só como dados e ações.
package com.kalidorabr.bancodeminutagens.ui.temas

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kalidorabr.bancodeminutagens.R
import com.kalidorabr.bancodeminutagens.data.GoogleSheetsExportService
import com.kalidorabr.bancodeminutagens.data.GoogleSheetsImportService
import com.kalidorabr.bancodeminutagens.data.GoogleSheetsPreferences
import com.kalidorabr.bancodeminutagens.data.TmdbConfiguracaoAusenteException
import com.kalidorabr.bancodeminutagens.data.TmdbSerieResultado
import com.kalidorabr.bancodeminutagens.data.TmdbService
import com.kalidorabr.bancodeminutagens.data.VisualMode
import com.kalidorabr.bancodeminutagens.isNetworkAvailable
import com.kalidorabr.bancodeminutagens.model.DestinoEpisodio
import com.kalidorabr.bancodeminutagens.model.EpisodioItem
import com.kalidorabr.bancodeminutagens.model.EstadoMinutagem
import com.kalidorabr.bancodeminutagens.model.MinutagemInconsistente
import com.kalidorabr.bancodeminutagens.model.MinutagemSecao
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.model.SerieProgressoUtils
import com.kalidorabr.bancodeminutagens.model.StatusEpisodio
import com.kalidorabr.bancodeminutagens.model.TemporadaItem
import com.kalidorabr.bancodeminutagens.model.TipoMinutagem
import com.kalidorabr.bancodeminutagens.ui.season.BatchFillRequest
import com.kalidorabr.bancodeminutagens.ui.season.requestApplicable
import com.kalidorabr.bancodeminutagens.ui.settings.SyncFeedbackMessage
import com.kalidorabr.bancodeminutagens.ui.settings.historicoVersoesApp
import com.kalidorabr.bancodeminutagens.ui.temaa.BuscarTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.ContinueCardTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.DadosBackupTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.EmBreveHeaderTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.EmBreveListaTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.EpisodioDetailTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.EstadoVazioTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.FilterChipsRowTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.GoogleSheetsTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.HomeHeaderTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.LixeiraTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.SerieDetailTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.SerieTemaACardItem
import com.kalidorabr.bancodeminutagens.ui.temaa.SettingsTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.SobreTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.TemaATokens
import com.kalidorabr.bancodeminutagens.ui.temaa.TemporadaDetailTemaAScreen
import com.kalidorabr.bancodeminutagens.ui.temaa.components.AvisoTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.BarraStatusAdaptativaTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.HomeBottomNavigationTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaABottomSheet
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAGroup
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAGroupDivider
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAMenuItem
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAOverflowMenu
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaAPrimaryButton
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASecondaryButton
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASheetColors
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASheetHeader
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TemaASwitch
import com.kalidorabr.bancodeminutagens.ui.temaa.components.TipoAvisoTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.cliqueTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.posterCompartilhadoTemaA
import com.kalidorabr.bancodeminutagens.ui.temaa.components.topoClaroTemaA
import com.kalidorabr.bancodeminutagens.ui.theme.AppThemeOpcao
import com.kalidorabr.bancodeminutagens.ui.theme.BancoDeMinutagensTheme
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TelaAviso(
    titulo: String,
    tipo: TipoAvisoTemaA,
    modifier: Modifier = Modifier,
    subtitulo: String = "",
    acao: String? = null,
    onAcao: () -> Unit = {}
) {
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        // Para trocar: acrescente aqui, por exemplo, VisualMode.TEMA_B -> AvisoTemaB(...)
        else -> AvisoTemaA(titulo = titulo, tipo = tipo, modifier = modifier, subtitulo = subtitulo, acao = acao, onAcao = onAcao)
    }
}

@Composable
fun TelaBuscar(
    modifier: Modifier = Modifier,
    seriesSalvasIds: Set<String> = emptySet(),
    onAdicionarSerie: suspend (TmdbSerieResultado) -> Result<Unit> = { Result.success(Unit) }
) {
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        // Para trocar: acrescente aqui, por exemplo, VisualMode.TEMA_B -> BuscarTemaBScreen(...)
        else -> BuscarTemaAScreen(modifier = modifier, seriesSalvasIds = seriesSalvasIds, onAdicionarSerie = onAdicionarSerie)
    }
}

@Composable
fun TelaContinuar(
    serie: SerieItem,
    destino: DestinoEpisodio,
    onOpenSerieClick: () -> Unit,
    onContinueClick: (DestinoEpisodio) -> Unit
) {
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        VisualMode.TEMA_C -> com.kalidorabr.bancodeminutagens.ui.temac.ContinueCardTemaC(serie = serie, destino = destino, onOpenSerieClick = onOpenSerieClick, onContinueClick = onContinueClick)
        VisualMode.TEMA_D -> com.kalidorabr.bancodeminutagens.ui.temad.ContinueCardTemaD(serie = serie, destino = destino, onOpenSerieClick = onOpenSerieClick, onContinueClick = onContinueClick)
        else -> ContinueCardTemaA(serie = serie, destino = destino, onOpenSerieClick = onOpenSerieClick, onContinueClick = onContinueClick)
    }
}

@Composable
fun TelaDadosBackup(
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
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        // Para trocar: acrescente aqui, por exemplo, VisualMode.TEMA_B -> DadosBackupTemaBScreen(...)
        else -> DadosBackupTemaAScreen(series = series, isOnline = isOnline, hasPendingSync = hasPendingSync, lastSyncDate = lastSyncDate, isSyncConfigured = isSyncConfigured, isAutoSyncEnabled = isAutoSyncEnabled, lastSyncStatus = lastSyncStatus, onBackClick = onBackClick, onExportBackup = onExportBackup, onImportBackup = onImportBackup, onOpenIssue = onOpenIssue, onSyncClick = onSyncClick)
    }
}

@Composable
fun TelaEmBreveCabecalho(
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {}
) {
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        // Para trocar: acrescente aqui, por exemplo, VisualMode.TEMA_B -> EmBreveHeaderTemaB(...)
        else -> EmBreveHeaderTemaA(searchQuery = searchQuery, onSearchQueryChange = onSearchQueryChange)
    }
}

@Composable
fun TelaEmBreveLista(
    series: List<SerieItem>,
    temSeriesNaColecao: Boolean,
    onOpenSerie: (SerieItem) -> Unit,
    onComecar: (SerieItem) -> Unit,
    onBuscarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        // Para trocar: acrescente aqui, por exemplo, VisualMode.TEMA_B -> EmBreveListaTemaB(...)
        else -> EmBreveListaTemaA(series = series, temSeriesNaColecao = temSeriesNaColecao, onOpenSerie = onOpenSerie, onComecar = onComecar, onBuscarClick = onBuscarClick, modifier = modifier)
    }
}

@Composable
fun TelaEpisodio(
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
    onUsarIntroDb: () -> Unit = {},
    duracaoEpisodioMin: Int = 0
) {
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        // Para trocar: acrescente aqui, por exemplo, VisualMode.TEMA_B -> EpisodioDetailTemaBScreen(...)
        else -> EpisodioDetailTemaAScreen(nomeSerie = nomeSerie, temporadaLabel = temporadaLabel, numeroEpisodio = numeroEpisodio, tituloEpisodio = tituloEpisodio, dataExibicao = dataExibicao, statusEpisodioLabel = statusEpisodioLabel, minutagens = minutagens, isAnteriorEnabled = isAnteriorEnabled, isProximoEnabled = isProximoEnabled, isCopyEnabled = isCopyEnabled, copySourceLabel = copySourceLabel, onBackClick = onBackClick, onCopyClick = onCopyClick, onSecaoClick = onSecaoClick, onAnteriorClick = { com.kalidorabr.bancodeminutagens.ui.temab.DirecaoEpisodioB.anterior = true; onAnteriorClick() }, onProximoClick = { com.kalidorabr.bancodeminutagens.ui.temab.DirecaoEpisodioB.anterior = false; onProximoClick() }, secaoEditando = secaoEditando, duracaoFixaEditando = duracaoFixaEditando, onSaveSecao = onSaveSecao, onDismissEdit = onDismissEdit, minutagensAnterior = minutagensAnterior, rotuloAnterior = rotuloAnterior, rotuloProximo = rotuloProximo, duracaoTipicaEditando = duracaoTipicaEditando, onConcluirClick = onConcluirClick, introDbResumo = introDbResumo, statusPlanilha = statusPlanilha, statusIntroDb = statusIntroDb, onTocarPlanilha = onTocarPlanilha, onTocarIntroDb = onTocarIntroDb, temporadaCompleta = temporadaCompleta, rotuloProximaTemporada = rotuloProximaTemporada, onProximaTemporada = onProximaTemporada, onIrProximoPendente = onIrProximoPendente, onUsarIntroDb = onUsarIntroDb, duracaoEpisodioMin = duracaoEpisodioMin)
    }
}

@Composable
fun TelaEstadoVazio(
    icone: ImageVector,
    titulo: String,
    texto: String,
    modifier: Modifier = Modifier,
    acao: String? = null,
    onAcao: () -> Unit = {}
) {
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        // Para trocar: acrescente aqui, por exemplo, VisualMode.TEMA_B -> EstadoVazioTemaB(...)
        else -> EstadoVazioTemaA(icone = icone, titulo = titulo, texto = texto, modifier = modifier, acao = acao, onAcao = onAcao)
    }
}

@Composable
fun TelaFiltros(
    filters: List<String> = listOf("Todas", "Em andamento", "Finalizadas"),
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    totalCount: Int? = null
) {
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        VisualMode.TEMA_B -> {
            val idx = filters.indexOfFirst {
                it.equals(selectedFilter, true) || (it == "Finalizadas" && selectedFilter == "Concluídas")
            }.coerceAtLeast(0)
            com.kalidorabr.bancodeminutagens.ui.temab.FiltrosTemaB(
                opcoes = filters,
                rotulos = filters.map { if (it == "Todas" && totalCount != null) "Todas · $totalCount" else it },
                selecionado = idx,
                onSelecionar = { onFilterSelected(filters[it]) },
                modifier = Modifier.padding(horizontal = TemaATokens.SideMargin, vertical = 10.dp)
            )
        }
        VisualMode.TEMA_D -> {
            val idx = filters.indexOfFirst { it.equals(selectedFilter, true) || (it == "Finalizadas" && selectedFilter == "Concluídas") }.coerceAtLeast(0)
            com.kalidorabr.bancodeminutagens.ui.temad.FiltrosTemaD(
                rotulos = filters.map { if (it == "Todas" && totalCount != null) "Todas · $totalCount" else it },
                selecionado = idx,
                onSelecionar = { onFilterSelected(filters[it]) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
        else -> FilterChipsRowTemaA(filters = filters, selectedFilter = selectedFilter, onFilterSelected = onFilterSelected, totalCount = totalCount)
    }
}

@Composable
fun TelaGoogleSheets(
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
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        // Para trocar: acrescente aqui, por exemplo, VisualMode.TEMA_B -> GoogleSheetsTemaBScreen(...)
        else -> GoogleSheetsTemaAScreen(modifier = modifier, seriesLocaisAtuais = seriesLocaisAtuais, onImportSuccess = onImportSuccess, onBackClick = onBackClick, chaveIntroDb = chaveIntroDb, onSalvarChaveIntroDb = onSalvarChaveIntroDb, autoIntroDb = autoIntroDb, onAutoIntroDbChange = onAutoIntroDbChange, onConferirIntroDbAgora = onConferirIntroDbAgora)
    }
}

@Composable
fun TelaBarraAbas(
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {}
) {
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        VisualMode.TEMA_B -> com.kalidorabr.bancodeminutagens.ui.temab.BarraAbasTemaB(
            itens = listOf(
                painterResource(R.drawable.ic_home) to "Início",
                painterResource(R.drawable.ic_search) to "Buscar",
                painterResource(R.drawable.ic_em_breve) to "Em breve",
                painterResource(R.drawable.ic_adjusts_sliders) to "Ajustes"
            ),
            selecionado = selectedTab,
            onSelecionar = onTabSelected
        )
        VisualMode.TEMA_C -> com.kalidorabr.bancodeminutagens.ui.temac.BarraAbasTemaC(listOf(
                painterResource(R.drawable.ic_home) to "Início",
                painterResource(R.drawable.ic_search) to "Buscar",
                painterResource(R.drawable.ic_em_breve) to "Em breve",
                painterResource(R.drawable.ic_adjusts_sliders) to "Ajustes"
            ), selectedTab, onTabSelected)
        VisualMode.TEMA_D -> com.kalidorabr.bancodeminutagens.ui.temad.BarraAbasTemaD(listOf(
                painterResource(R.drawable.ic_home) to "Início",
                painterResource(R.drawable.ic_search) to "Buscar",
                painterResource(R.drawable.ic_em_breve) to "Em breve",
                painterResource(R.drawable.ic_adjusts_sliders) to "Ajustes"
            ), selectedTab, onTabSelected)
        else -> HomeBottomNavigationTemaA(selectedTab = selectedTab, onTabSelected = onTabSelected)
    }
}

@Composable
fun TelaInicioCabecalho(
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    subtitulo: String = "Continuar de onde parou"
) {
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        // Para trocar: acrescente aqui, por exemplo, VisualMode.TEMA_B -> HomeHeaderTemaB(...)
        else -> HomeHeaderTemaA(searchQuery = searchQuery, onSearchQueryChange = onSearchQueryChange, onRefreshClick = onRefreshClick, onSearchClick = onSearchClick, subtitulo = subtitulo)
    }
}

@Composable
fun TelaLixeira(
    seriesLixeira: List<SerieItem>,
    permanentlyDeletedIds: Set<String>,
    onBackClick: () -> Unit,
    onRestaurarSerie: (SerieItem) -> Unit,
    onExcluirDefinitivamente: (SerieItem) -> Unit,
    onPermitirImportacao: (String) -> Unit
) {
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        // Para trocar: acrescente aqui, por exemplo, VisualMode.TEMA_B -> LixeiraTemaBScreen(...)
        else -> LixeiraTemaAScreen(seriesLixeira = seriesLixeira, permanentlyDeletedIds = permanentlyDeletedIds, onBackClick = onBackClick, onRestaurarSerie = onRestaurarSerie, onExcluirDefinitivamente = onExcluirDefinitivamente, onPermitirImportacao = onPermitirImportacao)
    }
}

@Composable
fun TelaSerie(
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
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        VisualMode.TEMA_D -> com.kalidorabr.bancodeminutagens.ui.temad.SerieDetailTemaDScreen(serie = serie, onBackClick = onBackClick, onMoverParaLixeiraClick = onMoverParaLixeiraClick, onTemporadaClick = onTemporadaClick, onSaveStatus = onSaveStatus, onSaveIdentificadores = onSaveIdentificadores, onSaveMinutagemFixa = onSaveMinutagemFixa, onContinueClick = onContinueClick, onSyncTmdbClick = onSyncTmdbClick, isSyncingTmdb = isSyncingTmdb)
        VisualMode.TEMA_B -> com.kalidorabr.bancodeminutagens.ui.temab.SerieDetailTemaBScreen(serie = serie, onBackClick = onBackClick, onMoverParaLixeiraClick = onMoverParaLixeiraClick, onTemporadaClick = onTemporadaClick, onSaveStatus = onSaveStatus, onSaveIdentificadores = onSaveIdentificadores, onSaveMinutagemFixa = onSaveMinutagemFixa, onContinueClick = onContinueClick, onSyncTmdbClick = onSyncTmdbClick, isSyncingTmdb = isSyncingTmdb)
        else -> SerieDetailTemaAScreen(serie = serie, onBackClick = onBackClick, onShareClick = onShareClick, onStatusClick = onStatusClick, onIdentificadoresClick = onIdentificadoresClick, onMinutagemFixaClick = onMinutagemFixaClick, onMoverParaLixeiraClick = onMoverParaLixeiraClick, onTemporadaClick = onTemporadaClick, onSaveStatus = onSaveStatus, onSaveIdentificadores = onSaveIdentificadores, onSaveMinutagemFixa = onSaveMinutagemFixa, onContinueClick = onContinueClick, onSyncTmdbClick = onSyncTmdbClick, isSyncingTmdb = isSyncingTmdb)
    }
}

@Composable
fun TelaLinhaSerie(
    serie: SerieItem,
    onOpenSerieClick: (SerieItem) -> Unit = {},
    onContinueClick: (DestinoEpisodio) -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        VisualMode.TEMA_D -> com.kalidorabr.bancodeminutagens.ui.temad.SerieTemaDCardItem(serie = serie, onOpenSerieClick = onOpenSerieClick, modifier = modifier)
        else -> SerieTemaACardItem(serie = serie, onOpenSerieClick = onOpenSerieClick, onContinueClick = onContinueClick, modifier = modifier)
    }
}

@Composable
fun TelaAjustes(
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
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        // Para trocar: acrescente aqui, por exemplo, VisualMode.TEMA_B -> SettingsTemaBScreen(...)
        else -> SettingsTemaAScreen(modifier = modifier, selectedTheme = selectedTheme, onThemeSelected = onThemeSelected, visualMode = visualMode, onVisualModeSelected = onVisualModeSelected, onGoogleSheetsClick = onGoogleSheetsClick, onLixeiraClick = onLixeiraClick, onDadosBackupClick = onDadosBackupClick, onSobreClick = onSobreClick, onEstatisticasClick = onEstatisticasClick, chaveIntroDb = chaveIntroDb, onSalvarChaveIntroDb = onSalvarChaveIntroDb, autoIntroDb = autoIntroDb, onAutoIntroDbChange = onAutoIntroDbChange, maratona = maratona, onMaratonaChange = onMaratonaChange, avisoEstreia = avisoEstreia, onAvisoEstreiaChange = onAvisoEstreiaChange, backupAutoPasta = backupAutoPasta, onBackupAutoChange = onBackupAutoChange)
    }
}

@Composable
fun TelaSobre(
    onBackClick: () -> Unit,
    versaoNova: String? = null,
    onProcurarAtualizacao: suspend () -> Result<String?> = { Result.success(null) },
    onAtualizar: () -> Unit = {}
) {
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        // Para trocar: acrescente aqui, por exemplo, VisualMode.TEMA_B -> SobreTemaBScreen(...)
        else -> SobreTemaAScreen(onBackClick = onBackClick, versaoNova = versaoNova, onProcurarAtualizacao = onProcurarAtualizacao, onAtualizar = onAtualizar)
    }
}

@Composable
fun TelaTemporada(
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
    conferirAoAbrir: () -> Unit = {},
    temporadaAnterior: String? = null,
    temporadaProxima: String? = null,
    onTrocarTemporada: (Int) -> Unit = {}
) {
    // nome da temporada como chave: ao trocar pelas setas, confere a nova também
    LaunchedEffect(nomeTemporada) { conferirAoAbrir() }
    when (LocalModoVisual.current) {
        // Os temas B, C e D usam as telas do A até ganharem o próprio desenho.
        // Para trocar: acrescente aqui, por exemplo, VisualMode.TEMA_B -> TemporadaDetailTemaBScreen(...)
        else -> TemporadaDetailTemaAScreen(nomeSerie = nomeSerie, nomeTemporada = nomeTemporada, episodios = episodios, onBackClick = onBackClick, onExportJsonClick = onExportJsonClick, onBatchFillConfirmed = onBatchFillConfirmed, onEpisodioClick = onEpisodioClick, onLimparEpisodiosConfirmed = onLimparEpisodiosConfirmed, temChaveIntroDb = temChaveIntroDb, onEnviarIntroDbConfirmed = onEnviarIntroDbConfirmed, noIntroDb = noIntroDb, emRevisaoIntroDb = emRevisaoIntroDb, onConferirIntroDb = onConferirIntroDb, temporadaAnterior = temporadaAnterior, temporadaProxima = temporadaProxima, onTrocarTemporada = onTrocarTemporada)
    }
}
