package com.kalidorabr.bancodeminutagens.data

import android.content.Context
import androidx.core.content.edit
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.model.SerieProgressoUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Andamento da conferência de todas as séries no IntroDB (para a barra da tela Sincronização). */
data class ProgressoConferencia(
    val rodando: Boolean = false,
    val serieAtual: String = "",
    val serieIndice: Int = 0,
    val seriesTotal: Int = 0,
    val feitos: Int = 0,
    val total: Int = 0,
    val comDados: Int = 0,
    /** Parou porque o site pediu pausa (continua na próxima vez). */
    val pausaDoSite: Boolean = false
) {
    val porcentagem: Int get() = if (total == 0) 0 else feitos * 100 / total
}

/**
 * Confere no IntroDB (sem chave) quais episódios já têm minutagem no site, em todas as séries.
 * Roda ao abrir o app, enquanto ele está aberto e em segundo plano pelo Android (com o app fechado).
 * Os nunca conferidos vão primeiro; os sem as três seções voltam a ser conferidos depois de 1 dia.
 */
object ConferenciaIntroDb {
    private const val PREFS = "conferencia_introdb"
    private const val KEY_ULTIMA = "ultima"
    private const val TRABALHO = "conferencia_introdb"
    private const val UM_DIA = 24 * 60 * 60 * 1000L
    private val formatoBr = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private val _estado = MutableStateFlow(ProgressoConferencia())
    val estado: StateFlow<ProgressoConferencia> = _estado

    // uma conferência por vez (app e segundo plano rodam no mesmo processo)
    private val trava = Mutex()

    fun ultima(ctx: Context): Long = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_ULTIMA, 0L)

    /** Liga (ou desliga) a conferência pelo Android com o app fechado, a cada ~15 min com internet. */
    fun agendar(ctx: Context) {
        val wm = WorkManager.getInstance(ctx)
        if (!IntroDbPreferences(ctx).conferenciaAutomatica()) {
            wm.cancelUniqueWork(TRABALHO)
            return
        }
        val req = PeriodicWorkRequestBuilder<ConferenciaIntroDbWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        wm.enqueueUniquePeriodicWork(TRABALHO, ExistingPeriodicWorkPolicy.KEEP, req)
    }

    private data class Pendente(val serie: SerieItem, val temporada: Int, val episodioId: String, val episodio: Int, val ordem: Long)

    /** Episódios que precisam ser conferidos, na ordem em que vão ser. */
    fun pendentes(series: List<SerieItem>, prefs: IntroDbPreferences, agora: Long = System.currentTimeMillis()): Int =
        listaPendentes(series, prefs, agora).size

    private fun listaPendentes(series: List<SerieItem>, prefs: IntroDbPreferences, agora: Long): List<Pendente> {
        val hoje = LocalDate.now()
        return series
            .filter { !it.isDeleted } // episódios que ainda não estrearam são pulados pela data, logo abaixo
            .sortedByDescending { it.lastUpdatedAt ?: 0L }
            .flatMap { s ->
                s.temporadas.flatMap { t ->
                    val n = SerieProgressoUtils.numeroTemporada(t.numero)?.takeIf { it > 0 } ?: return@flatMap emptyList()
                    t.episodios.sortedBy { it.numero }.mapNotNull { ep ->
                        val estreia = runCatching { LocalDate.parse(ep.dataExibicao.trim(), formatoBr) }.getOrNull()
                        if (estreia != null && estreia.isAfter(hoje)) return@mapNotNull null
                        val conferido = prefs.conferido(ep.id)
                        val quando = prefs.conferidoEm(ep.id)
                        val precisa = !conferido || (prefs.noSite(ep.id).size < 3 && agora - quando > UM_DIA)
                        if (precisa) Pendente(s, n, ep.id, ep.numero, if (conferido) quando + 1 else 0L) else null
                    }
                }
            }
            // nunca conferidos primeiro, mantendo a ordem das séries e dos episódios
            .sortedBy { it.ordem }
    }

    /**
     * Confere até [limite] episódios. [imdbDe] devolve o código do IMDb da série (ou null para pular).
     * Devolve false se outra conferência já estava rodando.
     */
    suspend fun rodar(
        ctx: Context,
        series: List<SerieItem>,
        limite: Int = 60,
        imdbDe: suspend (SerieItem) -> String?,
        aoConferir: () -> Unit = {}
    ): Boolean {
        if (!trava.tryLock()) return false
        try {
            val prefs = IntroDbPreferences(ctx)
            // escolhe os mais urgentes e depois arruma série por série, para a barra andar em ordem
            val posicao = series.withIndex().associate { (i, s) -> s.id to i }
            val lote = listaPendentes(series, prefs, System.currentTimeMillis()).take(limite)
                .sortedWith(compareBy({ posicao[it.serie.id] ?: 0 }, { it.temporada }, { it.episodio }))
            val ordemSeries = lote.map { it.serie.id }.distinct()
            var comDados = 0
            var pausa = false
            _estado.value = ProgressoConferencia(rodando = true, seriesTotal = ordemSeries.size, total = lote.size)
            val imdbs = mutableMapOf<String, String?>()
            for ((i, p) in lote.withIndex()) {
                _estado.value = _estado.value.copy(
                    serieAtual = p.serie.titulo,
                    serieIndice = ordemSeries.indexOf(p.serie.id) + 1,
                    feitos = i
                )
                val imdb = imdbs.getOrPut(p.serie.id) { imdbDe(p.serie) } ?: continue
                var tipos = IntroDbService.consultar(imdb, p.temporada, p.episodio)
                if (tipos != null && IntroDbService.LIMITE in tipos) {
                    // o site pediu pausa: espera um pouco e tenta uma vez; se continuar, para e segue na próxima
                    delay(20_000L)
                    tipos = IntroDbService.consultar(imdb, p.temporada, p.episodio)
                    if (tipos != null && IntroDbService.LIMITE in tipos) { pausa = true; break }
                }
                if (tipos != null) {
                    prefs.salvarNoSite(p.episodioId, tipos)
                    if (tipos.isNotEmpty()) comDados++
                    aoConferir()
                }
                _estado.value = _estado.value.copy(feitos = i + 1, comDados = comDados)
                delay(1_200)
            }
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putLong(KEY_ULTIMA, System.currentTimeMillis()) }
            _estado.value = _estado.value.copy(rodando = false, pausaDoSite = pausa)
            return true
        } finally {
            if (_estado.value.rodando) _estado.value = _estado.value.copy(rodando = false)
            trava.unlock()
        }
    }
}

/** Conferência com o app fechado: lê as séries salvas e confere um lote. */
class ConferenciaIntroDbWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val ctx = applicationContext
        if (!IntroDbPreferences(ctx).conferenciaAutomatica()) return Result.success()
        val series = SeriesRepository(ctx).carregarSeries()
        // sem salvar nada nas séries: o código do IMDb buscado aqui só é usado nesta conferência
        ConferenciaIntroDb.rodar(ctx, series, limite = 40, imdbDe = { s ->
            s.imdbId.trim().takeIf { it.startsWith("tt") } ?: TmdbService.buscarImdbId(s.id)
        })
        return Result.success()
    }
}
