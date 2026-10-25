package com.kalidorabr.bancodeminutagens

import com.kalidorabr.bancodeminutagens.model.*
import org.junit.Assert.*
import org.junit.Test

class ProgressoRegressionTest {
    @Test fun detectaIntervaloInvertidoEDuracaoIncomum() {
        val invertido = MinutagemSecao(TipoMinutagem.ABERTURA, EstadoMinutagem.PREENCHIDO, "05:00", "04:00")
        val longo = MinutagemSecao(TipoMinutagem.RECAP, EstadoMinutagem.PREENCHIDO, "00:00", "16:00")
        assertTrue(SerieProgressoUtils.inconsistencias(invertido).contains("Fim anterior ao início"))
        assertTrue(SerieProgressoUtils.inconsistencias(longo).contains("Duração superior a 15 minutos"))
        assertTrue(SerieProgressoUtils.inconsistencias(MinutagemSecao(TipoMinutagem.ENCERRAMENTO)).isEmpty())
    }

    @Test fun listaLocalExatoDaInconsistencia() {
        val secao = MinutagemSecao(TipoMinutagem.ABERTURA, EstadoMinutagem.PREENCHIDO, "05:00", "04:00")
        val episodio = EpisodioItem("ep-3", 3, "Teste", minutagens = listOf(secao))
        val temporada = TemporadaItem("temp-2", "T02", "Temporada 2", 1, 0, episodios = listOf(episodio))
        val serie = SerieItem("serie-1", "Série teste", 2026, "", episodiosAssistidos = 0,
            totalEpisodios = 1, proximoEpisodio = "T02E03", imageUrl = "", temporadas = listOf(temporada))

        val issue = SerieProgressoUtils.listarInconsistencias(listOf(serie)).single()

        assertEquals("serie-1", issue.serieId)
        assertEquals("temp-2", issue.temporadaId)
        assertEquals("ep-3", issue.episodioId)
        assertEquals(TipoMinutagem.ABERTURA, issue.tipo)
        assertTrue(issue.mensagens.contains("Fim anterior ao início"))
    }
    private fun completa() = TipoMinutagem.entries.map {
        MinutagemSecao(it, EstadoMinutagem.PREENCHIDO, "00:00", "01:30")
    }

    private fun serie(tamanhos: List<Int>, preenchidos: Set<Pair<Int, Int>> = emptySet()): SerieItem = SerieItem(
        id = "31654", titulo = "Digimon", ano = 1999, generos = "", episodiosAssistidos = 99,
        totalEpisodios = 999, proximoEpisodio = "T01E01", imageUrl = "",
        aberturaAtiva = true, recapAtivo = true, encerramentoAtivo = true,
        duracaoFixaAbertura = "01:30", lastEditedEpisodeId = "31654_s1_ep_1",
        temporadas = tamanhos.mapIndexed { index, total ->
            val season = index + 1
            TemporadaItem("31654_s$season", "T${season.toString().padStart(2, '0')}", "Temporada $season",
                total, 99, episodios = (1..total).map { numero ->
                    EpisodioItem("31654_s${season}_ep_$numero", numero, status = StatusEpisodio.PARCIAL,
                        minutagens = if (season to numero in preenchidos) completa() else TipoMinutagem.entries.map {
                            MinutagemSecao(it, EstadoMinutagem.PREENCHIDO, "null", "00:00", isFilled = true)
                        })
                })
        }
    )

    @Test fun digimon104ContinuarT02E10EAtualizarAposSalvar() {
        val serie = SerieProgressoUtils.recalcularProgressoSerie(serie(listOf(54, 50), setOf(2 to 9)))
        assertEquals(104, serie.totalEpisodios)
        assertEquals(1, serie.episodiosAssistidos)
        assertEquals(1, serie.progressoPorcentagem)
        assertEquals(1f / 104f, serie.progressoFloat, 0.000001f)
        val destino = SerieProgressoUtils.encontrarProximoEpisodio(serie)!!
        assertEquals(DestinoEpisodio("31654", 2, 10), destino)
        assertEquals("T02E10", serie.proximoEpisodio)
        val aberto = SerieProgressoUtils.localizarEpisodio(listOf(serie), destino)!!
        assertEquals("T02", aberto.second.numero)
        assertEquals("31654_s2_ep_10", aberto.third.id)
        val salva = serie.copy(temporadas = serie.temporadas.map { temp ->
            if (temp.id != aberto.second.id) temp else temp.copy(episodios = temp.episodios.map {
                if (it.id == aberto.third.id) it.copy(minutagens = completa()) else it
            })
        })
        assertEquals("T02E11", SerieProgressoUtils.recalcularProgressoSerie(salva).proximoEpisodio)
    }

    @Test fun temporada53SomenteEp05A08Concluidos() {
        val serie = SerieProgressoUtils.recalcularProgressoSerie(serie(listOf(53), (5..8).map { 1 to it }.toSet()))
        val temp = serie.temporadas.single()
        assertEquals(4, temp.episodiosConcluidos)
        assertEquals(8, temp.progressoPorcentagem)
        assertEquals(8, serie.progressoPorcentagem)
        assertEquals(49, temp.episodios.count { it.status == StatusEpisodio.PENDENTE })
        assertTrue(temp.episodios.none { it.status == StatusEpisodio.PARCIAL })
    }

    @Test fun semHistoricoMesmoComConfiguracaoFixaGlobal() {
        val serie = SerieProgressoUtils.recalcularProgressoSerie(serie(listOf(54, 50)))
        assertEquals(0, serie.episodiosAssistidos)
        assertEquals(0, serie.progressoPorcentagem)
        assertTrue(serie.temporadas.flatMap { it.episodios }.all { it.status == StatusEpisodio.PENDENTE })
        assertEquals("T01E01", serie.proximoEpisodio)
        assertEquals(serie, SerieProgressoUtils.recalcularProgressoSerie(serie))
    }

    @Test fun numerosRepetidosNuncaPerdemATemporada() {
        val serie = serie(listOf(54, 50))
        for (season in 1..2) {
            val destino = DestinoEpisodio("31654", season, 10)
            val ep = SerieProgressoUtils.localizarEpisodio(listOf(serie), destino)!!
            assertEquals("31654_s${season}_ep_10", ep.third.id)
        }
        assertNull(SerieProgressoUtils.localizarEpisodio(listOf(serie), DestinoEpisodio("31654", 3, 10)))
        assertNull(SerieProgressoUtils.localizarEpisodio(listOf(serie), DestinoEpisodio("outra", 2, 10)))
        assertNull(SerieProgressoUtils.localizarEpisodio(listOf(serie), DestinoEpisodio("31654", 2, 999)))
    }

    @Test fun seisCamposVaziosFlagsPadraoEHorariosInvalidos() {
        for (vazio in listOf("", " ", "null", "{}", "0", "00:00")) {
            val secoes = TipoMinutagem.entries.map { MinutagemSecao(it, tempoInicio = vazio, tempoFim = vazio, isFilled = true, hasNot = true) }
            assertEquals(StatusEpisodio.PENDENTE, SerieProgressoUtils.calcularStatusEpisodio(EpisodioItem("ep", 1, minutagens = secoes)))
        }
        for (tipo in TipoMinutagem.entries) for (inicio in listOf(true, false)) {
            val secao = MinutagemSecao(tipo, tempoInicio = if (inicio) "01:00" else "", tempoFim = if (inicio) "" else "02:00")
            assertEquals(StatusEpisodio.PARCIAL, SerieProgressoUtils.calcularStatusEpisodio(EpisodioItem("ep", 1, minutagens = listOf(secao))))
        }
        assertFalse(SerieProgressoUtils.isSecaoConcluida(MinutagemSecao(TipoMinutagem.ABERTURA, tempoInicio = "02:00", tempoFim = "01:00")))
        assertFalse(SerieProgressoUtils.isSecaoConcluida(MinutagemSecao(TipoMinutagem.ABERTURA, tempoInicio = "00:99", tempoFim = "03:00")))
        assertTrue(SerieProgressoUtils.isSecaoConcluida(MinutagemSecao(TipoMinutagem.ABERTURA, tempoInicio = "00:00", tempoFim = "00:00", confirmadaIndividualmente = true)))
        assertTrue(SerieProgressoUtils.isSecaoConcluida(MinutagemSecao(TipoMinutagem.RECAP, EstadoMinutagem.NAO_POSSUI)))
        assertEquals(0f, SerieProgressoUtils.progresso(1, 0))
        assertEquals(1f, SerieProgressoUtils.progresso(5, 2))
    }
}
