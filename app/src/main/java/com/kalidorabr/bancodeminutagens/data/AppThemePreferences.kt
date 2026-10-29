package com.kalidorabr.bancodeminutagens.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.kalidorabr.bancodeminutagens.ui.theme.AppThemeOpcao

enum class VisualMode(
    val id: String,
    val titulo: String,
    val descricao: String,
    /** false = ainda em construção: não aparece na escolha de Interface. */
    val pronto: Boolean = true
) {
    // Lite foi removido na 1.7: fica escondido e quem usava passa a abrir no Tema A
    LITE("LITE", "Versão Lite", "Visual compacto, simples e com melhor desempenho.", pronto = false),
    TEMA_A("TEMA_A", "Tema A", "Escuro e minimalista, com detalhes na cor de destaque."),
    TEMA_B("TEMA_B", "Tema B", "Claro e quente, cabeçalho escuro e cards brancos."),
    TEMA_C("TEMA_C", "Tema C", "Streaming: imagens grandes, grafite e vermelho."),
    TEMA_D("TEMA_D", "Tema D", "Neon roxo: anéis de progresso e brilho suave.");

    /** Temas novos (A, B, C, D): usam as telas de ui/temas/ em vez das antigas. */
    val usaTelasNovas: Boolean
        get() = this == TEMA_A || this == TEMA_B || this == TEMA_C || this == TEMA_D

    companion object {
        /** Os que aparecem na escolha de Interface. */
        val disponiveis: List<VisualMode> get() = entries.filter { it.pronto }

        fun fromId(id: String?): VisualMode {
            // Visuais removidos (Full e Experimental) passam a abrir no Tema A
            if (id.equals("FULL", ignoreCase = true) || id.equals("EXPERIMENTAL", ignoreCase = true)) return TEMA_A
            val modo = entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: TEMA_A
            // Um tema ainda não pronto nunca fica ativo
            return if (modo.pronto) modo else TEMA_A
        }
    }
}

class AppThemePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSelectedTheme(): AppThemeOpcao {
        val themeId = prefs.getString(KEY_THEME_ID, AppThemeOpcao.VERMELHO.id)
        return AppThemeOpcao.fromId(themeId)
    }

    fun saveSelectedTheme(theme: AppThemeOpcao) {
        prefs.edit {
            putString(KEY_THEME_ID, theme.id)
        }
    }

    fun getVisualMode(): VisualMode {
        val modeId = prefs.getString(KEY_VISUAL_MODE, VisualMode.TEMA_A.id)
        return VisualMode.fromId(modeId)
    }

    fun saveVisualMode(mode: VisualMode) {
        prefs.edit {
            putString(KEY_VISUAL_MODE, mode.id)
        }
    }

    companion object {
        private const val PREFS_NAME = "app_theme_prefs"
        private const val KEY_THEME_ID = "selected_theme_id"
        private const val KEY_VISUAL_MODE = "selected_visual_mode"
    }
}
