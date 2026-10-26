package com.kalidorabr.bancodeminutagens.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/** Versão nova publicada no GitHub. [link] baixa o arquivo do app. */
data class VersaoNova(val versao: String, val link: String, val notas: String)

/**
 * Confere no GitHub (projeto público, sem chave) se existe uma versão mais nova do app.
 * O app baixa o arquivo, mas quem instala é o próprio Android, depois que o usuário confirma.
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

    suspend fun verificar(versaoAtual: String): VersaoNova? = procurar(versaoAtual).getOrNull()

    /** Como [verificar], mas separa "já está na mais recente" (sucesso com null) de "não deu para conferir" (falha). */
    suspend fun procurar(versaoAtual: String): Result<VersaoNova?> = withContext(Dispatchers.IO) {
        runCatching {
            val req = Request.Builder().url(ULTIMA).header("Accept", "application/vnd.github+json").build()
            client.newCall(req).execute().use { r ->
                if (!r.isSuccessful) error("GitHub respondeu ${r.code}")
                val j = JSONObject(r.body?.string().orEmpty())
                val tag = j.optString("tag_name")
                if (tag.isBlank()) error("Sem versão publicada")
                if (!maisNova(tag, versaoAtual)) return@runCatching null
                val assets = j.optJSONArray("assets")
                val apk = (0 until (assets?.length() ?: 0)).map { assets!!.getJSONObject(it) }
                    .firstOrNull { it.optString("name").endsWith(".apk") }
                    ?.optString("browser_download_url")
                    ?: j.optString("html_url")
                VersaoNova(tag.removePrefix("v"), apk, j.optString("body"))
            }
        }
    }

    /** O link aponta direto para o arquivo do app (dá para baixar dentro do app)? */
    fun linkDeArquivo(link: String): Boolean = link.endsWith(".apk", ignoreCase = true)

    private val clientDownload = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /** Baixa o arquivo da versão nova para a pasta temporária do app. [aoProgresso] recebe de 0 a 100. */
    suspend fun baixar(ctx: Context, link: String, aoProgresso: (Int) -> Unit): File = withContext(Dispatchers.IO) {
        val pasta = File(ctx.cacheDir, "atualizacao").apply { mkdirs() }
        pasta.listFiles()?.forEach { it.delete() } // tira downloads antigos
        val destino = File(pasta, "banco-de-minutagens.apk")
        val parcial = File(pasta, "baixando.tmp")
        clientDownload.newCall(Request.Builder().url(link).build()).execute().use { r ->
            if (!r.isSuccessful) error("Download falhou (${r.code})")
            val corpo = r.body ?: error("Download vazio")
            val total = corpo.contentLength()
            var lido = 0L
            var ultimo = -1
            corpo.byteStream().use { entrada ->
                parcial.outputStream().use { saida ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        ensureActive()
                        val n = entrada.read(buffer)
                        if (n < 0) break
                        saida.write(buffer, 0, n)
                        lido += n
                        if (total > 0) {
                            val pct = (lido * 100 / total).toInt()
                            if (pct != ultimo) { ultimo = pct; withContext(Dispatchers.Main) { aoProgresso(pct) } }
                        }
                    }
                }
            }
        }
        if (!parcial.renameTo(destino)) error("Não deu para salvar o arquivo")
        destino
    }

    /** O Android já deixa este app instalar atualizações? */
    fun podeInstalar(ctx: Context): Boolean = ctx.packageManager.canRequestPackageInstalls()

    /** Abre a tela do Android para permitir que este app instale atualizações. */
    fun pedirPermissao(ctx: Context) {
        ctx.startActivity(
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${ctx.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    /** Entrega o arquivo baixado ao instalador do Android (ele pede a confirmação do usuário). */
    fun instalar(ctx: Context, arquivo: File) {
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.arquivos", arquivo)
        ctx.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
