package com.kalidorabr.bancodeminutagens

import com.kalidorabr.bancodeminutagens.model.*
import org.junit.Assert.*
import org.junit.Test

class NovidadesTest {
    private fun secao(tipo: TipoMinutagem, ini: String, fim: String) =
        MinutagemSecao(tipo = tipo, estado = EstadoMinutagem.PREENCHIDO, tempoInicio = ini, tempoFim = fim)

    private fun ep(n: Int, abertura: Pair<String, String>? = null, completo: Boolean = false) = EpisodioItem(
        id = "s_ep_$n", numero = n,
        minutagens = if (completo) listOf(
            secao(TipoMinutagem.ABERTURA, "00:10", "01:40"),
            MinutagemSecao(TipoMinutagem.RECAP, EstadoMinutagem.NAO_POSSUI),
            secao(TipoMinutagem.ENCERRAMENTO, "20:00", "21:30")
        ) else listOfNotNull(abertura?.let { secao(TipoMinutagem.ABERTURA, it.first, it.second) })
    )

    private fun serie(vararg eps: EpisodioItem) = SerieProgressoUtils.recalcularProgressoSerie(
        SerieItem(
            id = "s", titulo = "Série", ano = 2020, generos = "", episodiosAssistidos = 0, totalEpisodios = 0,
            proximoEpisodio = "", imageUrl = "",
            temporadas = listOf(TemporadaItem("s_s1", "T01", "Temporada 1", eps.size, 0, episodios = eps.toList()))
        )
    )

    @Test fun duracaoTipicaPrecisaDeTresAmostras() {
        val poucas = serie(ep(1, "00:10" to "01:40"), ep(2, "00:10" to "01:40"))
        assertNull(AvisoTempo.duracaoTipica(poucas, TipoMinutagem.ABERTURA))
        val varias = serie(ep(1, "00:10" to "01:40"), ep(2, "00:05" to "01:35"), ep(3, "00:00" to "01:30"), ep(4, "00:00" to "09:00"))
        assertEquals(90, AvisoTempo.duracaoTipica(varias, TipoMinutagem.ABERTURA, excetoEpisodioId = "s_ep_4"))
    }

    @Test fun avisaQuandoFogeMuitoDoNormal() {
        assertNotNull(AvisoTempo.verificar(TipoMinutagem.ABERTURA, 45, 555, 90))   // 8min30s contra 1min30s
        assertNotNull(AvisoTempo.verificar(TipoMinutagem.ABERTURA, 45, 60, 90))    // 15s contra 1min30s
        assertNull(AvisoTempo.verificar(TipoMinutagem.ABERTURA, 45, 140, 90))      // 1min35s: normal
        assertNull(AvisoTempo.verificar(TipoMinutagem.RECAP, 0, 40, 15))           // diferença menor que 30s
    }

    @Test fun semReferenciaSoAvisaTempoMuitoLongo() {
        assertNull(AvisoTempo.verificar(TipoMinutagem.ABERTURA, 0, 240, null))
        assertNotNull(AvisoTempo.verificar(TipoMinutagem.ABERTURA, 0, 360, null))
    }

    @Test fun avisaEncerramentoAntesDaAbertura() {
        val outras = listOf(secao(TipoMinutagem.ABERTURA, "01:00", "02:30"))
        assertNotNull(AvisoTempo.verificar(TipoMinutagem.ENCERRAMENTO, 120, 200, null, outras))
        assertNull(AvisoTempo.verificar(TipoMinutagem.ENCERRAMENTO, 1200, 1290, null, outras))
    }

    @Test fun naoAvisaTempoInvalido() {
        assertNull(AvisoTempo.verificar(TipoMinutagem.ABERTURA, null, 90, 90))
        assertNull(AvisoTempo.verificar(TipoMinutagem.ABERTURA, 100, 50, 90))
    }

    @Test fun adicionarEpisodiosMantemMinutagensEOrdem() {
        val original = serie(ep(1, completo = true), ep(2))
        val novos = listOf(
            TemporadaItem("s_s1", "T01", "Temporada 1", 1, 0, episodios = listOf(EpisodioItem("s_ep_3", 3), EpisodioItem("s_ep_2", 2))),
            TemporadaItem("s_s2", "T02", "Temporada 2", 1, 0, episodios = listOf(EpisodioItem("s2_ep_1", 1)))
        )
        val nova = Novidades.adicionarEpisodios(original, novos)
        assertEquals(listOf("s_s1", "s_s2"), nova.temporadas.map { it.id })
        assertEquals(listOf(1, 2, 3), nova.temporadas[0].episodios.map { it.numero })
        assertEquals(original.temporadas[0].episodios[0].minutagens, nova.temporadas[0].episodios[0].minutagens)
        assertEquals(4, nova.totalEpisodios)
        assertEquals(1, nova.episodiosAssistidos)
    }

    @Test fun lembreteDeBackup() {
        val dia = Novidades.DIA_MS
        assertNull(Novidades.diasParaLembrete(6 * dia, 0, 0, true))
        assertEquals(7, Novidades.diasParaLembrete(7 * dia, 0, 0, true))
        assertNull(Novidades.diasParaLembrete(9 * dia, 0, 10 * dia, true))  // adiado
        assertNull(Novidades.diasParaLembrete(9 * dia, 0, 0, false))        // sem séries
    }

    @Test fun comemoraSoQuandoAcabaDeCompletar() {
        val quase = serie(ep(1, completo = true), ep(2))
        val completa = serie(ep(1, completo = true), ep(2, completo = true))
        assertTrue(Novidades.acabouDeCompletar(quase, completa))
        assertFalse(Novidades.acabouDeCompletar(completa, completa))
        assertFalse(Novidades.acabouDeCompletar(null, completa))
    }

    @Test fun estatisticas() {
        val s = serie(ep(1, completo = true), ep(2))
        val hoje = 100L
        val est = Estatisticas.calcular(listOf(s), mapOf("a" to 100L, "b" to 99L, "c" to 99L, "d" to 97L), hoje)
        assertEquals(listOf(0, 0, 0, 1, 0, 2, 1), est.semana)
        assertEquals(2, est.diasSeguidos)
        assertEquals(1, est.episodiosConcluidos)
        assertEquals(90, est.aberturaMedia)
        assertEquals(1, est.faltando.single().faltam)
    }

    @Test fun continuarVoltaNoParcialEditadoPeloUsuario() {
        val parcial = EpisodioItem("s_ep_2", 2, minutagens = listOf(
            secao(TipoMinutagem.ABERTURA, "00:10", "01:40"),
            MinutagemSecao(TipoMinutagem.RECAP, EstadoMinutagem.NAO_POSSUI),
            MinutagemSecao(TipoMinutagem.ENCERRAMENTO)
        ))
        val base = serie(ep(1, completo = true), parcial, ep(3))
        // editado no app: volta no E02
        assertEquals(2, SerieProgressoUtils.encontrarProximoEpisodio(base.copy(lastEditedEpisodeId = "s_ep_2"))?.episodeNumber)
        // parcial que não foi editado no app (ex.: veio da sincronização): segue para o E03
        assertEquals(3, SerieProgressoUtils.encontrarProximoEpisodio(base.copy(lastEditedEpisodeId = null))?.episodeNumber)
        // último editado já completo: segue normal
        assertEquals(3, SerieProgressoUtils.encontrarProximoEpisodio(base.copy(lastEditedEpisodeId = "s_ep_1"))?.episodeNumber)
    }
}
