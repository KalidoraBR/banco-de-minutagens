package com.kalidorabr.bancodeminutagens.data

import android.content.Context
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class SeriesRepository(context: Context) {

    private val file = File(context.filesDir, FILE_NAME)
    private val tempFile = File(context.filesDir, TEMP_FILE_NAME)

    suspend fun carregarSeries(): List<SerieItem> = withContext(Dispatchers.IO) {
        if (!file.exists() || file.length() == 0L) {
            return@withContext emptyList()
        }

        try {
            val content = file.readText(Charsets.UTF_8)
            if (content.isBlank()) return@withContext emptyList()

            val jsonArray = JSONArray(content)
            val seriesList = mutableListOf<SerieItem>()
            for (i in 0 until jsonArray.length()) {
                val sObj = jsonArray.optJSONObject(i) ?: continue
                try {
                    val parsed = jsonToSerie(sObj)
                    val recalcular = SerieProgressoUtils.recalcularProgressoSerie(parsed)
                    seriesList.add(recalcular)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            seriesList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun salvarSeries(seriesList: List<SerieItem>) = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray()
            for (serie in seriesList) {
                jsonArray.put(serieToJson(serie))
            }
            val jsonString = jsonArray.toString(2)

            // Gravação segura usando arquivo temporário antes de substituir o arquivo principal
            tempFile.writeText(jsonString, Charsets.UTF_8)

            if (tempFile.exists() && tempFile.length() > 0L) {
                if (file.exists()) {
                    file.delete()
                }
                tempFile.renameTo(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun criarBackup(seriesList: List<SerieItem>): String {
        val root = JSONObject()
        root.put("format", "banco-de-minutagens-backup")
        root.put("version", 1)
        root.put("createdAt", System.currentTimeMillis())
        val data = JSONArray()
        seriesList.forEach { data.put(serieToJson(it)) }
        root.put("series", data)
        return root.toString(2)
    }

    fun lerBackup(content: String): List<SerieItem> {
        val root = JSONObject(content)
        require(root.optString("format") == "banco-de-minutagens-backup") { "Arquivo de backup inválido" }
        val data = root.getJSONArray("series")
        return List(data.length()) { index ->
            SerieProgressoUtils.recalcularProgressoSerie(jsonToSerie(data.getJSONObject(index)))
        }
    }

    private fun serieToJson(serie: SerieItem): JSONObject {
        val json = JSONObject()
        json.put("id", serie.id)
        json.put("titulo", serie.titulo)
        json.put("ano", serie.ano)
        json.put("generos", serie.generos)
        json.put("status", serie.status)
        json.put("episodiosAssistidos", serie.episodiosAssistidos)
        json.put("totalEpisodios", serie.totalEpisodios)
        json.put("proximoEpisodio", serie.proximoEpisodio)
        json.put("imageUrl", serie.imageUrl)
        json.put("posterUrl", serie.posterUrl)
        json.put("sinopse", serie.sinopse)
        json.put("identificadores", serie.identificadores)
        json.put("imdbId", serie.imdbId)
        json.put("tvdbId", serie.tvdbId)
        json.put("minutagensAtivas", serie.minutagensAtivas)
        json.put("aberturaAtiva", serie.aberturaAtiva)
        json.put("recapAtivo", serie.recapAtivo)
        json.put("encerramentoAtivo", serie.encerramentoAtivo)
        json.put("duracaoFixaAbertura", serie.duracaoFixaAbertura)
        json.put("duracaoFixaRecap", serie.duracaoFixaRecap)
        json.put("duracaoFixaEncerramento", serie.duracaoFixaEncerramento)
        json.put("isDeleted", serie.isDeleted)
        if (serie.deletedAt != null) {
            json.put("deletedAt", serie.deletedAt)
        }
        if (serie.lastUpdatedAt != null) {
            json.put("lastUpdatedAt", serie.lastUpdatedAt)
        }
        if (serie.lastEditedEpisodeId != null) {
            json.put("lastEditedEpisodeId", serie.lastEditedEpisodeId)
        }

        val temporadasArr = JSONArray()
        for (temp in serie.temporadas) {
            temporadasArr.put(temporadaToJson(temp))
        }
        json.put("temporadas", temporadasArr)

        return json
    }

    private fun temporadaToJson(temp: TemporadaItem): JSONObject {
        val json = JSONObject()
        json.put("id", temp.id)
        json.put("numero", temp.numero)
        json.put("nome", temp.nome)
        json.put("totalEpisodios", temp.totalEpisodios)
        json.put("episodiosConcluidos", temp.episodiosConcluidos)
        json.put("status", temp.status)
        json.put("imageUrl", temp.imageUrl)

        val episodiosArr = JSONArray()
        for (ep in temp.episodios) {
            episodiosArr.put(episodioToJson(ep))
        }
        json.put("episodios", episodiosArr)

        return json
    }

    private fun episodioToJson(ep: EpisodioItem): JSONObject {
        val json = JSONObject()
        json.put("id", ep.id)
        json.put("numero", ep.numero)
        json.put("titulo", ep.titulo)
        json.put("status", ep.status.name)
        json.put("dataExibicao", ep.dataExibicao)
        if (ep.duracaoMin > 0) json.put("duracaoMin", ep.duracaoMin)

        val minutagensArr = JSONArray()
        for (min in ep.minutagens) {
            minutagensArr.put(minutagemToJson(min))
        }
        json.put("minutagens", minutagensArr)

        return json
    }

    private fun minutagemToJson(min: MinutagemSecao): JSONObject {
        val json = JSONObject()
        json.put("tipo", min.tipo.name)
        json.put("estado", min.estado.name)
        json.put("tempoInicio", min.tempoInicio)
        json.put("tempoFim", min.tempoFim)
        if (min.confirmadaIndividualmente) json.put("confirmadaIndividualmente", true)
        return json
    }

    private fun jsonToSerie(json: JSONObject): SerieItem {
        val temporadasArr = json.optJSONArray("temporadas")
        val temporadasList = mutableListOf<TemporadaItem>()
        if (temporadasArr != null) {
            for (i in 0 until temporadasArr.length()) {
                val tObj = temporadasArr.getJSONObject(i)
                temporadasList.add(jsonToTemporada(tObj))
            }
        }

        val deletedAtVal = if (json.has("deletedAt") && !json.isNull("deletedAt")) json.optLong("deletedAt") else null
        val lastUpdatedAtVal = if (json.has("lastUpdatedAt") && !json.isNull("lastUpdatedAt")) json.optLong("lastUpdatedAt") else null
        val lastEditedEpisodeIdVal = if (json.has("lastEditedEpisodeId") && !json.isNull("lastEditedEpisodeId")) json.optString("lastEditedEpisodeId") else null

        val aberturaAtivaVal = json.optBoolean("aberturaAtiva", false)
        val recapAtivoVal = json.optBoolean("recapAtivo", false)
        val encerramentoAtivoVal = json.optBoolean("encerramentoAtivo", false)
        val ativasCount = (if (aberturaAtivaVal) 1 else 0) + (if (recapAtivoVal) 1 else 0) + (if (encerramentoAtivoVal) 1 else 0)
        val defaultMinutagensAtivas = "$ativasCount de 3 ativas"

        val minutagensAtivasVal = json.optString("minutagensAtivas", defaultMinutagensAtivas)
            .ifBlank { defaultMinutagensAtivas }

        return SerieItem(
            id = json.optString("id", ""),
            titulo = json.optString("titulo", ""),
            ano = json.optInt("ano", 0),
            generos = json.optString("generos", ""),
            status = json.optString("status", "Em andamento"),
            episodiosAssistidos = json.optInt("episodiosAssistidos", 0),
            totalEpisodios = json.optInt("totalEpisodios", 0),
            proximoEpisodio = json.optString("proximoEpisodio", "S01E01"),
            imageUrl = json.optString("imageUrl", ""),
            posterUrl = json.optString("posterUrl", ""),
            sinopse = json.optString("sinopse", ""),
            identificadores = json.optString("identificadores", "IMDb • TVDB"),
            imdbId = json.optString("imdbId", ""),
            tvdbId = json.optString("tvdbId", ""),
            minutagensAtivas = minutagensAtivasVal,
            aberturaAtiva = aberturaAtivaVal,
            recapAtivo = recapAtivoVal,
            encerramentoAtivo = encerramentoAtivoVal,
            duracaoFixaAbertura = json.optString("duracaoFixaAbertura", ""),
            duracaoFixaRecap = json.optString("duracaoFixaRecap", ""),
            duracaoFixaEncerramento = json.optString("duracaoFixaEncerramento", ""),
            isDeleted = json.optBoolean("isDeleted", false),
            deletedAt = deletedAtVal,
            lastUpdatedAt = lastUpdatedAtVal,
            lastEditedEpisodeId = lastEditedEpisodeIdVal,
            temporadas = temporadasList,
        )
    }

    private fun jsonToTemporada(json: JSONObject): TemporadaItem {
        val episodiosArr = json.optJSONArray("episodios")
        val episodiosList = mutableListOf<EpisodioItem>()
        if (episodiosArr != null) {
            for (i in 0 until episodiosArr.length()) {
                val eObj = episodiosArr.getJSONObject(i)
                episodiosList.add(jsonToEpisodio(eObj))
            }
        }

        return TemporadaItem(
            id = json.optString("id", ""),
            numero = json.optString("numero", ""),
            nome = json.optString("nome", ""),
            totalEpisodios = json.optInt("totalEpisodios", 0),
            episodiosConcluidos = json.optInt("episodiosConcluidos", 0),
            status = json.optString("status", "Não iniciada"),
            imageUrl = json.optString("imageUrl", ""),
            episodios = episodiosList
        )
    }

    private fun jsonToEpisodio(json: JSONObject): EpisodioItem {
        val minutagensArr = json.optJSONArray("minutagens")
        val minutagensList = mutableListOf<MinutagemSecao>()
        if (minutagensArr != null) {
            for (i in 0 until minutagensArr.length()) {
                val mObj = minutagensArr.optJSONObject(i) ?: continue
                minutagensList.add(jsonToMinutagem(mObj))
            }
        }

        val statusStr = json.optString("status", "PENDENTE")
        val statusEnum = try {
            StatusEpisodio.valueOf(statusStr)
        } catch (_: Exception) {
            StatusEpisodio.PENDENTE
        }

        return EpisodioItem(
            id = json.optString("id", ""),
            numero = json.optInt("numero", 1),
            titulo = json.optString("titulo", ""),
            status = statusEnum,
            dataExibicao = json.optString("dataExibicao", ""),
            duracaoMin = json.optInt("duracaoMin", 0),
            minutagens = minutagensList
        )
    }

    private fun jsonToMinutagem(json: JSONObject): MinutagemSecao {
        val tipoStr = json.optString("tipo", "ABERTURA")
        val tipoEnum = try {
            TipoMinutagem.valueOf(tipoStr)
        } catch (_: Exception) {
            TipoMinutagem.ABERTURA
        }

        val estadoStr = json.optString("estado", "PENDENTE")
        val estadoEnum = try {
            EstadoMinutagem.valueOf(estadoStr)
        } catch (_: Exception) {
            EstadoMinutagem.PENDENTE
        }

        return MinutagemSecao(
            tipo = tipoEnum,
            estado = estadoEnum,
            tempoInicio = SerieProgressoUtils.normalizarCampo(json.optString("tempoInicio", "")),
            tempoFim = SerieProgressoUtils.normalizarCampo(json.optString("tempoFim", "")),
            confirmadaIndividualmente = json.optBoolean("confirmadaIndividualmente", false)
        )
    }

    companion object {
        private const val FILE_NAME = "banco_minutagens_series.json"
        private const val TEMP_FILE_NAME = "banco_minutagens_series.json.tmp"
    }
}
