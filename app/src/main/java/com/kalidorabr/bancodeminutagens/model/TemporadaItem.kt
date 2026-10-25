package com.kalidorabr.bancodeminutagens.model

data class TemporadaItem(
    val id: String,
    val numero: String,
    val nome: String,
    val totalEpisodios: Int,
    val episodiosConcluidos: Int,
    val status: String = "Não iniciada",
    val imageUrl: String = "",
    val episodios: List<EpisodioItem> = emptyList()
) {
    val progressoPorcentagem: Int
        get() = SerieProgressoUtils.porcentagem(episodiosConcluidos, totalEpisodios)

    val progressoFloat: Float
        get() = SerieProgressoUtils.progresso(episodiosConcluidos, totalEpisodios)
}
