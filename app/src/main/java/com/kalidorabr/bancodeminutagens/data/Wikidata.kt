package com.kalidorabr.bancodeminutagens.data

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Wikidata (enciclopédia aberta, sem chave): guarda os códigos de cada série em vários sites.
 * Usado só quando o TMDB não tem o código do TVDB (comum em séries japonesas). Só lê.
 */
object Wikidata {
    private const val TVDB_SERIE = "P4835" // "TheTVDB.com series ID"
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /** Código do TVDB do item [qid] (ex.: "Q15548228"), ou null se não tiver. */
    suspend fun tvdbId(qid: String): String? = withContext(Dispatchers.IO) {
        val id = qid.trim()
        if (!id.matches(Regex("Q\\d+"))) return@withContext null
        try {
            val req = Request.Builder()
                .url("https://www.wikidata.org/wiki/Special:EntityData/$id.json")
                // o Wikidata pede que cada app se identifique
                .header("User-Agent", "BancoDeMinutagens (https://github.com/KalidoraBR/banco-de-minutagens)")
                .build()
            client.newCall(req).execute().use { r ->
                if (!r.isSuccessful) return@withContext null
                val entidade = JSONObject(r.body?.string().orEmpty()).optJSONObject("entities")
                    ?.let { ents -> ents.keys().asSequence().firstOrNull()?.let { ents.optJSONObject(it) } }
                    ?: return@withContext null
                val valores = entidade.optJSONObject("claims")?.optJSONArray(TVDB_SERIE) ?: return@withContext null
                (0 until valores.length()).asSequence()
                    .mapNotNull { valores.optJSONObject(it)?.optJSONObject("mainsnak")?.optJSONObject("datavalue")?.optString("value") }
                    .firstOrNull { it.isNotBlank() && it.all(Char::isDigit) }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }
}
