package com.kalidorabr.bancodeminutagens

import com.kalidorabr.bancodeminutagens.data.IntroJsonExporter
import com.kalidorabr.bancodeminutagens.model.EpisodioItem
import com.kalidorabr.bancodeminutagens.model.EstadoMinutagem
import com.kalidorabr.bancodeminutagens.model.MinutagemSecao
import com.kalidorabr.bancodeminutagens.model.SerieItem
import com.kalidorabr.bancodeminutagens.model.TemporadaItem
import com.kalidorabr.bancodeminutagens.model.TipoMinutagem
import org.junit.Assert.assertEquals
import org.junit.Test

class IntroJsonExporterTest {
    @Test
    fun exportaSomenteMinutagensPreenchidasNoFormatoDoIntro() {
        val serie = SerieItem(
            id = "903747", titulo = "Teste", ano = 2024, generos = "",
            episodiosAssistidos = 0, totalEpisodios = 1, proximoEpisodio = "",
            imageUrl = "", imdbId = "tt0903747", tvdbId = "81189"
        )
        val temporada = TemporadaItem(
            id = "s1", numero = "T01", nome = "Temporada 1", totalEpisodios = 1,
            episodiosConcluidos = 0,
            episodios = listOf(
                EpisodioItem(
                    id = "e1", numero = 1,
                    minutagens = listOf(
                        MinutagemSecao(TipoMinutagem.ABERTURA, EstadoMinutagem.PREENCHIDO, "00:02", "00:58"),
                        MinutagemSecao(TipoMinutagem.RECAP, EstadoMinutagem.NAO_POSSUI),
                        MinutagemSecao(TipoMinutagem.ENCERRAMENTO, EstadoMinutagem.PENDENTE)
                    )
                )
            )
        )

        val expected = listOf(
            "{",
            "  \"items\": [",
            "    {",
            "      \"imdb_id\": \"tt0903747\",",
            "      \"segment_type\": \"intro\",",
            "      \"season\": 1,",
            "      \"episode\": 1,",
            "      \"start_sec\": 2,",
            "      \"end_sec\": 58,",
            "      \"tvdb_id\": 81189",
            "    }",
            "  ]",
            "}"
        ).joinToString("\r\n")
        assertEquals(expected, IntroJsonExporter.exportar(serie, temporada))
    }
}
