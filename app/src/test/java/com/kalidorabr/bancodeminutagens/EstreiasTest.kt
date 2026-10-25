package com.kalidorabr.bancodeminutagens

import com.kalidorabr.bancodeminutagens.data.Estreias
import com.kalidorabr.bancodeminutagens.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class EstreiasTest {
    private fun serie(id: String, status: String, data: String) = SerieItem(
        id = id, titulo = id, ano = 2026, generos = "", status = status, episodiosAssistidos = 0, totalEpisodios = 1,
        proximoEpisodio = "", imageUrl = "",
        temporadas = listOf(TemporadaItem("${id}_s1", "T01", "Temporada 1", 1, 0, episodios = listOf(EpisodioItem("${id}_e1", 1, dataExibicao = data))))
    )

    @Test fun soEmBreveQueEstreiaHoje() {
        val hoje = LocalDate.of(2026, 10, 25)
        val r = Estreias.deHoje(listOf(
            serie("a", "Em breve", "25/10/2026"),
            serie("b", "Em breve", "26/10/2026"),
            serie("c", "Em andamento", "25/10/2026")
        ), hoje)
        assertEquals(listOf("a"), r.map { it.first.id })
    }
}
