package com.kalidorabr.bancodeminutagens

import com.kalidorabr.bancodeminutagens.model.StatusPlanilha
import org.junit.Assert.assertEquals
import org.junit.Test

class StatusPlanilhaTest {
    @Test fun scriptAntigoEhIgnorado() =
        assertEquals("Finalizada", StatusPlanilha.escolher("Finalizada", "Em andamento", temMarca = false))

    @Test fun incompletaTambemNaoEhRebaixadaPeloScriptAntigo() =
        assertEquals("Incompleta", StatusPlanilha.escolher("Incompleta", "Em andamento", temMarca = false))

    @Test fun f2VaziaMantemOApp() =
        assertEquals("Finalizada", StatusPlanilha.escolher("Finalizada", "", temMarca = true))

    @Test fun f2ValidaVale() =
        assertEquals("Em andamento", StatusPlanilha.escolher("Finalizada", "Em andamento", temMarca = true))

    @Test fun f2SemDiferencaDeMaiusculas() =
        assertEquals("Incompleta", StatusPlanilha.escolher("Em andamento", " incompleta ", temMarca = true))

    @Test fun f2ComErroDeDigitacaoMantemOApp() =
        assertEquals("Em breve", StatusPlanilha.escolher("Em breve", "Finalisada", temMarca = true))
}
