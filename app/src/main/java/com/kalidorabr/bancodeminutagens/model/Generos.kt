package com.kalidorabr.bancodeminutagens.model

/**
 * Gêneros como aparecem na tela: o TMDB manda alguns em inglês mesmo pedindo em português
 * (ex.: "Action & Adventure"). Só muda o que é mostrado; o que fica salvo e vai para a planilha é igual.
 */
object Generos {
    private val traducoes = mapOf(
        "action & adventure" to "Ação e Aventura",
        "sci-fi & fantasy" to "Ficção científica e Fantasia",
        "war & politics" to "Guerra e Política",
        "kids" to "Infantil",
        "news" to "Notícias",
        "reality" to "Reality",
        "soap" to "Novela",
        "talk" to "Entrevistas",
        "action" to "Ação",
        "adventure" to "Aventura",
        "animation" to "Animação",
        "comedy" to "Comédia",
        "crime" to "Crime",
        "documentary" to "Documentário",
        "drama" to "Drama",
        "family" to "Família",
        "fantasy" to "Fantasia",
        "history" to "História",
        "horror" to "Terror",
        "music" to "Música",
        "mystery" to "Mistério",
        "romance" to "Romance",
        "science fiction" to "Ficção científica",
        "thriller" to "Suspense",
        "war" to "Guerra",
        "western" to "Faroeste"
    )

    /** "Action & Adventure • Drama" → "Ação e Aventura · Drama". */
    fun exibir(texto: String): String =
        texto.split(" • ", " / ", " · ")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(" · ") { traducoes[it.lowercase()] ?: it }
}
