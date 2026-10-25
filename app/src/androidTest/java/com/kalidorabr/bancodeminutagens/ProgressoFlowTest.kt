package com.kalidorabr.bancodeminutagens

import android.content.ContextWrapper
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.kalidorabr.bancodeminutagens.data.GoogleSheetsImportService
import com.kalidorabr.bancodeminutagens.data.SeriesRepository
import com.kalidorabr.bancodeminutagens.model.*
import com.kalidorabr.bancodeminutagens.ui.home.SerieCard
import com.kalidorabr.bancodeminutagens.ui.episode.EpisodioDetailScreen
import com.kalidorabr.bancodeminutagens.ui.season.TemporadaDetailScreen
import com.kalidorabr.bancodeminutagens.ui.theme.BancoDeMinutagensTheme
import java.io.File
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ProgressoFlowTest {
    @get:Rule val compose = createComposeRule()

    private fun serie(temporadaPreenchida: Int): SerieItem = SerieProgressoUtils.recalcularProgressoSerie(
        SerieItem("31654", "Digimon", 1999, "", episodiosAssistidos = 0, totalEpisodios = 104,
            proximoEpisodio = "", imageUrl = "", temporadas = (1..2).map { season ->
                TemporadaItem("31654_s$season", "T0$season", "Temporada $season", if (season == 1) 54 else 50, 0,
                    episodios = (1..if (season == 1) 54 else 50).map { ep ->
                        EpisodioItem("31654_s${season}_ep_$ep", ep, minutagens = TipoMinutagem.entries.map {
                            if (season == temporadaPreenchida && ep == 9)
                                MinutagemSecao(it, EstadoMinutagem.PREENCHIDO, "00:00", "01:30")
                            else MinutagemSecao(it)
                        })
                    })
            })
    )

    @Test fun cardEnviaIdentidadeCompletaEAbrirDestinoCorretoNasDuasTemporadas() {
        var atual by mutableStateOf(serie(2))
        var destino by mutableStateOf<DestinoEpisodio?>(null)
        compose.setContent {
            BancoDeMinutagensTheme {
                val aberto = destino?.let { SerieProgressoUtils.localizarEpisodio(listOf(atual), it) }
                if (aberto == null) {
                    SerieCard(atual, onContinueClick = { destino = it })
                } else {
                    EpisodioDetailScreen(nomeSerie = aberto.first.titulo, temporadaLabel = aberto.second.numero,
                        numeroEpisodio = aberto.third.numero, tituloEpisodio = aberto.third.titulo,
                        minutagens = aberto.third.minutagens)
                }
            }
        }
        compose.onNodeWithText("1 de 104 episódios • 1%").assertExists()
        compose.onNodeWithText("Continuar T02E10").performClick()
        compose.onNodeWithText("T02").assertExists()
        compose.onNodeWithText("EP 10 · Episódio 10").assertExists()
        compose.runOnIdle { assertEquals(DestinoEpisodio("31654", 2, 10), destino); destino = null; atual = serie(1) }
        compose.onNodeWithText("Continuar T01E10").performClick()
        compose.onNodeWithText("T01").assertExists()
        compose.onNodeWithText("EP 10 · Episódio 10").assertExists()
        compose.runOnIdle { assertEquals(DestinoEpisodio("31654", 1, 10), destino) }
    }

    @Test fun filtroParciaisNaoIncluiEpisodiosVazios() {
        val episodios = (1..53).map { numero ->
            val ep = EpisodioItem("ep_$numero", numero, minutagens = TipoMinutagem.entries.map {
                if (numero in 5..8) MinutagemSecao(it, EstadoMinutagem.PREENCHIDO, "00:00", "01:30")
                else MinutagemSecao(it, tempoInicio = "null", tempoFim = "00:00")
            })
            ep.copy(status = SerieProgressoUtils.calcularStatusEpisodio(ep))
        }
        compose.setContent { BancoDeMinutagensTheme { TemporadaDetailScreen(episodios = episodios) } }
        compose.onNodeWithText("Parciais").performClick()
        compose.onAllNodesWithText("EP 01").assertCountEquals(0)
        compose.onAllNodesWithText("EP 05").assertCountEquals(0)
    }

    @Test fun importacaoNormalizaObjetosNulosEZerosSemApagarMinutagemValida() {
        val tipo = TipoMinutagem.ABERTURA
        val valida = MinutagemSecao(tipo, EstadoMinutagem.PREENCHIDO, "00:00", "01:30")
        val vazios = listOf(null, JSONObject.NULL, "", " ", "null", 0, "00:00", JSONObject(),
            JSONObject("""{"start":null,"end":null,"isFilled":true,"hasNot":false}"""),
            JSONObject("""{"start":"00:00","end":"00:00","isFilled":true}"""))
        for (raw in vazios) {
            val imported = GoogleSheetsImportService.mergeMinutagemSecao(null, tipo, raw)
            assertEquals(StatusEpisodio.PENDENTE, SerieProgressoUtils.calcularStatusEpisodio(EpisodioItem("ep", 1, minutagens = listOf(imported))))
            assertEquals(valida, GoogleSheetsImportService.mergeMinutagemSecao(valida, tipo, raw))
        }
        val real = GoogleSheetsImportService.mergeMinutagemSecao(null, tipo,
            JSONObject("""{"startTime":"00:00","endTime":"01:30"}"""))
        assertTrue(SerieProgressoUtils.isSecaoConcluida(real))
    }

    @Test fun carregamentoReprocessaJsonAntigoEPreservaZeroConfirmado() = runBlocking {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val isolated = File(base.cacheDir, "progresso-test-${System.nanoTime()}").apply { mkdirs() }
        val context = object : ContextWrapper(base) { override fun getFilesDir(): File = isolated }
        try {
            val repository = SeriesRepository(context)
            val original = serie(2)
            repository.salvarSeries(listOf(original))
            val file = File(isolated, "banco_minutagens_series.json")
            // Simula o JSON antigo apenas na pasta temporária deste teste.
            val json = org.json.JSONArray(file.readText())
            val s = json.getJSONObject(0)
            s.put("episodiosAssistidos", 104)
            val vazio = s.getJSONArray("temporadas").getJSONObject(0).getJSONArray("episodios").getJSONObject(0)
            vazio.put("status", "PARCIAL")
            vazio.getJSONArray("minutagens").getJSONObject(0).put("tempoInicio", JSONObject.NULL).put("tempoFim", "00:00")
            file.writeText(json.toString())
            val carregada = repository.carregarSeries().single()
            assertEquals(1, carregada.episodiosAssistidos)
            assertEquals(1, carregada.progressoPorcentagem)
            assertEquals("T02E10", carregada.proximoEpisodio)
            assertEquals(StatusEpisodio.PENDENTE, carregada.temporadas[0].episodios[0].status)
            val zero = MinutagemSecao(TipoMinutagem.ABERTURA, EstadoMinutagem.PREENCHIDO, "00:00", "00:00", confirmadaIndividualmente = true)
            val temp = carregada.temporadas[0]
            val atualizada = carregada.copy(temporadas = listOf(temp.copy(episodios = listOf(temp.episodios[0].copy(minutagens = listOf(zero))))))
            repository.salvarSeries(listOf(atualizada))
            val persistida = repository.carregarSeries().single().temporadas.single().episodios.single().minutagens.single()
            assertEquals(zero, persistida)
        } finally {
            isolated.listFiles()?.forEach { it.delete() }
            isolated.delete()
        }
    }
}
