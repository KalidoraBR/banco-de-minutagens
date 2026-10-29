package com.kalidorabr.bancodeminutagens.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.kalidorabr.bancodeminutagens.model.EnvioIntroDb
import com.kalidorabr.bancodeminutagens.model.TipoMinutagem
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/** Chave do IntroDB e registro do que já foi enviado. Fica só no aparelho, separado do Google Sheets. */
class IntroDbPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("introdb_prefs", Context.MODE_PRIVATE)

    fun chave(): String = prefs.getString(KEY_CHAVE, "").orEmpty()

    fun salvarChave(chave: String) {
        prefs.edit { putString(KEY_CHAVE, chave.trim()) }
    }

    /** Já enviado antes com estes mesmos tempos? (chave: episódio + seção) */
    fun jaEnviado(item: EnvioIntroDb): Boolean =
        prefs.getString(KEY_ENVIADO + item.episodioId + "_" + item.segmentType, null) == "${item.inicioSeg}-${item.fimSeg}"

    /** Tempos ("início-fim" em segundos) da última vez que este trecho foi enviado, ou null se nunca foi. */
    fun enviadoAntes(episodioId: String, segmentType: String): String? =
        prefs.getString(KEY_ENVIADO + episodioId + "_" + segmentType, null)

    fun marcarEnviado(item: EnvioIntroDb) {
        prefs.edit { putString(KEY_ENVIADO + item.episodioId + "_" + item.segmentType, "${item.inicioSeg}-${item.fimSeg}") }
    }

    /** Envio automático ao tocar em Concluir (ligado por padrão quando há chave). */
    fun automatico(): Boolean = prefs.getBoolean(KEY_AUTO, true)

    fun salvarAutomatico(ligado: Boolean) {
        prefs.edit { putBoolean(KEY_AUTO, ligado) }
    }

    /** O que o IntroDB já tem deste episódio (tipos de seção: "intro", "recap", "outro"). */
    fun noSite(episodioId: String): Set<String> =
        prefs.getStringSet(KEY_NO_SITE + episodioId, emptySet()).orEmpty()

    fun conferido(episodioId: String): Boolean = prefs.contains(KEY_NO_SITE + episodioId)

    /** Algum trecho foi enviado (o site aceitou) mas a consulta ainda não mostra publicado: está em revisão. */
    fun emRevisao(episodioId: String): Boolean {
        val noSite = noSite(episodioId)
        return listOf("intro", "recap", "outro").any { enviadoAntes(episodioId, it) != null && it !in noSite }
    }

    fun salvarNoSite(episodioId: String, tipos: Set<String>) {
        prefs.edit {
            putStringSet(KEY_NO_SITE + episodioId, tipos)
            putLong(KEY_CONFERIDO_EM + episodioId, System.currentTimeMillis())
        }
    }

    /** Esquece o que foi conferido destes episódios (ex.: o código do IMDb da série mudou). */
    fun esquecerConferencias(episodioIds: Collection<String>) {
        prefs.edit { episodioIds.forEach { remove(KEY_NO_SITE + it); remove(KEY_CONFERIDO_EM + it) } }
    }

    /** Quando o episódio foi conferido pela última vez (0 = nunca, ou antes da 1.10). */
    fun conferidoEm(episodioId: String): Long = prefs.getLong(KEY_CONFERIDO_EM + episodioId, 0L)

    /** Conferência automática em segundo plano (ligada por padrão). */
    fun conferenciaAutomatica(): Boolean = prefs.getBoolean(KEY_CONF_AUTO, true)

    fun salvarConferenciaAutomatica(ligado: Boolean) {
        prefs.edit { putBoolean(KEY_CONF_AUTO, ligado) }
    }

    /**
     * Versão 1.9: apaga uma vez as marcas "no site" antigas (a 1.8 marcava ao enviar, mesmo que o site
     * ainda não mostrasse), para tudo ser conferido de novo. O registro do que já foi enviado fica.
     */
    fun refazerConferenciasUmaVez() {
        if (prefs.getBoolean(KEY_CONFERENCIA_V2, false)) return
        prefs.edit {
            prefs.all.keys.filter { it.startsWith(KEY_NO_SITE) }.forEach { remove(it) }
            putBoolean(KEY_CONFERENCIA_V2, true)
        }
    }

    private companion object {
        const val KEY_CONFERENCIA_V2 = "conferencia_v2"
        const val KEY_CHAVE = "api_key"
        const val KEY_ENVIADO = "enviado_"
        const val KEY_AUTO = "automatico"
        const val KEY_NO_SITE = "no_site_"
        const val KEY_CONFERIDO_EM = "conferido_em_"
        const val KEY_CONF_AUTO = "conferencia_automatica"
    }
}

sealed interface RespostaIntroDb {
    data object Ok : RespostaIntroDb
    data object ChaveInvalida : RespostaIntroDb
    data object Limite : RespostaIntroDb
    data class Erro(val mensagem: String) : RespostaIntroDb
}

object IntroDbService {
    private const val BASE = "https://api.introdb.app"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .followRedirects(false)
        .build()

    /** Envia uma seção de um episódio (POST /submit com a chave do usuário). */
    suspend fun enviar(chave: String, imdbId: String, item: EnvioIntroDb): RespostaIntroDb = withContext(Dispatchers.IO) {
        val corpo = JSONObject()
            .put("imdb_id", imdbId)
            .put("segment_type", item.segmentType)
            .put("season", item.temporada)
            .put("episode", item.episodio)
            .put("start_sec", item.inicioSeg)
            .put("end_sec", item.fimSeg)
            .toString()
        val request = Request.Builder()
            .url("$BASE/submit")
            .header("X-API-Key", chave)
            .header("Accept", "application/json")
            .post(corpo.toRequestBody("application/json".toMediaType()))
            .build()
        try {
            client.newCall(request).execute().use { r ->
                val texto = r.body?.string().orEmpty()
                when {
                    r.isSuccessful -> RespostaIntroDb.Ok
                    r.code == 401 || r.code == 403 -> RespostaIntroDb.ChaveInvalida
                    r.code == 429 -> RespostaIntroDb.Limite
                    else -> RespostaIntroDb.Erro(mensagemDe(texto) ?: "erro ${r.code}")
                }
            }
        } catch (_: IOException) {
            RespostaIntroDb.Erro("sem conexão")
        }
    }

    /**
     * Consulta (sem chave) o que o IntroDB já tem de um episódio.
     * Devolve os tipos presentes ("intro", "recap", "outro"; "não possui" confirmado também conta),
     * null quando não deu para consultar, ou "LIMITE" no conjunto quando o site pediu pausa.
     */
    suspend fun consultar(imdbId: String, temporada: Int, episodio: Int): Set<String>? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE/segments?imdb_id=$imdbId&season=$temporada&episode=$episodio")
            .header("Accept", "application/json")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { r ->
                when {
                    r.code == 404 -> emptySet()
                    r.code == 429 -> setOf(LIMITE)
                    !r.isSuccessful -> null
                    else -> {
                        val j = JSONObject(r.body?.string().orEmpty())
                        buildSet {
                            if (!j.isNull("intro") || j.optBoolean("no_intro")) add("intro")
                            if (!j.isNull("recap") || j.optBoolean("no_recap")) add("recap")
                            if (!j.isNull("outro") || j.optBoolean("no_outro")) add("outro")
                        }
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    const val LIMITE = "LIMITE"

    /** Busca (sem chave) os tempos que o IntroDB tem de um episódio, para importar no app. */
    suspend fun buscarTempos(imdbId: String, temporada: Int, episodio: Int): com.kalidorabr.bancodeminutagens.model.TemposIntroDb? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE/segments?imdb_id=$imdbId&season=$temporada&episode=$episodio")
            .header("Accept", "application/json")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { r ->
                if (!r.isSuccessful) return@withContext null
                val j = JSONObject(r.body?.string().orEmpty())
                fun seg(nome: String): Pair<Int, Int>? {
                    val o = j.optJSONObject(nome) ?: return null
                    // os milissegundos são arredondados para o segundo mais próximo, como o app guarda
                    val a = Math.round(o.optDouble("start_ms", o.optDouble("start_sec", Double.NaN) * 1000) / 1000.0).toInt()
                    val b = Math.round(o.optDouble("end_ms", o.optDouble("end_sec", Double.NaN) * 1000) / 1000.0).toInt()
                    return if (b > a && a >= 0) a to b else null
                }
                com.kalidorabr.bancodeminutagens.model.TemposIntroDb(
                    secoes = buildMap {
                        seg("intro")?.let { put(TipoMinutagem.ABERTURA, it) }
                        seg("recap")?.let { put(TipoMinutagem.RECAP, it) }
                        seg("outro")?.let { put(TipoMinutagem.ENCERRAMENTO, it) }
                    },
                    naoPossui = buildSet {
                        if (j.optBoolean("no_intro")) add(TipoMinutagem.ABERTURA)
                        if (j.optBoolean("no_recap")) add(TipoMinutagem.RECAP)
                        if (j.optBoolean("no_outro")) add(TipoMinutagem.ENCERRAMENTO)
                    }
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun mensagemDe(texto: String): String? = runCatching {
        val j = JSONObject(texto)
        listOf("error", "message", "detail").firstNotNullOfOrNull { k -> j.optString(k).takeIf { it.isNotBlank() } }
    }.getOrNull()
}
