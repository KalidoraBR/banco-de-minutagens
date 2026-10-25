package com.kalidorabr.bancodeminutagens.model

/** Uma seção pronta para mandar ao IntroDB. */
data class EnvioIntroDb(
    val episodioId: String,
    val temporada: Int,
    val episodio: Int,
    val tipo: TipoMinutagem,
    val inicioSeg: Int,
    val fimSeg: Int
) {
    /** Nome da seção como o IntroDB espera ("intro", "recap", "outro"). */
    val segmentType: String
        get() = when (tipo) {
            TipoMinutagem.ABERTURA -> "intro"
            TipoMinutagem.RECAP -> "recap"
            TipoMinutagem.ENCERRAMENTO -> "outro"
        }

    val rotulo: String
        get() = "T${temporada.toString().padStart(2, '0')}E${episodio.toString().padStart(2, '0')} · ${
            when (tipo) {
                TipoMinutagem.ABERTURA -> "Abertura"
                TipoMinutagem.RECAP -> "Recap"
                TipoMinutagem.ENCERRAMENTO -> "Encerramento"
            }
        }"
}

/** Seção que não pode ir (fora dos limites do IntroDB). */
data class PuladoIntroDb(val rotulo: String, val motivo: String)

data class PlanoIntroDb(val envios: List<EnvioIntroDb>, val pulados: List<PuladoIntroDb>)

object IntroDbEnvio {
    /** Limites de duração do IntroDB, em segundos (mínimo e máximo). */
    fun limites(tipo: TipoMinutagem): IntRange = when (tipo) {
        TipoMinutagem.ABERTURA -> 5..180
        TipoMinutagem.RECAP -> 5..480
        TipoMinutagem.ENCERRAMENTO -> 5..900
    }

    /**
     * Monta o que vai ser enviado dos episódios escolhidos.
     * Só vão seções com tempo; "Não possui" e vazias ficam de fora (a API não recebe "não possui").
     */
    fun planejar(temporadaNumero: Int, episodios: List<EpisodioItem>): PlanoIntroDb {
        val envios = mutableListOf<EnvioIntroDb>()
        val pulados = mutableListOf<PuladoIntroDb>()
        episodios.sortedBy { it.numero }.forEach { ep ->
            TipoMinutagem.entries.forEach { tipo ->
                val s = ep.minutagens.firstOrNull { it.tipo == tipo } ?: return@forEach
                if (s.estado != EstadoMinutagem.PREENCHIDO) return@forEach
                val a = MinutagemSecao.parseMmSsToSeconds(s.tempoInicio)
                val b = MinutagemSecao.parseMmSsToSeconds(s.tempoFim)
                val item = EnvioIntroDb(ep.id, temporadaNumero, ep.numero, tipo, a ?: 0, b ?: 0)
                val faixa = limites(tipo)
                when {
                    a == null || b == null || b <= a -> pulados += PuladoIntroDb(item.rotulo, "tempo inválido")
                    (b - a) !in faixa -> pulados += PuladoIntroDb(
                        item.rotulo,
                        "duração de ${MinutagemSecao.formatSecondsToMmSs(b - a)} (o IntroDB aceita de " +
                            "${MinutagemSecao.formatSecondsToMmSs(faixa.first)} a ${MinutagemSecao.formatSecondsToMmSs(faixa.last)})"
                    )
                    else -> envios += item
                }
            }
        }
        return PlanoIntroDb(envios, pulados)
    }
}

/** Tempos que o IntroDB tem de um episódio (em segundos) e as seções que ele confirma que não existem. */
data class TemposIntroDb(
    val secoes: Map<TipoMinutagem, Pair<Int, Int>>,
    val naoPossui: Set<TipoMinutagem> = emptySet()
) {
    val vazio: Boolean get() = secoes.isEmpty() && naoPossui.isEmpty()
}

object ImportarIntroDb {
    /**
     * Preenche só as seções vazias do episódio com o que veio do IntroDB.
     * O resultado é uma minutagem comum (editável); o que já estava preenchido não muda.
     */
    fun aplicar(atual: List<MinutagemSecao>, tempos: TemposIntroDb): List<MinutagemSecao> =
        TipoMinutagem.entries.map { tipo ->
            val s = atual.firstOrNull { it.tipo == tipo } ?: MinutagemSecao(tipo = tipo)
            if (s.estado != EstadoMinutagem.PENDENTE) return@map s
            val t = tempos.secoes[tipo]
            when {
                t != null -> s.copy(
                    estado = EstadoMinutagem.PREENCHIDO,
                    tempoInicio = MinutagemSecao.formatSecondsToMmSs(t.first),
                    tempoFim = MinutagemSecao.formatSecondsToMmSs(t.second),
                    isFilled = true, hasNot = false, confirmadaIndividualmente = true
                )
                tipo in tempos.naoPossui -> s.copy(
                    estado = EstadoMinutagem.NAO_POSSUI, tempoInicio = "", tempoFim = "",
                    isFilled = true, hasNot = true, confirmadaIndividualmente = true
                )
                else -> s
            }
        }

    /** Tem algo do IntroDB que preencheria uma seção vazia? */
    fun ajuda(atual: List<MinutagemSecao>, tempos: TemposIntroDb): Boolean = aplicar(atual, tempos) != TipoMinutagem.entries.map { tipo ->
        atual.firstOrNull { it.tipo == tipo } ?: MinutagemSecao(tipo = tipo)
    }
}
