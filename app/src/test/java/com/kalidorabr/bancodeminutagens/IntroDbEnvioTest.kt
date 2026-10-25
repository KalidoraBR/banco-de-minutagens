package com.kalidorabr.bancodeminutagens

import com.kalidorabr.bancodeminutagens.model.*
import org.junit.Assert.*
import org.junit.Test

class IntroDbEnvioTest {
    private fun s(t: TipoMinutagem, a: String, b: String) =
        MinutagemSecao(tipo = t, estado = EstadoMinutagem.PREENCHIDO, tempoInicio = a, tempoFim = b)

    @Test fun enviaSoSecoesComTempoEDentroDosLimites() {
        val eps = listOf(
            EpisodioItem("e2", 2, minutagens = listOf(
                s(TipoMinutagem.ABERTURA, "00:10", "01:40"),
                MinutagemSecao(TipoMinutagem.RECAP, EstadoMinutagem.NAO_POSSUI),
                s(TipoMinutagem.ENCERRAMENTO, "20:00", "21:30")
            )),
            EpisodioItem("e1", 1, minutagens = listOf(
                s(TipoMinutagem.ABERTURA, "00:00", "04:00"),   // 4 min: acima de 3 min
                MinutagemSecao(TipoMinutagem.RECAP)            // vazio
            ))
        )
        val plano = IntroDbEnvio.planejar(1, eps)
        assertEquals(listOf("intro", "outro"), plano.envios.map { it.segmentType })
        assertEquals(listOf(10, 1200), plano.envios.map { it.inicioSeg })
        assertEquals(listOf(100, 1290), plano.envios.map { it.fimSeg })
        assertTrue(plano.envios.all { it.temporada == 1 && it.episodio == 2 })
        assertEquals(1, plano.pulados.size)
        assertEquals("T01E01 · Abertura", plano.pulados.single().rotulo)
    }
}

class ImportarIntroDbTest {
    @Test fun preencheSoAsVaziasEFicaEditavel() {
        val atual = listOf(
            MinutagemSecao(TipoMinutagem.ABERTURA, EstadoMinutagem.PREENCHIDO, "00:05", "01:35"),
            MinutagemSecao(TipoMinutagem.RECAP),
            MinutagemSecao(TipoMinutagem.ENCERRAMENTO)
        )
        val tempos = TemposIntroDb(
            secoes = mapOf(TipoMinutagem.ABERTURA to (126 to 214), TipoMinutagem.ENCERRAMENTO to (1300 to 1387)),
            naoPossui = setOf(TipoMinutagem.RECAP)
        )
        val r = ImportarIntroDb.aplicar(atual, tempos)
        assertEquals("00:05", r[0].tempoInicio)                       // a sua abertura não muda
        assertEquals(EstadoMinutagem.NAO_POSSUI, r[1].estado)
        assertEquals(EstadoMinutagem.PREENCHIDO, r[2].estado)        // vira minutagem comum
        assertEquals("21:40", r[2].tempoInicio)
        assertEquals("23:07", r[2].tempoFim)
        assertTrue(ImportarIntroDb.ajuda(atual, tempos))
        assertFalse(ImportarIntroDb.ajuda(r, tempos))                 // depois de usar, não oferece de novo
    }
}
