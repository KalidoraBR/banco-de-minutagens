package com.kalidorabr.bancodeminutagens

import com.kalidorabr.bancodeminutagens.model.*
import com.kalidorabr.bancodeminutagens.ui.temaa.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class TemaARegrasTest {
    @Test fun digitacaoDaDireitaParaEsquerda() {
        assertEquals("00:02", formatarTempoTemaA("2"))
        assertEquals("00:20", formatarTempoTemaA("20"))
        assertEquals("01:30", formatarTempoTemaA("130"))
        assertEquals("12:34", formatarTempoTemaA("1234"))
        assertEquals(90, segundosTempoTemaA("130"))
        assertNull(segundosTempoTemaA(""))
    }

    @Test fun teclasDoTeclado() {
        var d = ""
        listOf("1", "3", "0").forEach { d = teclaTempoTemaA(d, it) }
        assertEquals("130", d)
        assertEquals("13", teclaTempoTemaA(d, "Apagar"))
        assertEquals("", teclaTempoTemaA(d, "Limpar"))
        assertEquals("1234", teclaTempoTemaA("1234", "5")) // no máximo 4 dígitos
        assertEquals("", teclaTempoTemaA("", "0"))       // zero à esquerda não conta
    }

    @Test fun segundosAcimaDe59SaoInvalidos() {
        assertFalse(segundosValidosTemaA("175"))
        assertTrue(segundosValidosTemaA("159"))
    }

    @Test fun carregaTempoSalvoSemZerosAEsquerda() {
        assertEquals("214", digitosTempoTemaA("02:14"))
        assertEquals("02:14", formatarTempoTemaA(digitosTempoTemaA("02:14")))
    }

    private fun serie(datas: List<String>, temporadaNumero: String = "1") = SerieItem(
        "s", "Teste", 2026, "", episodiosAssistidos = 0, totalEpisodios = datas.size,
        proximoEpisodio = "", imageUrl = "", status = "Em breve",
        temporadas = listOf(
            TemporadaItem("t", temporadaNumero, "Temporada $temporadaNumero", datas.size, 0,
                episodios = datas.mapIndexed { i, d -> EpisodioItem("e$i", i + 1, dataExibicao = d) })
        )
    )

    @Test fun estreiaEhOPrimeiroEpisodio() {
        assertEquals(LocalDate.of(2026, 3, 1), estreiaSerieTemaA(serie(listOf("08/03/2026", "01/03/2026"))))
    }

    @Test fun estreiaIgnoraEspeciaisEDatasVazias() {
        assertNull(estreiaSerieTemaA(serie(listOf("01/01/2020"), temporadaNumero = "0")))
        assertNull(estreiaSerieTemaA(serie(listOf("", "sem data"))))
    }

    @Test fun continuarPegaUltimaSerieSalvaComProximoEpisodio() {
        fun s(id: String, quando: Long, feito: Boolean) = SerieItem(
            id, id, 2026, "", episodiosAssistidos = 0, totalEpisodios = 1, proximoEpisodio = "", imageUrl = "",
            lastUpdatedAt = quando,
            temporadas = listOf(TemporadaItem("t$id", "1", "T1", 1, 0, episodios = listOf(
                EpisodioItem("e$id", 1, minutagens = TipoMinutagem.entries.map {
                    MinutagemSecao(it, if (feito) EstadoMinutagem.NAO_POSSUI else EstadoMinutagem.PENDENTE)
                })
            )))
        )
        // A mais recente já foi concluída: pula para a próxima em andamento
        val resultado = encontrarContinuarTemaA(listOf(s("antiga", 1, false), s("recente", 2, true)))
        assertEquals("antiga", resultado?.first?.id)
    }

    @Test fun liteFoiRemovidoEQuemUsavaAbreNoTemaA() {
        val V = com.kalidorabr.bancodeminutagens.data.VisualMode.Companion
        assertEquals(listOf("TEMA_A", "TEMA_B", "TEMA_C", "TEMA_D"), V.disponiveis.map { it.id })
        assertEquals(com.kalidorabr.bancodeminutagens.data.VisualMode.TEMA_A, V.fromId("LITE"))
        assertEquals(com.kalidorabr.bancodeminutagens.data.VisualMode.TEMA_A, V.fromId(null))
        assertEquals(com.kalidorabr.bancodeminutagens.data.VisualMode.TEMA_C, V.fromId("TEMA_C"))
    }

    // ---- Status automático: Finalizada / Incompleta ----
    private fun ep(n: Int, vararg estados: EstadoMinutagem) = EpisodioItem("e$n", n, minutagens =
        TipoMinutagem.entries.mapIndexed { i, t ->
            val e = estados.getOrElse(i) { EstadoMinutagem.PENDENTE }
            if (e == EstadoMinutagem.PREENCHIDO) MinutagemSecao(t, e, "00:10", "01:00") else MinutagemSecao(t, e)
        })
    private val P = EstadoMinutagem.PREENCHIDO
    private val N = EstadoMinutagem.NAO_POSSUI
    private val V = EstadoMinutagem.PENDENTE
    private fun serieCom(vararg eps: EpisodioItem, status: String = "Em andamento") = SerieItem(
        "s", "S", 2026, "", status = status, episodiosAssistidos = 0, totalEpisodios = eps.size,
        proximoEpisodio = "", imageUrl = "",
        temporadas = listOf(TemporadaItem("t1", "1", "Temporada 1", eps.size, 0, episodios = eps.toList())))

    @Test fun tudoPreenchidoViraFinalizadaENaoPossuiConta() {
        val s = serieCom(ep(1, P, N, P), ep(2, N, N, N))
        assertEquals("Finalizada", SerieProgressoUtils.aplicarStatusAutomatico(s).status)
    }

    @Test fun chegouAoFimComLacunaViraIncompleta() {
        val s = serieCom(ep(1, P, V, P), ep(2, P, N, P))
        val r = SerieProgressoUtils.aplicarStatusAutomatico(s)
        assertEquals("Incompleta", r.status)
        assertTrue(r.isIncompleta)
    }

    @Test fun episodioDoMeioEmBrancoTambemDeixaIncompleta() {
        val s = serieCom(ep(1, P, N, P), ep(2), ep(3, P, N, P))
        assertEquals("Incompleta", SerieProgressoUtils.aplicarStatusAutomatico(s).status)
    }

    @Test fun antesDoFimNaoMudaEVoltaParaEmAndamentoSeSaiuDoFim() {
        val emAndamento = serieCom(ep(1, P, N, P), ep(2))
        assertEquals("Em andamento", SerieProgressoUtils.aplicarStatusAutomatico(emAndamento).status)
        // estava Finalizada e o último episódio foi limpo
        val limpou = serieCom(ep(1, P, N, P), ep(2), status = "Finalizada")
        assertEquals("Em andamento", SerieProgressoUtils.aplicarStatusAutomatico(limpou).status)
        // "Em breve" sem fim alcançado continua "Em breve"
        val emBreve = serieCom(ep(1), ep(2), status = "Em breve")
        assertEquals("Em breve", SerieProgressoUtils.aplicarStatusAutomatico(emBreve).status)
    }
}
