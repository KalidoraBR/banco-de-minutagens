package com.kalidorabr.bancodeminutagens.model

/** Episódios que saíram no TMDB e ainda não estão na série salva. */
data class EpisodiosNovosSerie(
    val serieId: String,
    val titulo: String,
    val posterUrl: String,
    val totalTmdb: Int,
    /** Só os episódios novos, agrupados por temporada (temporadas novas vêm completas). */
    val temporadas: List<TemporadaItem>
) {
    val quantidade: Int get() = temporadas.sumOf { it.episodios.size }

    val resumo: String
        get() {
            val rotulos = temporadas.flatMap { t -> t.episodios.map { "${t.numero} E${it.numeroFormatado}" } }
            val inicio = if (quantidade == 1) "+1 episódio" else "+$quantidade episódios"
            return if (rotulos.size <= 2) "$inicio · ${rotulos.joinToString(" e ")}" else "$inicio · a partir de ${rotulos.first()}"
        }
}

object Novidades {
    const val DIA_MS = 24L * 60 * 60 * 1000
    const val DIAS_LEMBRETE_BACKUP = 7

    /** Junta os episódios novos à série, sem mexer nas minutagens que já existem. */
    fun adicionarEpisodios(serie: SerieItem, novos: List<TemporadaItem>): SerieItem {
        val temporadas = serie.temporadas.toMutableList()
        novos.forEach { nova ->
            val i = temporadas.indexOfFirst { it.id == nova.id }
            if (i < 0) {
                temporadas.add(nova)
            } else {
                val atual = temporadas[i]
                val numeros = atual.episodios.map { it.numero }.toSet()
                val juntos = (atual.episodios + nova.episodios.filter { it.numero !in numeros }).sortedBy { it.numero }
                temporadas[i] = atual.copy(episodios = juntos)
            }
        }
        val ordenadas = temporadas.sortedBy { SerieProgressoUtils.numeroTemporada(it.numero) ?: Int.MAX_VALUE }
        return SerieProgressoUtils.recalcularProgressoSerie(serie.copy(temporadas = ordenadas))
    }

    /** Dias desde o último backup (em arquivo ou Sheets); null quando ainda não é hora de lembrar. */
    fun diasParaLembrete(agora: Long, ultimoBackup: Long, adiadoAte: Long, temSeries: Boolean): Int? {
        if (!temSeries || agora < adiadoAte) return null
        val dias = ((agora - ultimoBackup) / DIA_MS).toInt()
        return dias.takeIf { it >= DIAS_LEMBRETE_BACKUP }
    }

    /** Uma série acabou de ficar com todos os episódios concluídos? */
    fun acabouDeCompletar(antes: SerieItem?, depois: SerieItem?): Boolean =
        antes != null && depois != null && depois.totalEpisodios > 0 &&
            antes.episodiosAssistidos < antes.totalEpisodios &&
            depois.episodiosAssistidos >= depois.totalEpisodios
}

data class FaltaSerie(val titulo: String, val concluidos: Int, val total: Int) {
    val faltam: Int get() = (total - concluidos).coerceAtLeast(0)
    val progresso: Float get() = SerieProgressoUtils.progresso(concluidos, total)
}

data class Estatisticas(
    val totalSeries: Int,
    val seriesFinalizadas: Int,
    val episodiosConcluidos: Int,
    val episodiosTotal: Int,
    /** Episódios concluídos em cada um dos últimos 7 dias (o último é hoje). */
    val semana: List<Int>,
    val diasSeguidos: Int,
    val aberturaMedia: Int?,
    val encerramentoMedio: Int?,
    val faltando: List<FaltaSerie>
) {
    val episodiosSemana: Int get() = semana.sum()

    companion object {
        /** [conclusoes]: dia (época em dias) em que cada episódio foi concluído; [hoje] no mesmo formato. */
        fun calcular(series: List<SerieItem>, conclusoes: Map<String, Long>, hoje: Long): Estatisticas {
            val ativas = series.filterNot { it.isDeleted }
            val episodios = ativas.flatMap { s -> s.temporadas.flatMap { it.episodios } }
            val porDia = conclusoes.values.groupingBy { it }.eachCount()
            val semana = (6 downTo 0).map { porDia[hoje - it] ?: 0 }
            var seguidos = 0
            var dia = if ((porDia[hoje] ?: 0) > 0) hoje else hoje - 1
            while ((porDia[dia] ?: 0) > 0) { seguidos++; dia-- }

            fun media(tipo: TipoMinutagem): Int? {
                val d = episodios.mapNotNull { ep ->
                    ep.minutagens.firstOrNull { it.tipo == tipo && it.estado == EstadoMinutagem.PREENCHIDO }?.let {
                        val a = MinutagemSecao.parseMmSsToSeconds(it.tempoInicio)
                        val b = MinutagemSecao.parseMmSsToSeconds(it.tempoFim)
                        if (a != null && b != null && b > a) b - a else null
                    }
                }
                return if (d.isEmpty()) null else d.sum() / d.size
            }

            return Estatisticas(
                totalSeries = ativas.size,
                seriesFinalizadas = ativas.count { it.totalEpisodios > 0 && it.episodiosAssistidos >= it.totalEpisodios },
                episodiosConcluidos = ativas.sumOf { it.episodiosAssistidos },
                episodiosTotal = ativas.sumOf { it.totalEpisodios },
                semana = semana,
                diasSeguidos = seguidos,
                aberturaMedia = media(TipoMinutagem.ABERTURA),
                encerramentoMedio = media(TipoMinutagem.ENCERRAMENTO),
                faltando = ativas
                    .filter { it.totalEpisodios > 0 && it.episodiosAssistidos in 1 until it.totalEpisodios }
                    .sortedByDescending { it.lastUpdatedAt ?: 0L }
                    .map { FaltaSerie(it.titulo, it.episodiosAssistidos, it.totalEpisodios) }
            )
        }
    }
}
