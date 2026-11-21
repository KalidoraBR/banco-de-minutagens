package com.kalidorabr.bancodeminutagens

import com.kalidorabr.bancodeminutagens.model.EpisodioItem
import com.kalidorabr.bancodeminutagens.model.EstadoMinutagem
import com.kalidorabr.bancodeminutagens.model.MinutagemSecao
import com.kalidorabr.bancodeminutagens.model.SerieProgressoUtils
import com.kalidorabr.bancodeminutagens.model.StatusEpisodio
import com.kalidorabr.bancodeminutagens.model.TipoMinutagem
import org.junit.Assert.assertEquals
import org.junit.Test

class ConcluirEpisodioTest {
    private val completas = listOf(
        MinutagemSecao(tipo = TipoMinutagem.ABERTURA, estado = EstadoMinutagem.PREENCHIDO, tempoInicio = "00:10", tempoFim = "01:00"),
        MinutagemSecao(tipo = TipoMinutagem.RECAP, estado = EstadoMinutagem.NAO_POSSUI),
        MinutagemSecao(tipo = TipoMinutagem.ENCERRAMENTO, estado = EstadoMinutagem.PREENCHIDO, tempoInicio = "40:00", tempoFim = "42:00")
    )

    @Test
    fun completoEsperandoConcluirFicaParcial() {
        val ep = EpisodioItem(id = "e1", numero = 1, minutagens = completas, aguardandoConcluir = true)
        assertEquals(StatusEpisodio.PARCIAL, SerieProgressoUtils.calcularStatusEpisodio(ep))
    }

    @Test
    fun completoDepoisDoConcluirFicaConcluido() {
        val ep = EpisodioItem(id = "e1", numero = 1, minutagens = completas, aguardandoConcluir = false)
        assertEquals(StatusEpisodio.CONCLUIDO, SerieProgressoUtils.calcularStatusEpisodio(ep))
    }

    @Test
    fun episodioAntigoSemMarcaContinuaConcluido() {
        // episódios salvos antes desta versão (ou vindos da planilha) não têm a marca
        val ep = EpisodioItem(id = "e1", numero = 1, minutagens = completas)
        assertEquals(StatusEpisodio.CONCLUIDO, SerieProgressoUtils.calcularStatusEpisodio(ep))
    }
}
