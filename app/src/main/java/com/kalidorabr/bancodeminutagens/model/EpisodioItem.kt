package com.kalidorabr.bancodeminutagens.model

enum class StatusEpisodio {
    CONCLUIDO,
    PROXIMO,
    PARCIAL,
    PENDENTE
}

data class EpisodioItem(
    val id: String,
    val numero: Int,
    val titulo: String = "Episódio $numero",
    val status: StatusEpisodio = StatusEpisodio.PENDENTE,
    val dataExibicao: String = "",
    /** Duração do episódio em minutos, vinda do TMDB (0 = desconhecida). */
    val duracaoMin: Int = 0,
    val minutagens: List<MinutagemSecao> = emptyList(),
    /**
     * Editado no app e ainda sem o toque em Concluir: mesmo com as 3 seções marcadas, fica Parcial.
     * Episódios antigos e os que vêm da planilha não têm essa marca e contam como concluídos.
     */
    val aguardandoConcluir: Boolean = false
) {
    val numeroFormatado: String
        get() = if (numero < 10) "0$numero" else "$numero"
}
