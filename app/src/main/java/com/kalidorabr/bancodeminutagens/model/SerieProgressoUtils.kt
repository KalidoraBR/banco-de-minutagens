package com.kalidorabr.bancodeminutagens.model

import kotlin.math.roundToInt

data class DestinoEpisodio(val tmdbId: String, val seasonNumber: Int, val episodeNumber: Int) {
    val label: String get() = "T${seasonNumber.toString().padStart(2, '0')}E${episodeNumber.toString().padStart(2, '0')}"
}

data class MinutagemInconsistente(
    val serieId: String,
    val serieTitulo: String,
    val temporadaId: String,
    val temporadaNumero: String,
    val episodioId: String,
    val episodioNumero: Int,
    val tipo: TipoMinutagem,
    val mensagens: List<String>
)

object SerieProgressoUtils {
    fun inconsistencias(secao: MinutagemSecao): List<String> {
        if (secao.estado != EstadoMinutagem.PREENCHIDO) return emptyList()
        val inicio = MinutagemSecao.parseMmSsToSeconds(secao.tempoInicio)
        val fim = MinutagemSecao.parseMmSsToSeconds(secao.tempoFim)
        return buildList {
            if (inicio == null) add("Início inválido")
            if (fim == null) add("Fim inválido")
            if (inicio != null && fim != null && fim < inicio) add("Fim anterior ao início")
            if (inicio != null && fim != null && fim - inicio > 900) add("Duração superior a 15 minutos")
        }
    }

    fun contarInconsistencias(series: List<SerieItem>): Int = series.sumOf { serie ->
        serie.temporadas.sumOf { temporada ->
            temporada.episodios.sumOf { episodio -> episodio.minutagens.count { inconsistencias(it).isNotEmpty() } }
        }
    }

    fun listarInconsistencias(series: List<SerieItem>): List<MinutagemInconsistente> = buildList {
        series.filterNot { it.isDeleted }.forEach { serie ->
            serie.temporadas.forEach { temporada ->
                temporada.episodios.forEach { episodio ->
                    episodio.minutagens.forEach { secao ->
                        val mensagens = inconsistencias(secao)
                        if (mensagens.isNotEmpty()) {
                            add(
                                MinutagemInconsistente(
                                    serieId = serie.id,
                                    serieTitulo = serie.titulo,
                                    temporadaId = temporada.id,
                                    temporadaNumero = temporada.numero,
                                    episodioId = episodio.id,
                                    episodioNumero = episodio.numero,
                                    tipo = secao.tipo,
                                    mensagens = mensagens
                                )
                            )
                        }
                    }
                }
            }
        }
    }
    fun progresso(concluidos: Int, total: Int): Float =
        if (total > 0) (concluidos.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f

    fun porcentagem(concluidos: Int, total: Int): Int = (progresso(concluidos, total) * 100f).roundToInt()

    fun normalizarCampo(raw: String?): String = raw?.trim()?.takeUnless {
        it.equals("null", true) || it == "{}" || it == "[]"
    }.orEmpty()

    fun normalizarSecao(secao: MinutagemSecao): MinutagemSecao {
        var inicio = normalizarCampo(secao.tempoInicio)
        var fim = normalizarCampo(secao.tempoFim)
        fun isZero(value: String): Boolean = value.toDoubleOrNull() == 0.0 ||
            MinutagemSecao.parseMmSsToSeconds(value) == 0
        // Zero só é histórico quando confirmado, ou faz parte de um intervalo real.
        val temOutroDado = listOf(inicio, fim).any { it.isNotBlank() && !isZero(it) }
        if (!secao.confirmadaIndividualmente && !temOutroDado) {
            if (isZero(inicio)) inicio = ""
            if (isZero(fim)) fim = ""
        } else {
            if (isZero(inicio)) inicio = "00:00"
            if (isZero(fim)) fim = "00:00"
        }
        // NAO_POSSUI persistido é individual; flags isoladas não comprovam histórico.
        val naoPossui = secao.estado == EstadoMinutagem.NAO_POSSUI ||
            (secao.confirmadaIndividualmente && secao.hasNot)
        val estado = when {
            naoPossui -> EstadoMinutagem.NAO_POSSUI
            inicio.isNotBlank() || fim.isNotBlank() -> EstadoMinutagem.PREENCHIDO
            else -> EstadoMinutagem.PENDENTE
        }
        return secao.copy(tempoInicio = inicio, tempoFim = fim, estado = estado,
            isFilled = estado != EstadoMinutagem.PENDENTE, hasNot = naoPossui)
    }

    fun isSecaoConcluida(secao: MinutagemSecao): Boolean {
        val normalizada = normalizarSecao(secao)
        if (normalizada.estado == EstadoMinutagem.NAO_POSSUI) return true
        val inicio = MinutagemSecao.parseMmSsToSeconds(normalizada.tempoInicio) ?: return false
        val fim = MinutagemSecao.parseMmSsToSeconds(normalizada.tempoFim) ?: return false
        return fim >= inicio
    }

    private fun temHistorico(secao: MinutagemSecao): Boolean {
        val normalizada = normalizarSecao(secao)
        return normalizada.tempoInicio.isNotBlank() || normalizada.tempoFim.isNotBlank() ||
            normalizada.estado == EstadoMinutagem.NAO_POSSUI || normalizada.confirmadaIndividualmente
    }

    fun isSecaoParcial(secao: MinutagemSecao): Boolean = temHistorico(secao) && !isSecaoConcluida(secao)

    fun calcularStatusEpisodio(episodio: EpisodioItem): StatusEpisodio {
        if (TipoMinutagem.entries.all { tipo ->
            episodio.minutagens.firstOrNull { it.tipo == tipo }?.let(::isSecaoConcluida) == true
        }) return StatusEpisodio.CONCLUIDO
        return if (episodio.minutagens.any(::temHistorico)) StatusEpisodio.PARCIAL else StatusEpisodio.PENDENTE
    }

    fun numeroTemporada(raw: String): Int? {
        val trimmed = raw.trim()
        val regexMatch = Regex("^(?:[TS])?(\\d+)$", RegexOption.IGNORE_CASE).matchEntire(trimmed)?.groupValues?.get(1)?.toIntOrNull()
        if (regexMatch != null) return regexMatch
        val digits = trimmed.filter { it.isDigit() }
        return digits.toIntOrNull()
    }

    fun encontrarProximoEpisodio(serie: SerieItem): DestinoEpisodio? {
        val episodios = serie.temporadas.mapNotNull { temp ->
            numeroTemporada(temp.numero)?.let { numero -> numero to temp }
        }.sortedBy { it.first }.flatMap { (numero, temp) ->
            temp.episodios.sortedBy { it.numero }.map { ep ->
                DestinoEpisodio(serie.id, numero, ep.numero) to ep
            }
        }
        // O último episódio que o usuário editou no app ficou parcial? Volta nele para terminar.
        // (lastEditedEpisodeId só é gravado por edições feitas no app, nunca pela sincronização.)
        episodios.firstOrNull { it.second.id == serie.lastEditedEpisodeId }
            ?.takeIf { calcularStatusEpisodio(it.second) == StatusEpisodio.PARCIAL }
            ?.let { return it.first }
        val ultimo = episodios.indexOfLast { calcularStatusEpisodio(it.second) != StatusEpisodio.PENDENTE }
        // Sem histórico começa no primeiro existente; não volta às pendências antigas.
        return episodios.getOrNull(ultimo + 1)?.first
    }

    fun localizarEpisodio(series: List<SerieItem>, destino: DestinoEpisodio): Triple<SerieItem, TemporadaItem, EpisodioItem>? {
        val targetTmdbId = destino.tmdbId.trim().replace(".0", "")
        val targetSeason = destino.seasonNumber
        val targetEpisode = destino.episodeNumber

        val serie = series.firstOrNull { it.id.trim().replace(".0", "") == targetTmdbId } ?: return null
        val temp = serie.temporadas.firstOrNull { numeroTemporada(it.numero) == targetSeason } ?: return null
        val ep = temp.episodios.firstOrNull { it.numero == targetEpisode } ?: return null
        return Triple(serie, temp, ep)
    }

    /**
     * Status automático pela minutagem (seção "Não possui" conta como marcada; só seção em branco conta como falta):
     *  - todos os episódios completos → "Finalizada";
     *  - chegou ao último episódio (ele tem alguma marcação) mas ficou episódio/seção em branco → "Incompleta";
     *  - nenhum dos dois → null (o app não decide; mantém o status).
     * Temporada 0 (especiais) não entra na conta.
     */
    fun statusAutomatico(serie: SerieItem): String? {
        val episodios = serie.temporadas
            .mapNotNull { temp -> numeroTemporada(temp.numero)?.takeIf { it > 0 }?.let { it to temp } }
            .sortedBy { it.first }
            .flatMap { (_, temp) -> temp.episodios.sortedBy { it.numero } }
        if (episodios.isEmpty()) return null
        val status = episodios.map(::calcularStatusEpisodio)
        if (status.all { it == StatusEpisodio.CONCLUIDO }) return "Finalizada"
        if (status.last() != StatusEpisodio.PENDENTE) return "Incompleta"
        return null
    }

    private val statusDeFim = setOf("finalizada", "incompleta", "concluída", "concluído")

    /** Aplica o status automático. Se a série deixou de estar no fim (ex.: limpou o último episódio), volta para "Em andamento". */
    fun aplicarStatusAutomatico(serie: SerieItem): SerieItem {
        val auto = statusAutomatico(serie)
        return when {
            auto != null -> if (serie.status.equals(auto, ignoreCase = true)) serie else serie.copy(status = auto)
            serie.status.lowercase() in statusDeFim -> serie.copy(status = "Em andamento")
            else -> serie
        }
    }

    fun recalcularProgressoSerie(serie: SerieItem): SerieItem {
        val temporadas = serie.temporadas.map { temp ->
            val episodios = temp.episodios.map { ep ->
                val normalizado = ep.copy(minutagens = ep.minutagens.map(::normalizarSecao))
                normalizado.copy(status = calcularStatusEpisodio(normalizado))
            }
            val concluidos = episodios.count { it.status == StatusEpisodio.CONCLUIDO }
            temp.copy(episodios = episodios, totalEpisodios = episodios.size,
                episodiosConcluidos = concluidos,
                status = if (episodios.any { it.status != StatusEpisodio.PENDENTE }) "Em andamento" else "Não iniciada")
        }
        val recalculada = serie.copy(temporadas = temporadas,
            episodiosAssistidos = temporadas.sumOf { it.episodiosConcluidos },
            totalEpisodios = temporadas.sumOf { it.totalEpisodios })
        return recalculada.copy(proximoEpisodio = encontrarProximoEpisodio(recalculada)?.label.orEmpty())
    }
}
