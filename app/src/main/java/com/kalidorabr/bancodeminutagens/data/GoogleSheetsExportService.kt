package com.kalidorabr.bancodeminutagens.data

import com.kalidorabr.bancodeminutagens.model.EstadoMinutagem
import com.kalidorabr.bancodeminutagens.model.MinutagemSecao
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.model.TipoMinutagem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

data class ResultadoExportacao(
    val success: Boolean,
    val seriesExportadasCount: Int = 0,
    val episodiosExportadosCount: Int = 0,
    val warnings: List<String> = emptyList(),
    val errorMessage: String? = null
)

data class ResultadoClearEpisodes(
    val success: Boolean,
    val requestedEpisodes: Int = 0,
    val foundEpisodes: Int = 0,
    val clearedEpisodes: Int = 0,
    val verifiedEmptyCells: Int = 0,
    val sheetName: String? = null,
    val errorMessage: String? = null
)

object GoogleSheetsExportService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .callTimeout(150, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun executarExportacao(
        scriptUrl: String,
        spreadsheetId: String,
        seriesLocais: List<SerieItem>
    ): ResultadoExportacao = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim()
        val cleanId = spreadsheetId.trim()

        if (cleanUrl.isBlank() || cleanId.isBlank()) {
            return@withContext ResultadoExportacao(
                success = false,
                errorMessage = "ID da planilha ou URL do Apps Script não informados."
            )
        }

        if (seriesLocais.isEmpty()) {
            return@withContext ResultadoExportacao(
                success = false,
                errorMessage = "Nenhuma série na coleção para exportar."
            )
        }

        val requestUrl = try {
            cleanUrl.toHttpUrl()
        } catch (e: Exception) {
            return@withContext ResultadoExportacao(
                success = false,
                errorMessage = "URL do Apps Script inválida."
            )
        }

        val payloadJson = try {
            buildExportPayload(cleanId, seriesLocais)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext ResultadoExportacao(
                success = false,
                errorMessage = "Erro ao estruturar os dados para exportação."
            )
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = payloadJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(requestUrl)
            .post(body)
            .build()

        val responseBodyString = try {
            val response = client.newCall(request).execute()
            response.use {
                if (!it.isSuccessful) {
                    return@withContext ResultadoExportacao(
                        success = false,
                        errorMessage = "Erro na resposta do servidor (${it.code})."
                    )
                }
                it.body?.string() ?: ""
            }
        } catch (e: Exception) {
            return@withContext ResultadoExportacao(
                success = false,
                errorMessage = when (e) {
                    is SocketTimeoutException ->
                        "A planilha recebeu a solicitação, mas demorou para confirmar. A sincronização continuará pendente."
                    is UnknownHostException ->
                        "Sem acesso ao servidor. Verifique a conexão com a internet."
                    else ->
                        "Falha de conexão (${e.javaClass.simpleName}). Tente novamente."
                }
            )
        }

        if (responseBodyString.isBlank()) {
            return@withContext ResultadoExportacao(
                success = false,
                errorMessage = "Resposta da planilha vazia."
            )
        }

        try {
            val rootJson = JSONObject(responseBodyString)
            val explicitError = rootJson.optString("error", "").trim()
            val successValue = rootJson.opt("success")
            val statusValue = rootJson.optString("status", "").trim()
            val isSuccess = when (successValue) {
                is Boolean -> successValue
                is String -> successValue.equals("true", ignoreCase = true) ||
                    successValue.equals("success", ignoreCase = true) ||
                    successValue.equals("ok", ignoreCase = true)
                else -> if (statusValue.isNotBlank()) {
                    statusValue.equals("success", ignoreCase = true) ||
                        statusValue.equals("ok", ignoreCase = true)
                } else {
                    explicitError.isBlank()
                }
            }

            val warningsList = mutableListOf<String>()
            val warningsArr = rootJson.optJSONArray("warnings")
            if (warningsArr != null) {
                for (i in 0 until warningsArr.length()) {
                    warningsList.add(warningsArr.getString(i))
                }
            }

            if (!isSuccess) {
                val errorMsg = explicitError.ifBlank { "Erro ao exportar dados para a planilha." }
                return@withContext ResultadoExportacao(
                    success = false,
                    warnings = warningsList,
                    errorMessage = errorMsg
                )
            }

            val seriesExportadas = rootJson.optInt("seriesCount", seriesLocais.size)
            val totalEpisodios = seriesLocais.sumOf { s -> s.temporadas.sumOf { t -> t.episodios.size } }
            val episodiosExportados = rootJson.optInt("episodesCount", totalEpisodios)

            ResultadoExportacao(
                success = true,
                seriesExportadasCount = seriesExportadas,
                episodiosExportadosCount = episodiosExportados,
                warnings = warningsList
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ResultadoExportacao(
                success = false,
                errorMessage = "Resposta do servidor com formato inválido."
            )
        }
    }

    suspend fun executarClearEpisodes(
        scriptUrl: String,
        spreadsheetId: String,
        pendingClearItem: PendingClearItem
    ): ResultadoClearEpisodes = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim()
        val cleanId = spreadsheetId.trim()

        if (cleanUrl.isBlank() || cleanId.isBlank()) {
            return@withContext ResultadoClearEpisodes(
                success = false,
                errorMessage = "ID da planilha ou URL do Apps Script não informados."
            )
        }

        val requestUrl = try {
            cleanUrl.toHttpUrl()
        } catch (e: Exception) {
            return@withContext ResultadoClearEpisodes(
                success = false,
                errorMessage = "URL do Apps Script inválida."
            )
        }

        val payloadJson = JSONObject()
        payloadJson.put("action", "clearEpisodes")
        payloadJson.put("spreadsheetId", cleanId)
        payloadJson.put("tmdbId", pendingClearItem.tmdbId)
        payloadJson.put("seriesTitle", pendingClearItem.seriesTitle)

        val episodesArr = JSONArray()
        for (ep in pendingClearItem.episodes) {
            val epObj = JSONObject()
            epObj.put("season", ep.season)
            epObj.put("seasonNumber", ep.seasonNumber)
            epObj.put("episode", ep.episode)
            epObj.put("episodeNumber", ep.episodeNumber)
            epObj.put("title", ep.title)
            episodesArr.put(epObj)
        }
        payloadJson.put("episodes", episodesArr)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = payloadJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(requestUrl)
            .post(body)
            .build()

        val responseBodyString = try {
            val response = client.newCall(request).execute()
            response.use {
                if (!it.isSuccessful) {
                    return@withContext ResultadoClearEpisodes(
                        success = false,
                        errorMessage = "Erro na resposta do servidor (${it.code})."
                    )
                }
                it.body?.string() ?: ""
            }
        } catch (e: Exception) {
            return@withContext ResultadoClearEpisodes(
                success = false,
                errorMessage = "Falha de conexão ao enviar a limpeza."
            )
        }

        if (responseBodyString.isBlank()) {
            return@withContext ResultadoClearEpisodes(
                success = false,
                errorMessage = "Resposta do Apps Script vazia."
            )
        }

        try {
            val rootJson = JSONObject(responseBodyString)
            val isSuccess = rootJson.optBoolean("success", false)
            val actionReceived = rootJson.optString("action", "")
            val requestedEp = rootJson.optInt("requestedEpisodes", pendingClearItem.episodes.size)
            val foundEp = rootJson.optInt("foundEpisodes", 0)
            val clearedEp = rootJson.optInt("clearedEpisodes", 0)
            val verifiedCells = if (rootJson.has("verifiedEmptyCells")) rootJson.optInt("verifiedEmptyCells") else rootJson.optInt("clearedCells", 0)
            val sheetName = rootJson.optString("sheetName", "")
            val message = rootJson.optString("message", "")

            val expectedCount = pendingClearItem.episodes.size
            val isValidValidation = isSuccess &&
                    actionReceived.equals("clearEpisodes", ignoreCase = true) &&
                    requestedEp == expectedCount &&
                    foundEp == expectedCount &&
                    clearedEp == expectedCount &&
                    verifiedCells == (expectedCount * 6)

            if (isValidValidation) {
                ResultadoClearEpisodes(
                    success = true,
                    requestedEpisodes = requestedEp,
                    foundEpisodes = foundEp,
                    clearedEpisodes = clearedEp,
                    verifiedEmptyCells = verifiedCells,
                    sheetName = sheetName
                )
            } else {
                val errorMsg = if (message.isNotBlank()) message else "Sincronização incompleta: $foundEp de $expectedCount episódios limpos e confirmados na planilha."
                ResultadoClearEpisodes(
                    success = false,
                    requestedEpisodes = requestedEp,
                    foundEpisodes = foundEp,
                    clearedEpisodes = clearedEp,
                    verifiedEmptyCells = verifiedCells,
                    sheetName = sheetName,
                    errorMessage = errorMsg
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ResultadoClearEpisodes(
                success = false,
                errorMessage = "Formato de resposta do servidor inválido."
            )
        }
    }

    private fun buildExportPayload(spreadsheetId: String, seriesLocais: List<SerieItem>): JSONObject {
        val root = JSONObject()
        root.put("action", "export_to_sheets")
        root.put("spreadsheetId", spreadsheetId)

        val seriesArr = JSONArray()
        val seriesParaExportar = seriesLocais.filter { !it.isDeleted }

        for (serie in seriesParaExportar) {
            val sObj = JSONObject()
            val tmdbIdNormalized = normalizeTmdbId(serie.id)

            sObj.put("id", serie.id)
            sObj.put("title", serie.titulo)
            sObj.put("seriesTitle", serie.titulo)
            sObj.put("tmdbId", tmdbIdNormalized)
            sObj.put("releaseYear", serie.ano)
            sObj.put("imdbId", serie.imdbId)
            sObj.put("tvdbId", serie.tvdbId)
            // selo da série: o script grava na F2
            sObj.put("status", serie.status)

            // Objeto de Minutagens Fixas
            val fixedTimingsObj = JSONObject()
            fixedTimingsObj.put("opening", serie.duracaoFixaAbertura)
            fixedTimingsObj.put("recap", serie.duracaoFixaRecap)
            fixedTimingsObj.put("ending", serie.duracaoFixaEncerramento)
            sObj.put("fixedTimings", fixedTimingsObj)

            // Array de Temporadas
            val seasonsArr = JSONArray()
            for (temp in serie.temporadas) {
                val tempObj = JSONObject()
                val seasonNum = parseNumber(temp.numero) ?: 1
                tempObj.put("seasonNumber", seasonNum)
                tempObj.put("title", temp.nome)

                // Array de Episódios
                val episodesArr = JSONArray()
                for (ep in temp.episodios) {
                    val epObj = JSONObject()
                    epObj.put("episodeNumber", ep.numero)
                    epObj.put("title", ep.titulo)
                    // códigos da série em cada episódio (colunas J e K); vazio não apaga o que está na planilha
                    epObj.put("imdbId", serie.imdbId.trim().takeIf { it.startsWith("tt") }.orEmpty())
                    epObj.put("tvdbId", serie.tvdbId.trim().takeIf { it.isNotEmpty() && it.all(Char::isDigit) }.orEmpty())

                    val localAbertura = ep.minutagens.firstOrNull { it.tipo == TipoMinutagem.ABERTURA }
                    val localRecap = ep.minutagens.firstOrNull { it.tipo == TipoMinutagem.RECAP }
                    val localEncerramento = ep.minutagens.firstOrNull { it.tipo == TipoMinutagem.ENCERRAMENTO }

                    epObj.put("opening", serializeMinutagemSecao(localAbertura))
                    epObj.put("recap", serializeMinutagemSecao(localRecap))
                    epObj.put("ending", serializeMinutagemSecao(localEncerramento))

                    episodesArr.put(epObj)
                }

                tempObj.put("episodes", episodesArr)
                seasonsArr.put(tempObj)
            }

            sObj.put("seasons", seasonsArr)
            seriesArr.put(sObj)
        }

        root.put("series", seriesArr)
        return root
    }

    private fun serializeMinutagemSecao(min: MinutagemSecao?): JSONObject {
        val obj = JSONObject()
        if (min == null) {
            obj.put("isFilled", false)
            obj.put("hasNot", false)
            obj.put("startTime", "")
            obj.put("endTime", "")
            obj.put("durationFormatted", "")
            return obj
        }

        when (min.estado) {
            EstadoMinutagem.PREENCHIDO -> {
                obj.put("isFilled", true)
                obj.put("hasNot", false)
                obj.put("startTime", min.tempoInicio)
                obj.put("endTime", min.tempoFim)
                obj.put("durationFormatted", min.duracaoFormatada)
            }
            EstadoMinutagem.NAO_POSSUI -> {
                obj.put("isFilled", true)
                obj.put("hasNot", true)
                obj.put("startTime", "")
                obj.put("endTime", "")
                obj.put("durationFormatted", "Não possui")
            }
            EstadoMinutagem.PENDENTE -> {
                obj.put("isFilled", false)
                obj.put("hasNot", false)
                obj.put("startTime", "")
                obj.put("endTime", "")
                obj.put("durationFormatted", "")
            }
        }

        return obj
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

    private fun parseNumber(raw: Any?): Int? {
        if (raw == null || raw == JSONObject.NULL) return null
        val str = raw.toString().filter { it.isDigit() }
        return str.toIntOrNull()
    }
}
