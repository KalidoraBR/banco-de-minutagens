package com.kalidorabr.bancodeminutagens.data

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/** Versão nova publicada no GitHub. [link] baixa o arquivo do app. */
data class VersaoNova(val versao: String, val link: String, val notas: String)

/**
 * Confere no GitHub (projeto público, sem chave) se existe uma versão mais nova do app.
 * Só lê: quem instala é o próprio Android, depois que o usuário confirma.
 */
object Atualizacao {
    private const val ULTIMA = "https://api.github.com/repos/KalidoraBR/banco-de-minutagens/releases/latest"
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /** "1.10" é mais novo que "1.9": compara número por número. */
    fun maisNova(remota: String, atual: String): Boolean {
        val r = remota.trim().removePrefix("v").split('.').map { it.toIntOrNull() ?: 0 }
        val a = atual.trim().removePrefix("v").split('.').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(r.size, a.size)) {
            val x = r.getOrElse(i) { 0 }
            val y = a.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }

    suspend fun verificar(versaoAtual: String): VersaoNova? = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url(ULTIMA).header("Accept", "application/vnd.github+json").build()
            client.newCall(req).execute().use { r ->
                if (!r.isSuccessful) return@withContext null
                val j = JSONObject(r.body?.string().orEmpty())
                val tag = j.optString("tag_name")
                if (tag.isBlank() || !maisNova(tag, versaoAtual)) return@withContext null
                val assets = j.optJSONArray("assets")
                val apk = (0 until (assets?.length() ?: 0)).map { assets!!.getJSONObject(it) }
                    .firstOrNull { it.optString("name").endsWith(".apk") }
                    ?.optString("browser_download_url")
                    ?: j.optString("html_url")
                VersaoNova(tag.removePrefix("v"), apk, j.optString("body"))
            }
        } catch (_: Exception) {
            null
        }
    }
}
