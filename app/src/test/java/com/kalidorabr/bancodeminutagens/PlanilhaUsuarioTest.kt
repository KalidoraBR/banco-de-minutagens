package com.kalidorabr.bancodeminutagens

import com.kalidorabr.bancodeminutagens.data.PlanilhaUsuario
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanilhaUsuarioTest {
    private val id = "1AbC_dEf-123456789xyz"

    @Test
    fun linkCompletoViraSoOId() {
        assertEquals(id, PlanilhaUsuario.extrairId("https://docs.google.com/spreadsheets/d/$id/edit?gid=0#gid=0"))
    }

    @Test
    fun linkComContaDoGoogleTambemFunciona() {
        assertEquals(id, PlanilhaUsuario.extrairId("  https://docs.google.com/spreadsheets/u/1/d/$id/edit  "))
    }

    @Test
    fun idPuroContinuaIgual() {
        assertEquals(id, PlanilhaUsuario.extrairId(" $id "))
    }

    @Test
    fun codigoDoAppsScriptEstaNoApp() {
        val arquivo = java.io.File("src/main/assets/${PlanilhaUsuario.ARQUIVO_CODIGO}")
        val texto = arquivo.readText()
        // as três ações que o app usa precisam existir no código que a pessoa cola
        listOf("import_to_app", "export_to_sheets", "clearEpisodes", "function doGet", "function doPost")
            .forEach { assert(texto.contains(it)) { "faltou $it no código da planilha" } }
    }
}
