package com.kalidorabr.bancodeminutagens.model

import kotlin.math.abs

/**
 * Confere se um tempo digitado "parece errado" comparando com o resto da série.
 * Só avisa: quem decide se salva assim é o usuário.
 */
object AvisoTempo {
    private const val MINIMO_AMOSTRAS = 3
    private const val DIFERENCA_MINIMA = 30
    private const val LIMITE_SEM_REFERENCIA = 5 * 60

    /** Duração típica (mediana, em segundos) da seção na série, sem contar o episódio editado. */
    fun duracaoTipica(serie: SerieItem, tipo: TipoMinutagem, excetoEpisodioId: String? = null): Int? {
        val duracoes = serie.temporadas.flatMap { it.episodios }
            .filter { it.id != excetoEpisodioId }
            .mapNotNull { ep -> ep.minutagens.firstOrNull { it.tipo == tipo }?.let(::duracao) }
            .sorted()
        if (duracoes.size < MINIMO_AMOSTRAS) return null
        return duracoes[duracoes.size / 2]
    }

    /** Devolve o texto do aviso, ou null quando o tempo parece normal. */
    fun verificar(
        tipo: TipoMinutagem,
        inicio: Int?,
        fim: Int?,
        duracaoTipica: Int?,
        outrasSecoes: List<MinutagemSecao> = emptyList(),
        duracaoEpisodioSeg: Int = 0
    ): String? {
        if (inicio == null || fim == null || fim < inicio) return null
        val dur = fim - inicio
        val fmt = MinutagemSecao::formatSecondsToMmSs

        if (duracaoEpisodioSeg > 0 && fim > duracaoEpisodioSeg + 30) {
            return "O fim (${fmt(fim)}) passa da duração do episódio (${fmt(duracaoEpisodioSeg)})."
        }

        if (duracaoTipica != null && abs(dur - duracaoTipica) >= DIFERENCA_MINIMA &&
            (dur > duracaoTipica * 2 || dur * 2 < duracaoTipica)
        ) {
            return "Duração de ${fmt(dur)}, mas nesta série costuma ser cerca de ${fmt(duracaoTipica)}."
        }
        if (duracaoTipica == null && dur > LIMITE_SEM_REFERENCIA) {
            return "Duração de ${fmt(dur)}. Parece longa; confira se o tempo está certo."
        }

        fun preenchida(t: TipoMinutagem) = outrasSecoes.firstOrNull {
            it.tipo == t && it.estado == EstadoMinutagem.PREENCHIDO
        }
        when (tipo) {
            TipoMinutagem.ENCERRAMENTO -> preenchida(TipoMinutagem.ABERTURA)
                ?.let { MinutagemSecao.parseMmSsToSeconds(it.tempoFim) }
                ?.takeIf { inicio < it }
                ?.let { return "O encerramento começa antes do fim da abertura (${fmt(it)})." }
            TipoMinutagem.ABERTURA -> preenchida(TipoMinutagem.ENCERRAMENTO)
                ?.let { MinutagemSecao.parseMmSsToSeconds(it.tempoInicio) }
                ?.takeIf { fim > it }
                ?.let { return "A abertura termina depois do início do encerramento (${fmt(it)})." }
            else -> {}
        }
        return null
    }

    private fun duracao(secao: MinutagemSecao): Int? {
        if (secao.estado != EstadoMinutagem.PREENCHIDO) return null
        val a = MinutagemSecao.parseMmSsToSeconds(secao.tempoInicio) ?: return null
        val b = MinutagemSecao.parseMmSsToSeconds(secao.tempoFim) ?: return null
        return (b - a).takeIf { it > 0 }
    }
}
