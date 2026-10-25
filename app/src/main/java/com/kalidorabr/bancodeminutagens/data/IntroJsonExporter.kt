package com.kalidorabr.bancodeminutagens.data

import com.kalidorabr.bancodeminutagens.model.EstadoMinutagem
import com.kalidorabr.bancodeminutagens.model.MinutagemSecao
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.model.SerieProgressoUtils
import com.kalidorabr.bancodeminutagens.model.TemporadaItem
import com.kalidorabr.bancodeminutagens.model.TipoMinutagem

object IntroJsonExporter {
    fun exportar(serie: SerieItem, temporada: TemporadaItem): String {
        val season = SerieProgressoUtils.numeroTemporada(temporada.numero) ?: 1
        val items = temporada.episodios
            .sortedBy { it.numero }
            .flatMap { episodio ->
                episodio.minutagens.mapNotNull { secao ->
                    criarItem(serie, season, episodio.numero, secao)
                }
            }

        return buildString {
            append("{\r\n  \"items\": [")
            if (items.isNotEmpty()) append("\r\n")
            items.forEachIndexed { index, item ->
                append(item)
                if (index < items.lastIndex) append(',')
                append("\r\n")
            }
            append("  ]\r\n}")
        }
    }

    private fun criarItem(
        serie: SerieItem,
        season: Int,
        episode: Int,
        secao: MinutagemSecao
    ): String? {
        if (secao.estado != EstadoMinutagem.PREENCHIDO) return null
        val start = MinutagemSecao.parseMmSsToSeconds(secao.tempoInicio) ?: return null
        val end = MinutagemSecao.parseMmSsToSeconds(secao.tempoFim) ?: return null
        if (end < start) return null

        val segmentType = when (secao.tipo) {
            TipoMinutagem.ABERTURA -> "intro"
            TipoMinutagem.RECAP -> "recap"
            TipoMinutagem.ENCERRAMENTO -> "outro"
        }

        val fields = mutableListOf<String>()
        if (serie.imdbId.isNotBlank()) fields += "\"imdb_id\": \"${escape(serie.imdbId)}\""
        fields += "\"segment_type\": \"$segmentType\""
        fields += "\"season\": $season"
        fields += "\"episode\": $episode"
        fields += "\"start_sec\": $start"
        fields += "\"end_sec\": $end"
        if (serie.tvdbId.isNotBlank()) {
            val tvdb = serie.tvdbId.trim()
            fields += if (tvdb.all(Char::isDigit)) "\"tvdb_id\": $tvdb" else "\"tvdb_id\": \"${escape(tvdb)}\""
        }
        return buildString {
            append("    {\r\n")
            fields.forEachIndexed { index, field ->
                append("      ")
                append(field)
                if (index < fields.lastIndex) append(',')
                append("\r\n")
            }
            append("    }")
        }
    }

    private fun escape(value: String): String = buildString {
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
    }
}
