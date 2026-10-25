package com.kalidorabr.bancodeminutagens.model

data class SerieItem(
    val id: String,
    val titulo: String,
    val ano: Int,
    val generos: String,
    val status: String = "Em andamento",
    val episodiosAssistidos: Int,
    val totalEpisodios: Int,
    val proximoEpisodio: String,
    val imageUrl: String,
    val posterUrl: String = imageUrl,
    val sinopse: String = "",
    val identificadores: String = "IMDb • TVDB",
    val imdbId: String = "",
    val tvdbId: String = "",
    val minutagensAtivas: String = "0 de 3 ativas",
    val aberturaAtiva: Boolean = false,
    val recapAtivo: Boolean = false,
    val encerramentoAtivo: Boolean = false,
    val duracaoFixaAbertura: String = "",
    val duracaoFixaRecap: String = "",
    val duracaoFixaEncerramento: String = "",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val lastUpdatedAt: Long? = null,
    val lastEditedEpisodeId: String? = null,
    val temporadas: List<TemporadaItem> = emptyList()
) {
    val isFinalizada: Boolean
        get() = status.equals("Finalizada", ignoreCase = true) ||
                status.equals("Concluída", ignoreCase = true) ||
                status.equals("Concluído", ignoreCase = true)

    val isIncompleta: Boolean
        get() = status.equals("Incompleta", ignoreCase = true) ||
                (isFinalizada && (totalEpisodios == 0 || episodiosAssistidos < totalEpisodios))

    val progressoPorcentagem: Int
        get() = SerieProgressoUtils.porcentagem(episodiosAssistidos, totalEpisodios)

    val progressoFloat: Float
        get() = SerieProgressoUtils.progresso(episodiosAssistidos, totalEpisodios)
}
