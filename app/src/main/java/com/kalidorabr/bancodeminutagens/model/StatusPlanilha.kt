package com.kalidorabr.bancodeminutagens.model

/** Selo da série guardado na planilha (célula F2). */
object StatusPlanilha {
    val validos = listOf("Em andamento", "Finalizada", "Incompleta", "Em breve")

    /** Nome mostrado no selo: "Em breve" vira "Não iniciada" (série ainda sem minutagem). Na planilha fica igual. */
    fun rotulo(status: String): String = when {
        status.isBlank() -> "Em andamento"
        status.equals("Em breve", ignoreCase = true) -> "Não iniciada"
        else -> status
    }

    /**
     * Qual selo fica depois de importar.
     * [temMarca] = o script novo leu a F2 (o script antigo mandava sempre "Em andamento" e deve ser ignorado).
     * F2 vazia ou com valor fora da lista: mantém o selo do app.
     */
    fun escolher(local: String, daPlanilha: String, temMarca: Boolean): String {
        if (!temMarca) return local
        return validos.firstOrNull { it.equals(daPlanilha.trim(), ignoreCase = true) } ?: local
    }
}
