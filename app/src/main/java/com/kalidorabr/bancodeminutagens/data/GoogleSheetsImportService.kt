package com.kalidorabr.bancodeminutagens.data

import com.kalidorabr.bancodeminutagens.model.StatusPlanilha
import android.util.Log
import com.kalidorabr.bancodeminutagens.model.EpisodioItem
import com.kalidorabr.bancodeminutagens.model.EstadoMinutagem
import com.kalidorabr.bancodeminutagens.model.MinutagemSecao
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.model.SerieProgressoUtils
import com.kalidorabr.bancodeminutagens.model.StatusEpisodio
import com.kalidorabr.bancodeminutagens.model.TemporadaItem
import com.kalidorabr.bancodeminutagens.model.TipoMinutagem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ResultadoImportacao(
    val success: Boolean,
    val seriesAtualizadasCount: Int = 0,
    val episodiosAtualizadosCount: Int = 0,
    val warnings: List<String> = emptyList(),
    val errorMessage: String? = null,
    val novasSeries: List<SerieItem> = emptyList()
)

object GoogleSheetsImportService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun executarImportacao(
        scriptUrl: String,
        spreadsheetId: String,
        seriesLocaisAtuais: List<SerieItem>,
        permanentlyDeletedIds: Set<String> = emptySet()
    ): ResultadoImportacao = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim()
        val cleanId = spreadsheetId.trim()

        if (cleanUrl.isBlank() || cleanId.isBlank()) {
            return@withContext ResultadoImportacao(
                success = false,
                errorMessage = "ID da planilha ou URL do Apps Script não informados."
            )
        }

        val requestUrl = try {
            cleanUrl.toHttpUrl().newBuilder()
                .addQueryParameter("action", "import_to_app")
                .addQueryParameter("spreadsheetId", cleanId)
                .build()
        } catch (e: Exception) {
            return@withContext ResultadoImportacao(
                success = false,
                errorMessage = "URL do Apps Script inválida."
            )
        }

        val request = Request.Builder()
            .url(requestUrl)
            .get()
            .build()

        val responseBodyString = try {
            val response = client.newCall(request).execute()
            response.use {
                if (!it.isSuccessful) {
                    return@withContext ResultadoImportacao(
                        success = false,
                        errorMessage = "Erro na resposta do servidor (${it.code})."
                    )
                }
                it.body?.string() ?: ""
            }
        } catch (e: Exception) {
            return@withContext ResultadoImportacao(
                success = false,
                errorMessage = "Falha ao conectar. Verifique a internet e a permissão da planilha."
            )
        }

        if (responseBodyString.isBlank()) {
            return@withContext ResultadoImportacao(
                success = false,
                errorMessage = "Resposta da planilha vazia."
            )
        }

        try {
            val warningsList = mutableListOf<String>()
            val parsedSeriesList = mutableListOf<SheetSeriesData>()

            if (responseBodyString.trim().startsWith("[")) {
                val jsonArr = JSONArray(responseBodyString)
                for (i in 0 until jsonArr.length()) {
                    val sObj = jsonArr.optJSONObject(i) ?: continue
                    val sheetSeries = parseSheetSeriesObject(sObj)
                    if (sheetSeries.tmdbId.isNotBlank()) {
                        parsedSeriesList.add(sheetSeries)
                    }
                }
            } else {
                val rootJson = JSONObject(responseBodyString)
                val isSuccess = rootJson.optBoolean("success", true)

                val warningsArr = rootJson.optJSONArray("warnings")
                if (warningsArr != null) {
                    for (i in 0 until warningsArr.length()) {
                        warningsList.add(warningsArr.getString(i))
                    }
                }

                if (!isSuccess && rootJson.has("error")) {
                    return@withContext ResultadoImportacao(
                        success = false,
                        errorMessage = rootJson.optString("error", "Erro ao importar dados da planilha.")
                    )
                }

                parsedSeriesList.addAll(extractAllSheetSeries(rootJson))
            }

            // Diagnóstico no Logcat
            if (parsedSeriesList.isNotEmpty()) {
                val p = parsedSeriesList[0]
                Log.d("GoogleSheetsImport", "Primeira série recebida: tmdbId=${p.tmdbId}, titulo=${p.title}, ano=${p.releaseYear}, episodiosCount=${p.episodeRows.size}")
            } else {
                Log.d("GoogleSheetsImport", "Nenhuma série válida encontrada no JSON retornado.")
            }

            if (parsedSeriesList.isEmpty()) {
                return@withContext ResultadoImportacao(
                    success = false,
                    errorMessage = "Nenhum dado de série encontrado na planilha."
                )
            }

            val mergeResult = realizarMerge(parsedSeriesList, seriesLocaisAtuais, permanentlyDeletedIds)

            ResultadoImportacao(
                success = true,
                seriesAtualizadasCount = mergeResult.seriesCount,
                episodiosAtualizadosCount = mergeResult.episodiosCount,
                warnings = warningsList,
                novasSeries = mergeResult.seriesList
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ResultadoImportacao(
                success = false,
                errorMessage = "Formato de dados da planilha é inválido."
            )
        }
    }

    private data class SheetSeriesData(
        val tmdbId: String,
        val title: String,
        val releaseYear: Int,
        val imdbId: String,
        val tvdbId: String,
        val status: String,
        val temStatusDaPlanilha: Boolean,
        val fixedOpening: String,
        val fixedRecap: String,
        val fixedEnding: String,
        val episodeRows: List<JSONObject>
    )

    private data class MergeDataResult(
        val seriesList: List<SerieItem>,
        val seriesCount: Int,
        val episodiosCount: Int
    )

    private fun extractAllSheetSeries(rootJson: JSONObject): List<SheetSeriesData> {
        val resultList = mutableListOf<SheetSeriesData>()

        // Opção 1: 'series' é um JSONArray dentro de rootJson
        val seriesObj = rootJson.opt("series")
        val fixedTimingsObj = rootJson.opt("fixedTimings")
        val episodesObj = rootJson.opt("episodes")

        val fixedTimingsMap = extractFixedTimingsMap(fixedTimingsObj)
        val episodesMap = extractEpisodesMap(episodesObj)

        if (seriesObj is JSONArray) {
            for (i in 0 until seriesObj.length()) {
                val sObj = seriesObj.optJSONObject(i) ?: continue
                val parsed = parseSheetSeriesObject(sObj)
                if (parsed.tmdbId.isNotBlank()) {
                    val tmdbId = parsed.tmdbId
                    val fixed = fixedTimingsMap[tmdbId] ?: sObj.optJSONObject("fixedTimings")
                    val eps = episodesMap[tmdbId] ?: extractEpisodesListFromObj(sObj.opt("episodes"))

                    val finalParsed = parsed.copy(
                        fixedOpening = parsed.fixedOpening.ifBlank { parseTimingString(fixed?.opt("opening") ?: fixed?.opt("abertura")) },
                        fixedRecap = parsed.fixedRecap.ifBlank { parseTimingString(fixed?.opt("recap")) },
                        fixedEnding = parsed.fixedEnding.ifBlank { parseTimingString(fixed?.opt("ending") ?: fixed?.opt("encerramento")) },
                        episodeRows = if (eps.isNotEmpty()) eps else parsed.episodeRows
                    )
                    resultList.add(finalParsed)
                }
            }
        } else if (seriesObj is JSONObject) {
            val keys = seriesObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val sObj = seriesObj.optJSONObject(key) ?: continue
                val parsed = parseSheetSeriesObject(sObj)
                val tmdbId = parsed.tmdbId.ifBlank { normalizeTmdbId(key) }
                if (tmdbId.isNotBlank()) {
                    val fixed = fixedTimingsMap[tmdbId] ?: sObj.optJSONObject("fixedTimings")
                    val eps = episodesMap[tmdbId] ?: extractEpisodesListFromObj(sObj.opt("episodes"))

                    val finalParsed = parsed.copy(
                        tmdbId = tmdbId,
                        fixedOpening = parsed.fixedOpening.ifBlank { parseTimingString(fixed?.opt("opening") ?: fixed?.opt("abertura")) },
                        fixedRecap = parsed.fixedRecap.ifBlank { parseTimingString(fixed?.opt("recap")) },
                        fixedEnding = parsed.fixedEnding.ifBlank { parseTimingString(fixed?.opt("ending") ?: fixed?.opt("encerramento")) },
                        episodeRows = if (eps.isNotEmpty()) eps else parsed.episodeRows
                    )
                    resultList.add(finalParsed)
                }
            }
        } else {
            // Se 'series' não existia, tenta pegar do próprio rootJson
            val parsed = parseSheetSeriesObject(rootJson)
            if (parsed.tmdbId.isNotBlank()) {
                val tmdbId = parsed.tmdbId
                val eps = episodesMap[tmdbId] ?: extractEpisodesListFromObj(episodesObj)
                val fixed = fixedTimingsMap[tmdbId] ?: rootJson.optJSONObject("fixedTimings")

                val finalParsed = parsed.copy(
                    fixedOpening = parsed.fixedOpening.ifBlank { parseTimingString(fixed?.opt("opening") ?: fixed?.opt("abertura")) },
                    fixedRecap = parsed.fixedRecap.ifBlank { parseTimingString(fixed?.opt("recap")) },
                    fixedEnding = parsed.fixedEnding.ifBlank { parseTimingString(fixed?.opt("ending") ?: fixed?.opt("encerramento")) },
                    episodeRows = if (eps.isNotEmpty()) eps else parsed.episodeRows
                )
                resultList.add(finalParsed)
            }
        }

        return resultList
    }

    /** TVDB da série: o do topo da aba ou, se não houver, o primeiro preenchido na coluna K dos episódios. */
    private fun tvdbDaPlanilha(serie: SheetSeriesData): String =
        serie.tvdbId.trim().ifBlank {
            serie.episodeRows.asSequence()
                .map { it.optString("tvdbId", "").trim().removePrefix("'") }
                .firstOrNull { it.isNotEmpty() && it.all(Char::isDigit) }
                .orEmpty()
        }

    private fun parseSheetSeriesObject(sObj: JSONObject): SheetSeriesData {
        val tmdbId = normalizeTmdbId(sObj.opt("tmdbId") ?: sObj.opt("id"))
        val title = sObj.optString("title", "").ifBlank { sObj.optString("titulo", "") }.ifBlank { sObj.optString("name", "") }
        val releaseYear = parseYear(sObj.opt("releaseYear") ?: sObj.opt("year") ?: sObj.opt("ano"))
        val imdbId = sObj.optString("imdbId", "")
        val tvdbId = sObj.optString("tvdbId", "")
        val status = sObj.optString("status", "")
        val temStatusDaPlanilha = sObj.optBoolean("statusDaPlanilha", false)

        val fixedObj = sObj.optJSONObject("fixedTimings") ?: sObj.optJSONObject("minutagensFixas")
        val fixedOpening = parseTimingString(fixedObj?.opt("opening") ?: fixedObj?.opt("abertura") ?: sObj.opt("fixedOpening"))
        val fixedRecap = parseTimingString(fixedObj?.opt("recap") ?: sObj.opt("fixedRecap"))
        val fixedEnding = parseTimingString(fixedObj?.opt("ending") ?: fixedObj?.opt("encerramento") ?: sObj.opt("fixedEnding"))

        val episodesList = extractEpisodesListFromObj(sObj.opt("episodes") ?: sObj.opt("episodios"))

        return SheetSeriesData(
            tmdbId = tmdbId,
            title = title,
            releaseYear = releaseYear,
            imdbId = imdbId,
            tvdbId = tvdbId,
            status = status,
            temStatusDaPlanilha = temStatusDaPlanilha,
            fixedOpening = fixedOpening,
            fixedRecap = fixedRecap,
            fixedEnding = fixedEnding,
            episodeRows = episodesList
        )
    }

    private fun realizarMerge(
        parsedSeriesList: List<SheetSeriesData>,
        seriesLocais: List<SerieItem>,
        permanentlyDeletedIds: Set<String> = emptySet()
    ): MergeDataResult {
        val mapSeries = seriesLocais.associateBy { normalizeTmdbId(it.id) }.toMutableMap()

        var seriesAtualizadas = 0
        var episodiosAtualizados = 0

        for (sheetSeries in parsedSeriesList) {
            val tmdbId = normalizeTmdbId(sheetSeries.tmdbId)
            if (tmdbId.isBlank()) continue

            // PROTEÇÃO 1: Se o tmdbId foi excluído definitivamente pelo usuário, IGNORAR completamente durante a importação!
            if (permanentlyDeletedIds.contains(tmdbId)) {
                continue
            }

            val localSerie = mapSeries[tmdbId]

            // PROTEÇÃO 2: Se a série está na Lixeira (isDeleted = true), NÃO restaurar e NÃO criar outro card!
            if (localSerie != null && localSerie.isDeleted) {
                mapSeries[tmdbId] = localSerie
                continue
            }

            seriesAtualizadas++

            var serieToProcess = if (localSerie != null) {
                // Preserva pôster, backdrop, sinopse, gêneros do TMDB local
                val newTitle = sheetSeries.title
                val newYear = sheetSeries.releaseYear
                val newImdb = sheetSeries.imdbId
                val newTvdb = tvdbDaPlanilha(sheetSeries)

                localSerie.copy(
                    titulo = if (newTitle.isNotBlank()) newTitle else localSerie.titulo,
                    ano = if (newYear > 0) newYear else localSerie.ano,
                    status = StatusPlanilha.escolher(localSerie.status, sheetSeries.status, sheetSeries.temStatusDaPlanilha),
                    imdbId = if (newImdb.isNotBlank()) newImdb else localSerie.imdbId,
                    tvdbId = if (newTvdb.isNotBlank()) newTvdb else localSerie.tvdbId
                )
            } else {
                // Nova série provisória
                val newTitle = sheetSeries.title.ifBlank { "Série $tmdbId" }
                val newYear = sheetSeries.releaseYear
                val newImdb = sheetSeries.imdbId
                val newTvdb = tvdbDaPlanilha(sheetSeries)

                SerieItem(
                    id = tmdbId,
                    titulo = newTitle,
                    ano = newYear,
                    generos = "Importada",
                    status = StatusPlanilha.escolher("Em andamento", sheetSeries.status, sheetSeries.temStatusDaPlanilha),
                    episodiosAssistidos = 0,
                    totalEpisodios = 0,
                    proximoEpisodio = "S01E01",
                    imageUrl = "",
                    posterUrl = "",
                    sinopse = "Série importada via planilha. Aguardando atualização de metadados pelo TMDB.",
                    identificadores = if (newImdb.isNotBlank() && newTvdb.isNotBlank()) "IMDb • TVDB" else if (newImdb.isNotBlank()) "IMDb" else if (newTvdb.isNotBlank()) "TVDB" else "TMDB",
                    imdbId = newImdb,
                    tvdbId = newTvdb,
                    aberturaAtiva = false,
                    recapAtivo = false,
                    encerramentoAtivo = false,
                    minutagensAtivas = "0 de 3 ativas"
                )
            }

            // Merge das minutagens fixas
            val opening = sheetSeries.fixedOpening
            val recap = sheetSeries.fixedRecap
            val ending = sheetSeries.fixedEnding

            if (opening.isNotBlank()) {
                serieToProcess = serieToProcess.copy(duracaoFixaAbertura = opening, aberturaAtiva = true)
            }
            if (recap.isNotBlank()) {
                serieToProcess = serieToProcess.copy(duracaoFixaRecap = recap, recapAtivo = true)
            }
            if (ending.isNotBlank()) {
                serieToProcess = serieToProcess.copy(duracaoFixaEncerramento = ending, encerramentoAtivo = true)
            }

            val ativasCount = (if (serieToProcess.aberturaAtiva) 1 else 0) + (if (serieToProcess.recapAtivo) 1 else 0) + (if (serieToProcess.encerramentoAtivo) 1 else 0)
            serieToProcess = serieToProcess.copy(minutagensAtivas = "$ativasCount de 3 ativas")

            // Merge dos episódios
            val temporadasMutaveis = serieToProcess.temporadas.toMutableList()

            for (epRow in sheetSeries.episodeRows) {
                val seasonNum = parseNumber(epRow.opt("season") ?: epRow.opt("temporada") ?: epRow.opt("seasonNumber")) ?: continue
                val episodeNum = parseNumber(epRow.opt("number") ?: epRow.opt("episode") ?: epRow.opt("episodio") ?: epRow.opt("episodeNumber")) ?: continue
                val epTitle = epRow.optString("title", "").ifBlank { epRow.optString("titulo", "") }.ifBlank { epRow.optString("name", "") }

                val seasonFormatted = "T" + if (seasonNum < 10) "0$seasonNum" else "$seasonNum"

                var tempIdx = temporadasMutaveis.indexOfFirst {
                    parseNumber(it.numero) == seasonNum || it.id.endsWith("_s$seasonNum")
                }

                if (tempIdx < 0) {
                    val novaTemp = TemporadaItem(
                        id = "${tmdbId}_s$seasonNum",
                        numero = seasonFormatted,
                        nome = "Temporada $seasonNum",
                        totalEpisodios = 0,
                        episodiosConcluidos = 0,
                        status = "Não iniciada",
                        episodios = emptyList()
                    )
                    temporadasMutaveis.add(novaTemp)
                    tempIdx = temporadasMutaveis.lastIndex
                }

                val tempAtual = temporadasMutaveis[tempIdx]
                val episMutaveis = tempAtual.episodios.toMutableList()

                val epIdx = episMutaveis.indexOfFirst { it.numero == episodeNum }
                val localEp = if (epIdx >= 0) episMutaveis[epIdx] else null

                val rawOpening = epRow.opt("opening") ?: epRow.opt("abertura")
                val rawRecap = epRow.opt("recap")
                val rawEnding = epRow.opt("ending") ?: epRow.opt("encerramento")

                val currentMinutagens = localEp?.minutagens ?: listOf(
                    MinutagemSecao(tipo = TipoMinutagem.ABERTURA, estado = EstadoMinutagem.PENDENTE),
                    MinutagemSecao(tipo = TipoMinutagem.RECAP, estado = EstadoMinutagem.PENDENTE),
                    MinutagemSecao(tipo = TipoMinutagem.ENCERRAMENTO, estado = EstadoMinutagem.PENDENTE)
                )

                val localAbertura = currentMinutagens.firstOrNull { it.tipo == TipoMinutagem.ABERTURA }
                val localRecap = currentMinutagens.firstOrNull { it.tipo == TipoMinutagem.RECAP }
                val localEncerramento = currentMinutagens.firstOrNull { it.tipo == TipoMinutagem.ENCERRAMENTO }

                val mergedAbertura = mergeMinutagemSecao(localAbertura, TipoMinutagem.ABERTURA, rawOpening)
                val mergedRecap = mergeMinutagemSecao(localRecap, TipoMinutagem.RECAP, rawRecap)
                val mergedEncerramento = mergeMinutagemSecao(localEncerramento, TipoMinutagem.ENCERRAMENTO, rawEnding)

                val novasMinutagens = listOf(mergedAbertura, mergedRecap, mergedEncerramento)

                val tempEp = EpisodioItem(
                    id = "${tmdbId}_s${seasonNum}_ep_$episodeNum",
                    numero = episodeNum,
                    minutagens = novasMinutagens
                )
                val novoStatusEp = SerieProgressoUtils.calcularStatusEpisodio(tempEp)

                val tituloEpFinal = if (epTitle.isNotBlank()) epTitle else (localEp?.titulo ?: "Episódio $episodeNum")

                val epMerged = localEp?.copy(
                    titulo = tituloEpFinal,
                    minutagens = novasMinutagens,
                    status = novoStatusEp
                ) ?: EpisodioItem(
                    id = "${tmdbId}_s${seasonNum}_ep_$episodeNum",
                    numero = episodeNum,
                    titulo = tituloEpFinal,
                    status = novoStatusEp,
                    dataExibicao = "",
                    minutagens = novasMinutagens
                )

                if (epIdx >= 0) {
                    episMutaveis[epIdx] = epMerged
                } else {
                    episMutaveis.add(epMerged)
                }

                episodiosAtualizados++

                // Ordena episódios numericamente
                temporadasMutaveis[tempIdx] = tempAtual.copy(
                    episodios = episMutaveis.sortedBy { it.numero },
                    totalEpisodios = episMutaveis.size
                )
            }

            // Ordena temporadas numericamente
            val temporadasOrdenadas = temporadasMutaveis.sortedBy { parseNumber(it.numero) ?: 1 }

            val serieFinal = SerieProgressoUtils.recalcularProgressoSerie(serieToProcess.copy(temporadas = temporadasOrdenadas))
            val minutagensChanged = hasMinutagemChanges(localSerie, serieFinal)
            val finalLastUpdatedAt = if (minutagensChanged) {
                System.currentTimeMillis()
            } else {
                localSerie?.lastUpdatedAt
            }
            mapSeries[tmdbId] = serieFinal.copy(lastUpdatedAt = finalLastUpdatedAt)
        }

        return MergeDataResult(
            seriesList = mapSeries.values.toList(),
            seriesCount = seriesAtualizadas,
            episodiosCount = episodiosAtualizados
        )
    }

    private fun hasMinutagemChanges(local: SerieItem?, merged: SerieItem): Boolean {
        if (local == null) {
            return merged.aberturaAtiva || merged.recapAtivo || merged.encerramentoAtivo ||
                    merged.duracaoFixaAbertura.isNotBlank() || merged.duracaoFixaRecap.isNotBlank() || merged.duracaoFixaEncerramento.isNotBlank() ||
                    merged.temporadas.any { temp ->
                        temp.episodios.any { ep ->
                            ep.minutagens.any { min ->
                                min.estado != EstadoMinutagem.PENDENTE || min.tempoInicio.isNotBlank() || min.tempoFim.isNotBlank()
                            }
                        }
                    }
        }

        if (local.aberturaAtiva != merged.aberturaAtiva ||
            local.recapAtivo != merged.recapAtivo ||
            local.encerramentoAtivo != merged.encerramentoAtivo ||
            local.duracaoFixaAbertura != merged.duracaoFixaAbertura ||
            local.duracaoFixaRecap != merged.duracaoFixaRecap ||
            local.duracaoFixaEncerramento != merged.duracaoFixaEncerramento
        ) {
            return true
        }

        val tempMapLocal = local.temporadas.associateBy { it.id }
        val tempMapMerged = merged.temporadas.associateBy { it.id }

        for ((tempId, tempMerged) in tempMapMerged) {
            val tempLocal = tempMapLocal[tempId] ?: return true
            val epMapLocal = tempLocal.episodios.associateBy { it.id }
            val epMapMerged = tempMerged.episodios.associateBy { it.id }

            for ((epId, epMerged) in epMapMerged) {
                val epLocal = epMapLocal[epId]
                if (epLocal == null || epLocal.minutagens != epMerged.minutagens) {
                    return true
                }
            }
        }

        return false
    }

    internal fun mergeMinutagemSecao(
        localSecao: MinutagemSecao?,
        tipo: TipoMinutagem,
        cellRaw: Any?
    ): MinutagemSecao {
        val local = SerieProgressoUtils.normalizarSecao(localSecao ?: MinutagemSecao(tipo))
        if (cellRaw == null || cellRaw == JSONObject.NULL) return local
        fun texto(raw: Any?): String = when (raw) {
            null, JSONObject.NULL -> ""
            is JSONObject, is JSONArray -> ""
            else -> normalizeTimeString(SerieProgressoUtils.normalizarCampo(raw.toString()))
        }
        fun campo(obj: JSONObject, vararg nomes: String): String = nomes.asSequence()
            .map { texto(obj.opt(it)) }.firstOrNull { it.isNotBlank() }.orEmpty()
        val inicio: String
        val fim: String
        val confirmada: Boolean
        val naoPossui: Boolean
        if (cellRaw is JSONObject) {
            inicio = campo(cellRaw, "start", "inicio", "startTime", "tempoInicio")
            fim = campo(cellRaw, "end", "fim", "endTime", "tempoFim")
            confirmada = cellRaw.optBoolean("confirmadaIndividualmente", false)
            naoPossui = cellRaw.optString("estado") == "NAO_POSSUI" ||
                campo(cellRaw, "durationFormatted") == "Não possui" ||
                (confirmada && cellRaw.optBoolean("hasNot", false))
        } else {
            val times = parseStartAndEndTime(texto(cellRaw))
            inicio = texto(times.first)
            fim = texto(times.second)
            confirmada = MinutagemSecao.parseMmSsToSeconds(inicio) != null &&
                MinutagemSecao.parseMmSsToSeconds(fim) != null
            naoPossui = false
        }
        if (naoPossui || inicio == "Não possui" || fim == "Não possui") {
            return MinutagemSecao(tipo, EstadoMinutagem.NAO_POSSUI, confirmadaIndividualmente = true)
        }
        val incoming = SerieProgressoUtils.normalizarSecao(MinutagemSecao(
            tipo = tipo, tempoInicio = inicio, tempoFim = fim, confirmadaIndividualmente = confirmada
        ))
        if (incoming.tempoInicio.isBlank() && incoming.tempoFim.isBlank() && !confirmada) return local
        return SerieProgressoUtils.normalizarSecao(incoming.copy(
            tempoInicio = incoming.tempoInicio.ifBlank { local.tempoInicio },
            tempoFim = incoming.tempoFim.ifBlank { local.tempoFim }
        ))
    }
    private fun parseTimingString(raw: Any?): String {
        if (raw == null || raw == JSONObject.NULL) return ""
        if (raw is JSONObject) {
            val dur = raw.optString("duration", "").ifBlank { raw.optString("duracao", "") }.ifBlank { raw.optString("start", "") }
            val cleanDur = normalizeTimeString(dur)
            if (cleanDur.equals("Não possui", ignoreCase = true)) return "Não possui"
            return cleanDur
        }
        val str = raw.toString().trim()
        val cleanStr = normalizeTimeString(str)
        if (cleanStr.equals("Não possui", ignoreCase = true)) return "Não possui"
        return cleanStr
    }

    private fun normalizeTimeString(raw: String): String {
        if (raw.isBlank()) return ""
        var text = raw.trim()
        if (text.equals("Não possui", ignoreCase = true) || text.equals("Nao possui", ignoreCase = true)) {
            return "Não possui"
        }

        if (text.startsWith("'")) {
            text = text.substring(1).trim()
        }

        val threePartsMatch = Regex("^(\\d+):(\\d{2}):00$").find(text)
        if (threePartsMatch != null) {
            val p1 = threePartsMatch.groupValues[1].padStart(2, '0')
            val p2 = threePartsMatch.groupValues[2]
            return "$p1:$p2"
        }

        val twoPartsMatch = Regex("^(\\d+):(\\d{2})$").find(text)
        if (twoPartsMatch != null) {
            val p1 = twoPartsMatch.groupValues[1].padStart(2, '0')
            val p2 = twoPartsMatch.groupValues[2]
            return "$p1:$p2"
        }

        return text
    }

    private fun parseStartAndEndTime(raw: String): Pair<String, String> {
        val cleaned = raw.trim()
        val separators = listOf(" – ", " - ", " a ", " até ", ";", ",")
        for (sep in separators) {
            if (cleaned.contains(sep)) {
                val parts = cleaned.split(sep)
                if (parts.size >= 2) {
                    return Pair(normalizeTimeString(parts[0]), normalizeTimeString(parts[1]))
                }
            }
        }
        return Pair(normalizeTimeString(cleaned), "")
    }

    private fun normalizeTmdbId(raw: Any?): String {
        if (raw == null || raw == JSONObject.NULL) return ""
        val str = raw.toString().trim()
        if (str.isBlank()) return ""
        if (str.endsWith(".0")) {
            return str.substring(0, str.length - 2)
        }
        val asDouble = str.toDoubleOrNull()
        if (asDouble != null && asDouble == asDouble.toLong().toDouble()) {
            return asDouble.toLong().toString()
        }
        return str
    }

    private fun parseYear(raw: Any?): Int {
        if (raw == null || raw == JSONObject.NULL) return 0
        val str = raw.toString().trim()
        if (str.isBlank()) return 0
        val digits = str.take(4).filter { it.isDigit() }
        return digits.toIntOrNull() ?: 0
    }

    private fun parseNumber(raw: Any?): Int? {
        if (raw == null || raw == JSONObject.NULL) return null
        val str = raw.toString().filter { it.isDigit() }
        return str.toIntOrNull()
    }

    private fun extractFixedTimingsMap(rawObj: Any?): Map<String, JSONObject> {
        val result = mutableMapOf<String, JSONObject>()
        if (rawObj is JSONArray) {
            for (i in 0 until rawObj.length()) {
                val item = rawObj.optJSONObject(i) ?: continue
                val id = normalizeTmdbId(item.opt("tmdbId") ?: item.opt("id"))
                if (id.isNotBlank()) result[id] = item
            }
        } else if (rawObj is JSONObject) {
            val keys = rawObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = rawObj.optJSONObject(key) ?: continue
                val id = normalizeTmdbId(item.opt("tmdbId") ?: key)
                if (id.isNotBlank()) result[id] = item
            }
        }
        return result
    }

    private fun extractEpisodesMap(rawObj: Any?): Map<String, List<JSONObject>> {
        val result = mutableMapOf<String, MutableList<JSONObject>>()
        if (rawObj is JSONArray) {
            for (i in 0 until rawObj.length()) {
                val item = rawObj.optJSONObject(i) ?: continue
                val id = normalizeTmdbId(item.opt("tmdbId") ?: item.opt("id"))
                if (id.isNotBlank()) {
                    result.getOrPut(id) { mutableListOf() }.add(item)
                }
            }
        } else if (rawObj is JSONObject) {
            val keys = rawObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val valObj = rawObj.opt(key)
                if (valObj is JSONArray) {
                    for (i in 0 until valObj.length()) {
                        val item = valObj.optJSONObject(i) ?: continue
                        val id = normalizeTmdbId(item.opt("tmdbId") ?: key)
                        if (id.isNotBlank()) {
                            result.getOrPut(id) { mutableListOf() }.add(item)
                        }
                    }
                } else if (valObj is JSONObject) {
                    val id = normalizeTmdbId(valObj.opt("tmdbId") ?: key)
                    if (id.isNotBlank()) {
                        result.getOrPut(id) { mutableListOf() }.add(valObj)
                    }
                }
            }
        }
        return result
    }

    private fun extractEpisodesListFromObj(rawObj: Any?): List<JSONObject> {
        val result = mutableListOf<JSONObject>()
        if (rawObj is JSONArray) {
            for (i in 0 until rawObj.length()) {
                val item = rawObj.optJSONObject(i) ?: continue
                result.add(item)
            }
        } else if (rawObj is JSONObject) {
            result.add(rawObj)
        }
        return result
    }
}
