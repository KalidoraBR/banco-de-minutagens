package com.kalidorabr.bancodeminutagens.data

import com.kalidorabr.bancodeminutagens.BuildConfig
import com.kalidorabr.bancodeminutagens.model.EpisodioItem
import com.kalidorabr.bancodeminutagens.model.EpisodiosNovosSerie
import com.kalidorabr.bancodeminutagens.model.EstadoMinutagem
import com.kalidorabr.bancodeminutagens.model.MinutagemSecao
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.model.StatusEpisodio
import com.kalidorabr.bancodeminutagens.model.TemporadaItem
import com.kalidorabr.bancodeminutagens.model.TipoMinutagem
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

// Search-only data: independent of the collection and its episode/minutagem models.
data class TmdbSerieResultado(
    val id: String,
    val titulo: String,
    val ano: String,
    val generos: String,
    val descricao: String,
    val posterUrl: String? = null,
    val generoIds: List<Int> = emptyList()
)

class TmdbConfiguracaoAusenteException : Exception("Configuração ausente.")

object TmdbService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .followRedirects(false)
        .build()

    private var generosCache: Map<Int, String>? = null

    suspend fun pesquisarSeries(query: String): List<TmdbSerieResultado> = withContext(Dispatchers.IO) {
        val termo = query.trim()
        if (termo.length < 2) return@withContext emptyList()
        if (BuildConfig.TMDB_READ_TOKEN.isBlank()) throw TmdbConfiguracaoAusenteException()

        val json = getJson("search/tv", mapOf(
            "query" to termo, "include_adult" to "false", "page" to "1"
        ))
        val results = json.getJSONArray("results")
        if (results.length() == 0) return@withContext emptyList()

        // Genre lookup is optional: its failure must not hide successful search results.
        val generos = generosCache ?: try {
            val array = getJson("genre/tv/list").getJSONArray("genres")
            buildMap {
                for (i in 0 until array.length()) {
                    val genre = array.getJSONObject(i)
                    put(genre.getInt("id"), genre.getString("name"))
                }
            }.also { generosCache = it }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            emptyMap()
        }

        List(results.length()) { index ->
            val item = results.getJSONObject(index)
            val ids = item.optJSONArray("genre_ids")
            val generoIds = if (ids == null) emptyList() else List(ids.length()) { ids.getInt(it) }
            val poster = item.text("poster_path")
            TmdbSerieResultado(
                id = item.getInt("id").toString(),
                titulo = item.text("name").ifBlank { item.text("original_name").ifBlank { "Sem título" } },
                ano = item.text("first_air_date").take(4).ifBlank { "Ano indisponível" },
                generos = generoIds.mapNotNull { generos[it] }.joinToString(" • "),
                descricao = item.text("overview"),
                posterUrl = poster.takeIf { it.startsWith("/") }?.let { "https://image.tmdb.org/t/p/w342$it" },
                generoIds = generoIds
            )
        }.distinctBy { it.id }
    }

    suspend fun buscarDetalhesSerieCompleta(tmdbId: String): SerieItem = withContext(Dispatchers.IO) {
        if (BuildConfig.TMDB_READ_TOKEN.isBlank()) throw TmdbConfiguracaoAusenteException()

        val detailJson = getJson("tv/$tmdbId", mapOf("append_to_response" to "external_ids"))
        val (imdbTmdb, tvdbSoTmdb) = codigosExternos(detailJson)
        val tvdbTmdb = tvdbSoTmdb.ifBlank { tvdbPeloWikidata(detailJson) }
        val titulo = detailJson.text("name").ifBlank { detailJson.text("original_name").ifBlank { "Sem título" } }
        val anoStr = detailJson.text("first_air_date").take(4)
        val ano = anoStr.toIntOrNull() ?: 0
        val sinopse = detailJson.text("overview")

        val posterPath = detailJson.text("poster_path")
        val posterUrl = if (posterPath.startsWith("/")) "https://image.tmdb.org/t/p/w500$posterPath" else ""

        val backdropPath = detailJson.text("backdrop_path")
        val backdropUrl = if (backdropPath.startsWith("/")) "https://image.tmdb.org/t/p/w1280$backdropPath" else posterUrl

        val genresArray = detailJson.optJSONArray("genres")
        val generosList = mutableListOf<String>()
        if (genresArray != null) {
            for (i in 0 until genresArray.length()) {
                val g = genresArray.getJSONObject(i)
                val gName = g.text("name")
                if (gName.isNotBlank()) generosList.add(gName)
            }
        }
        val generosText = generosList.joinToString(" • ").ifBlank { "Geral" }

        // O selo da série (Em andamento / Finalizada / Incompleta) é do usuário: não vem do status do TMDB
        val statusText = "Em andamento"

        val seasonsArray = detailJson.optJSONArray("seasons")
        val temporadasList = mutableListOf<TemporadaItem>()
        var totalEpisodiosGeral = 0

        val defaultMinutagens = listOf(
            MinutagemSecao(tipo = TipoMinutagem.ABERTURA, estado = EstadoMinutagem.PENDENTE),
            MinutagemSecao(tipo = TipoMinutagem.RECAP, estado = EstadoMinutagem.PENDENTE),
            MinutagemSecao(tipo = TipoMinutagem.ENCERRAMENTO, estado = EstadoMinutagem.PENDENTE)
        )

        if (seasonsArray != null) {
            for (i in 0 until seasonsArray.length()) {
                val sObj = seasonsArray.getJSONObject(i)
                val seasonNumber = sObj.optInt("season_number", -1)

                // REGRA OBRIGATÓRIA: Ignorar a temporada número 0 ("Especiais")
                if (seasonNumber <= 0) continue

                val seasonName = sObj.text("name").ifBlank { "Temporada $seasonNumber" }
                val seasonJson = try {
                    getJson("tv/$tmdbId/season/$seasonNumber")
                } catch (_: Exception) { null }

                val specificPosterPath = seasonJson?.text("poster_path") ?: ""
                val seasonPoster = if (specificPosterPath.startsWith("/")) specificPosterPath else sObj.text("poster_path")
                val seasonPosterUrl = if (seasonPoster.startsWith("/")) "https://image.tmdb.org/t/p/w342$seasonPoster" else posterUrl

                // Buscar episódios da temporada
                val episodiosList = mutableListOf<EpisodioItem>()
                try {
                    val episodesArray = seasonJson?.optJSONArray("episodes")
                    if (episodesArray != null) {
                        for (eIdx in 0 until episodesArray.length()) {
                            val epObj = episodesArray.getJSONObject(eIdx)
                            val epNum = epObj.optInt("episode_number", eIdx + 1)
                            val epName = epObj.text("name").ifBlank { "Episódio $epNum" }
                            val airDateRaw = epObj.text("air_date")
                            val airDateFormatted = formatAirDateToBrazilian(airDateRaw)

                            episodiosList.add(
                                EpisodioItem(
                                    id = "${tmdbId}_s${seasonNumber}_ep_$epNum",
                                    numero = epNum,
                                    titulo = epName,
                                    status = StatusEpisodio.PENDENTE,
                                    dataExibicao = airDateFormatted,
                                    duracaoMin = epObj.optInt("runtime", 0),
                                    minutagens = defaultMinutagens
                                )
                            )
                        }
                    } else {
                        throw IOException()
                    }
                } catch (_: Exception) {
                    val epCount = sObj.optInt("episode_count", 0)
                    for (epNum in 1..epCount) {
                        episodiosList.add(
                            EpisodioItem(
                                id = "${tmdbId}_s${seasonNumber}_ep_$epNum",
                                numero = epNum,
                                titulo = "Episódio $epNum",
                                status = StatusEpisodio.PENDENTE,
                                minutagens = defaultMinutagens
                            )
                        )
                    }
                }

                val numFormatado = "T" + if (seasonNumber < 10) "0$seasonNumber" else "$seasonNumber"
                totalEpisodiosGeral += episodiosList.size

                temporadasList.add(
                    TemporadaItem(
                        id = "${tmdbId}_s$seasonNumber",
                        numero = numFormatado,
                        nome = seasonName,
                        totalEpisodios = episodiosList.size,
                        episodiosConcluidos = 0,
                        status = "Não iniciada",
                        imageUrl = seasonPosterUrl,
                        episodios = episodiosList
                    )
                )
            }
        }

        SerieItem(
            id = tmdbId,
            titulo = titulo,
            ano = ano,
            generos = generosText,
            status = statusText,
            episodiosAssistidos = 0,
            totalEpisodios = totalEpisodiosGeral,
            proximoEpisodio = "S01E01",
            imageUrl = backdropUrl,
            posterUrl = posterUrl.ifEmpty { backdropUrl },
            sinopse = sinopse,
            identificadores = "IMDb • TVDB",
            imdbId = imdbTmdb,
            tvdbId = tvdbTmdb,
            aberturaAtiva = false,
            recapAtivo = false,
            encerramentoAtivo = false,
            duracaoFixaAbertura = "",
            duracaoFixaRecap = "",
            duracaoFixaEncerramento = "",
            minutagensAtivas = "0 de 3 ativas",
            temporadas = temporadasList
        )
    }

    suspend fun enriquecerSerieComTmdb(serieLocal: SerieItem): SerieItem = withContext(Dispatchers.IO) {
        if (BuildConfig.TMDB_READ_TOKEN.isBlank() || serieLocal.id.isBlank()) return@withContext serieLocal

        try {
            val tmdbId = serieLocal.id
            val detailJson = getJson("tv/$tmdbId", mapOf("append_to_response" to "external_ids"))
            val (imdbTmdb, tvdbSoTmdb) = codigosExternos(detailJson)
            val tvdbTmdb = tvdbSoTmdb.ifBlank { if (serieLocal.tvdbId.isBlank()) tvdbPeloWikidata(detailJson) else "" }

            val tituloTmdb = detailJson.text("name").ifBlank { detailJson.text("original_name") }
            val anoStr = detailJson.text("first_air_date").take(4)
            val anoTmdb = anoStr.toIntOrNull() ?: 0
            val sinopseTmdb = detailJson.text("overview")

            val posterPath = detailJson.text("poster_path")
            val seriesPosterUrl = if (posterPath.startsWith("/")) "https://image.tmdb.org/t/p/w500$posterPath" else serieLocal.posterUrl

            val backdropPath = detailJson.text("backdrop_path")
            val backdropUrl = if (backdropPath.startsWith("/")) "https://image.tmdb.org/t/p/w1280$backdropPath" else seriesPosterUrl.ifBlank { serieLocal.imageUrl }

            val genresArray = detailJson.optJSONArray("genres")
            val generosList = mutableListOf<String>()
            if (genresArray != null) {
                for (i in 0 until genresArray.length()) {
                    val g = genresArray.getJSONObject(i)
                    val gName = g.text("name")
                    if (gName.isNotBlank()) generosList.add(gName)
                }
            }
            val generosText = generosList.joinToString(" • ").ifBlank { serieLocal.generos }

            val seasonPosterMap = mutableMapOf<Int, String>()
            val seasonsArray = detailJson.optJSONArray("seasons")
            if (seasonsArray != null) {
                for (i in 0 until seasonsArray.length()) {
                    val sObj = seasonsArray.getJSONObject(i)
                    val sNum = sObj.optInt("season_number", -1)
                    val pPath = sObj.text("poster_path")
                    if (pPath.startsWith("/")) {
                        seasonPosterMap[sNum] = "https://image.tmdb.org/t/p/w342$pPath"
                    }
                }
            }

            val temporadasEnriquecidas = serieLocal.temporadas.map { temp ->
                val sNum = parseSeasonNumber(temp.numero)

                var posterTemporada = seasonPosterMap[sNum] ?: ""
                val tmdbEpisodesMap = mutableMapOf<Int, Pair<String, String>>()
                val tmdbDuracoes = mutableMapOf<Int, Int>()

                val seasonJson = try {
                    getJson("tv/$tmdbId/season/$sNum")
                } catch (_: Exception) { null }

                if (seasonJson != null) {
                    val pPath = seasonJson.text("poster_path")
                    if (pPath.startsWith("/") && posterTemporada.isBlank()) {
                        posterTemporada = "https://image.tmdb.org/t/p/w342$pPath"
                    }

                    val epArray = seasonJson.optJSONArray("episodes")
                    if (epArray != null) {
                        for (eIdx in 0 until epArray.length()) {
                            val epObj = epArray.getJSONObject(eIdx)
                            val epNum = epObj.optInt("episode_number", eIdx + 1)
                            val epName = epObj.text("name").ifBlank { "Episódio $epNum" }
                            val rawAirDate = epObj.text("air_date")
                            val formattedAirDate = formatAirDateToBrazilian(rawAirDate)
                            tmdbEpisodesMap[epNum] = Pair(epName, formattedAirDate)
                            epObj.optInt("runtime", 0).takeIf { it > 0 }?.let { tmdbDuracoes[epNum] = it }
                        }
                    }
                }

                val finalSeasonPoster = when {
                    posterTemporada.isNotBlank() -> posterTemporada
                    temp.imageUrl.isNotBlank() -> temp.imageUrl
                    seriesPosterUrl.isNotBlank() -> seriesPosterUrl
                    else -> ""
                }

                val episodiosEnriquecidos = temp.episodios.map { localEp ->
                    val tmdbEpData = tmdbEpisodesMap[localEp.numero]

                    // 1. Data de exibição: preenche somente se ausente localmente
                    val novaData = if (localEp.dataExibicao.isBlank() && tmdbEpData != null) {
                        tmdbEpData.second
                    } else {
                        formatAirDateToBrazilian(localEp.dataExibicao)
                    }

                    // 2. Título: atualiza somente se estiver vazio ou for um título genérico ("Episódio X", "Episódio 0X")
                    val isTituloGenerico = localEp.titulo.isBlank() ||
                            localEp.titulo.equals("Episódio ${localEp.numero}", ignoreCase = true) ||
                            localEp.titulo.equals("Episódio 0${localEp.numero}", ignoreCase = true) ||
                            localEp.titulo.equals("Episodio ${localEp.numero}", ignoreCase = true) ||
                            localEp.titulo.equals("Episode ${localEp.numero}", ignoreCase = true)

                    val novoTitulo = if (isTituloGenerico && tmdbEpData != null && tmdbEpData.first.isNotBlank()) {
                        tmdbEpData.first
                    } else {
                        localEp.titulo
                    }

                    localEp.copy(
                        titulo = novoTitulo,
                        dataExibicao = novaData,
                        duracaoMin = if (localEp.duracaoMin > 0) localEp.duracaoMin else tmdbDuracoes[localEp.numero] ?: 0
                    )
                }

                temp.copy(
                    imageUrl = finalSeasonPoster,
                    episodios = episodiosEnriquecidos
                )
            }

            serieLocal.copy(
                titulo = if (serieLocal.titulo.startsWith("Série ")) tituloTmdb.ifBlank { serieLocal.titulo } else serieLocal.titulo,
                ano = if (serieLocal.ano == 0) anoTmdb else serieLocal.ano,
                posterUrl = seriesPosterUrl.ifBlank { serieLocal.posterUrl },
                imageUrl = backdropUrl.ifBlank { serieLocal.imageUrl },
                sinopse = if (serieLocal.sinopse.startsWith("Série importada")) sinopseTmdb.ifBlank { serieLocal.sinopse } else serieLocal.sinopse.ifBlank { sinopseTmdb },
                generos = if (serieLocal.generos == "Importada") generosText else serieLocal.generos.ifBlank { generosText },
                // o TMDB é a referência: corrige código errado ou vazio (o IntroDB usa o do IMDb)
                imdbId = imdbTmdb.ifBlank { serieLocal.imdbId },
                tvdbId = tvdbTmdb.ifBlank { serieLocal.tvdbId },
                temporadas = temporadasEnriquecidas
            )
        } catch (_: Exception) {
            serieLocal
        }
    }

    /**
     * Procura episódios que já foram ao ar no TMDB e ainda não estão na série salva.
     * Só lê: não altera nada. Devolve null quando não há novidade (ou sem internet).
     */
    suspend fun buscarEpisodiosNovos(serie: SerieItem, hojeIso: String): EpisodiosNovosSerie? = withContext(Dispatchers.IO) {
        if (BuildConfig.TMDB_READ_TOKEN.isBlank() || serie.id.isBlank()) return@withContext null
        try {
            val tmdbId = serie.id
            val detailJson = getJson("tv/$tmdbId")
            val seasonsArray = detailJson.optJSONArray("seasons") ?: return@withContext null
            val locais = serie.temporadas.associateBy { parseSeasonNumber(it.numero) }
            val novas = mutableListOf<TemporadaItem>()
            var totalTmdb = 0
            val defaultMinutagens = listOf(
                MinutagemSecao(tipo = TipoMinutagem.ABERTURA, estado = EstadoMinutagem.PENDENTE),
                MinutagemSecao(tipo = TipoMinutagem.RECAP, estado = EstadoMinutagem.PENDENTE),
                MinutagemSecao(tipo = TipoMinutagem.ENCERRAMENTO, estado = EstadoMinutagem.PENDENTE)
            )

            for (i in 0 until seasonsArray.length()) {
                val sObj = seasonsArray.getJSONObject(i)
                val seasonNumber = sObj.optInt("season_number", -1)
                if (seasonNumber <= 0) continue
                val epCount = sObj.optInt("episode_count", 0)
                totalTmdb += epCount
                val local = locais[seasonNumber]
                if (local != null && local.episodios.size >= epCount) continue

                val seasonJson = getJson("tv/$tmdbId/season/$seasonNumber")
                val numerosLocais = local?.episodios?.map { it.numero }?.toSet().orEmpty()
                val epArray = seasonJson.optJSONArray("episodes") ?: continue
                val episodios = mutableListOf<EpisodioItem>()
                for (eIdx in 0 until epArray.length()) {
                    val epObj = epArray.getJSONObject(eIdx)
                    val epNum = epObj.optInt("episode_number", eIdx + 1)
                    val airDate = epObj.text("air_date")
                    // só o que já foi ao ar (datas no formato aaaa-mm-dd comparam como texto)
                    if (epNum in numerosLocais || airDate.isBlank() || airDate > hojeIso) continue
                    episodios.add(
                        EpisodioItem(
                            id = "${tmdbId}_s${seasonNumber}_ep_$epNum",
                            numero = epNum,
                            titulo = epObj.text("name").ifBlank { "Episódio $epNum" },
                            status = StatusEpisodio.PENDENTE,
                            dataExibicao = formatAirDateToBrazilian(airDate),
                            duracaoMin = epObj.optInt("runtime", 0),
                            minutagens = defaultMinutagens
                        )
                    )
                }
                if (episodios.isEmpty()) continue

                val numFormatado = "T" + if (seasonNumber < 10) "0$seasonNumber" else "$seasonNumber"
                val posterPath = seasonJson.text("poster_path").ifBlank { sObj.text("poster_path") }
                novas.add(
                    local?.copy(episodios = episodios) ?: TemporadaItem(
                        id = "${tmdbId}_s$seasonNumber",
                        numero = numFormatado,
                        nome = sObj.text("name").ifBlank { "Temporada $seasonNumber" },
                        totalEpisodios = episodios.size,
                        episodiosConcluidos = 0,
                        imageUrl = if (posterPath.startsWith("/")) "https://image.tmdb.org/t/p/w342$posterPath" else serie.posterUrl,
                        episodios = episodios
                    )
                )
            }
            if (novas.isEmpty()) null
            else EpisodiosNovosSerie(serie.id, serie.titulo, serie.posterUrl, totalTmdb, novas)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    /** Códigos do IMDb (tt…) e do TVDB vindos do TMDB ("" quando o TMDB não tem). */
    private fun codigosExternos(detailJson: JSONObject): Pair<String, String> {
        val ext = detailJson.optJSONObject("external_ids") ?: return "" to ""
        val imdb = ext.text("imdb_id").takeIf { it.startsWith("tt") }.orEmpty()
        val tvdb = if (ext.isNull("tvdb_id")) "" else ext.optLong("tvdb_id", 0L).takeIf { it > 0 }?.toString().orEmpty()
        return imdb to tvdb
    }

    /** TVDB pelo Wikidata, para quando o TMDB não tem (o TMDB informa qual é o item da série lá). */
    private suspend fun tvdbPeloWikidata(detailJson: JSONObject): String =
        detailJson.optJSONObject("external_ids")?.text("wikidata_id")
            ?.takeIf { it.isNotBlank() }
            ?.let { Wikidata.tvdbId(it) }
            .orEmpty()

    /** Código do IMDb da série (tt…), pelo TMDB. Só lê; null se não achar. */
    suspend fun buscarImdbId(tmdbId: String): String? = withContext(Dispatchers.IO) {
        if (BuildConfig.TMDB_READ_TOKEN.isBlank() || tmdbId.isBlank()) return@withContext null
        try {
            getJson("tv/$tmdbId/external_ids").text("imdb_id").takeIf { it.startsWith("tt") }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    fun formatAirDateToBrazilian(rawDate: String): String {
        val trimmed = rawDate.trim()
        if (trimmed.isBlank()) return ""

        if (Regex("^\\d{2}/\\d{2}/\\d{4}$").matches(trimmed)) {
            return trimmed
        }

        val matchYmd = Regex("^(\\d{4})-(\\d{2})-(\\d{2})$").find(trimmed)
        if (matchYmd != null) {
            val (year, month, day) = matchYmd.destructured
            return "$day/$month/$year"
        }

        return trimmed
    }

    private fun parseSeasonNumber(raw: String): Int {
        val digits = raw.filter { it.isDigit() }
        return digits.toIntOrNull() ?: 1
    }

    private fun JSONObject.text(key: String): String = if (isNull(key)) "" else optString(key, "")

    private suspend fun getJson(path: String, params: Map<String, String> = emptyMap()): JSONObject {
        val url = "https://api.themoviedb.org/3/$path".toHttpUrl().newBuilder()
            .addQueryParameter("language", "pt-BR")
            .apply { params.forEach { (key, value) -> addQueryParameter(key, value) } }
            .build()
        val request = Request.Builder().url(url)
            .header("Authorization", "Bearer ${BuildConfig.TMDB_READ_TOKEN}")
            .header("Accept", "application/json")
            .build()
        val body = suspendCancellableCoroutine<String> { continuation ->
            val call = client.newCall(request)
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) continuation.resumeWithException(IOException("Falha na pesquisa."))
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        response.use {
                            if (!it.isSuccessful) throw IOException("Falha na pesquisa.")
                            val text = it.body?.string() ?: throw IOException("Falha na pesquisa.")
                            if (continuation.isActive) continuation.resume(text)
                        }
                    } catch (_: Exception) {
                        if (continuation.isActive) continuation.resumeWithException(IOException("Falha na pesquisa."))
                    }
                }
            })
        }
        return JSONObject(body)
    }
}
