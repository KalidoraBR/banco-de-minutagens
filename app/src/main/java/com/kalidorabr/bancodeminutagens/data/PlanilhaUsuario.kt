package com.kalidorabr.bancodeminutagens.data

import android.content.Context

/** Ajuda quem vai criar a própria planilha: código do Apps Script e leitura do link. */
object PlanilhaUsuario {
    /** Arquivo em app/src/main/assets com o código que a pessoa cola no Apps Script. */
    const val ARQUIVO_CODIGO = "BancoDeMinutagens.gs"

    private val idNoLink = Regex("""/spreadsheets/(?:u/\d+/)?d/([A-Za-z0-9_-]+)""")

    /**
     * Aceita o link inteiro da planilha (docs.google.com/spreadsheets/d/ID/edit…) e devolve só o ID.
     * Qualquer outro texto volta como está, sem espaços nas pontas.
     */
    fun extrairId(texto: String): String {
        val limpo = texto.trim()
        return idNoLink.find(limpo)?.groupValues?.get(1) ?: limpo
    }

    fun codigo(context: Context): String =
        context.assets.open(ARQUIVO_CODIGO).bufferedReader(Charsets.UTF_8).use { it.readText() }
}
