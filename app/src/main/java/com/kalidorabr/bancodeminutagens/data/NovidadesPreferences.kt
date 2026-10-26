package com.kalidorabr.bancodeminutagens.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONObject

/**
 * Dados das funções da versão 1.7: lembrete de backup, aviso de episódios novos
 * e histórico de episódios concluídos (usado nas estatísticas).
 * Fica separado das preferências do Google Sheets de propósito.
 */
class NovidadesPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Momento do último backup em arquivo (0 = nunca). */
    fun ultimoBackup(): Long = prefs.getLong(KEY_ULTIMO_BACKUP, 0L)

    fun registrarBackup(agora: Long = System.currentTimeMillis()) {
        prefs.edit { putLong(KEY_ULTIMO_BACKUP, agora) }
    }

    /** Até quando o lembrete de backup fica escondido depois de "Agora não". */
    fun lembreteBackupAdiadoAte(): Long = prefs.getLong(KEY_LEMBRETE_ADIADO, 0L)

    fun adiarLembreteBackup(ate: Long) {
        prefs.edit { putLong(KEY_LEMBRETE_ADIADO, ate) }
    }

    /** Primeira vez que o app abriu com esta função (serve de referência quando nunca houve backup). */
    fun inicioContagem(agora: Long = System.currentTimeMillis()): Long {
        val salvo = prefs.getLong(KEY_INICIO_CONTAGEM, 0L)
        if (salvo != 0L) return salvo
        prefs.edit { putLong(KEY_INICIO_CONTAGEM, agora) }
        return agora
    }

    fun ultimaBuscaEpisodios(): Long = prefs.getLong(KEY_ULTIMA_BUSCA_EPISODIOS, 0L)

    fun registrarBuscaEpisodios(agora: Long = System.currentTimeMillis()) {
        prefs.edit { putLong(KEY_ULTIMA_BUSCA_EPISODIOS, agora) }
    }

    /** Episódios novos já dispensados, por série (id da série → total de episódios no TMDB naquele momento). */
    fun episodiosDispensados(): Map<String, Int> {
        val json = runCatching { JSONObject(prefs.getString(KEY_DISPENSADOS, "{}") ?: "{}") }.getOrNull() ?: return emptyMap()
        return json.keys().asSequence().associateWith { json.optInt(it) }
    }

    fun dispensarEpisodios(serieId: String, totalTmdb: Int) {
        val atual = episodiosDispensados().toMutableMap()
        atual[serieId] = totalTmdb
        prefs.edit { putString(KEY_DISPENSADOS, JSONObject(atual as Map<*, *>).toString()) }
    }

    /** Dia (época, em dias) em que cada episódio foi concluído. Chave: id do episódio. */
    fun conclusoes(): Map<String, Long> {
        val json = runCatching { JSONObject(prefs.getString(KEY_CONCLUSOES, "{}") ?: "{}") }.getOrNull() ?: return emptyMap()
        return json.keys().asSequence().associateWith { json.optLong(it) }
    }

    fun registrarConclusao(episodioId: String, dia: Long) {
        val atual = conclusoes().toMutableMap()
        if (atual.containsKey(episodioId)) return
        atual[episodioId] = dia
        prefs.edit { putString(KEY_CONCLUSOES, JSONObject(atual as Map<*, *>).toString()) }
    }

    /** Junta os dias de conclusão vindos de um backup (o que já existe no aparelho continua valendo). */
    fun mesclarConclusoes(doBackup: Map<String, Long>) {
        if (doBackup.isEmpty()) return
        val atual = doBackup + conclusoes()
        prefs.edit { putString(KEY_CONCLUSOES, JSONObject(atual as Map<*, *>).toString()) }
    }

    /** Modo maratona: Concluir abre o próximo episódio em vez de voltar para a temporada. */
    fun maratona(): Boolean = prefs.getBoolean(KEY_MARATONA, false)

    fun salvarMaratona(ligado: Boolean) {
        prefs.edit { putBoolean(KEY_MARATONA, ligado) }
    }

    /** Séries cuja duração dos episódios já foi buscada no TMDB (busca uma vez só). */
    fun duracaoBuscada(serieId: String): Boolean = serieId in prefs.getStringSet(KEY_DURACOES, emptySet()).orEmpty()

    /** Códigos do IMDb/TVDB já conferidos no TMDB (uma vez por série, a partir da 1.10). */
    fun codigosConferidos(serieId: String): Boolean = serieId in prefs.getStringSet(KEY_CODIGOS, emptySet()).orEmpty()

    fun marcarCodigosConferidos(serieId: String) {
        prefs.edit { putStringSet(KEY_CODIGOS, prefs.getStringSet(KEY_CODIGOS, emptySet()).orEmpty() + serieId) }
    }

    fun marcarDuracaoBuscada(serieId: String) {
        prefs.edit { putStringSet(KEY_DURACOES, prefs.getStringSet(KEY_DURACOES, emptySet()).orEmpty() + serieId) }
    }

    /** Aviso de estreia (notificação no dia) para as séries da aba Em breve. */
    fun avisoEstreia(): Boolean = prefs.getBoolean(KEY_AVISO_ESTREIA, false)

    fun salvarAvisoEstreia(ligado: Boolean) {
        prefs.edit { putBoolean(KEY_AVISO_ESTREIA, ligado) }
    }

    private companion object {
        const val KEY_AVISO_ESTREIA = "aviso_estreia"
        const val KEY_DURACOES = "duracoes_buscadas"
        const val KEY_MARATONA = "modo_maratona"
        const val PREFS_NAME = "novidades_prefs"
        const val KEY_ULTIMO_BACKUP = "ultimo_backup"
        const val KEY_LEMBRETE_ADIADO = "lembrete_backup_adiado"
        const val KEY_INICIO_CONTAGEM = "inicio_contagem"
        const val KEY_ULTIMA_BUSCA_EPISODIOS = "ultima_busca_episodios"
        const val KEY_DISPENSADOS = "episodios_dispensados"
        const val KEY_CODIGOS = "codigos_conferidos"
        const val KEY_CONCLUSOES = "conclusoes"
    }
}
