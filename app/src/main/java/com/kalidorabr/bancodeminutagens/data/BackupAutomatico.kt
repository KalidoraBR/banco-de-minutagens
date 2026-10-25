package com.kalidorabr.bancodeminutagens.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.content.edit
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * Backup automático: uma vez por semana grava o backup completo numa pasta escolhida pelo usuário
 * e guarda só os [MANTER] mais recentes. Não mexe na sincronização com o Google Sheets.
 */
object BackupAutomatico {
    private const val PREFS = "backup_automatico_prefs"
    private const val KEY_PASTA = "pasta"
    private const val KEY_ULTIMO = "ultimo"
    private const val TRABALHO = "backup_automatico"
    private const val PREFIXO = "banco-de-minutagens-auto-"
    const val MANTER = 4

    fun pasta(ctx: Context): Uri? =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_PASTA, null)?.let(Uri::parse)

    fun ultimo(ctx: Context): Long = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_ULTIMO, 0L)

    /** Nome legível da pasta escolhida (ex.: "Download"). */
    fun nomePasta(ctx: Context): String? = pasta(ctx)?.let { uri ->
        runCatching { DocumentsContract.getTreeDocumentId(uri).substringAfterLast(':').ifBlank { "pasta escolhida" } }.getOrNull()
    }

    fun ligar(ctx: Context, pasta: Uri) {
        ctx.contentResolver.takePersistableUriPermission(
            pasta, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putString(KEY_PASTA, pasta.toString()) }
        val wm = WorkManager.getInstance(ctx)
        wm.enqueueUniquePeriodicWork(TRABALHO, ExistingPeriodicWorkPolicy.UPDATE, PeriodicWorkRequestBuilder<BackupAutomaticoWorker>(7, TimeUnit.DAYS).build())
        // e já faz o primeiro agora
        wm.enqueue(OneTimeWorkRequestBuilder<BackupAutomaticoWorker>().build())
    }

    fun desligar(ctx: Context) {
        WorkManager.getInstance(ctx).cancelUniqueWork(TRABALHO)
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.getString(KEY_PASTA, null)?.let { uri ->
            runCatching {
                ctx.contentResolver.releasePersistableUriPermission(
                    Uri.parse(uri), Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
        }
        prefs.edit { remove(KEY_PASTA) }
    }

    /** Grava um backup agora. Devolve true se deu certo. */
    suspend fun fazer(ctx: Context): Boolean {
        val arvore = pasta(ctx) ?: return false
        val repo = SeriesRepository(ctx)
        val conteudo = repo.criarBackup(repo.carregarSeries())
        return runCatching {
            val cr = ctx.contentResolver
            val pai = DocumentsContract.buildDocumentUriUsingTree(arvore, DocumentsContract.getTreeDocumentId(arvore))
            val nome = "$PREFIXO${LocalDate.now()}.json"
            // se já existe um de hoje, substitui
            filhos(ctx, arvore).filter { it.second == nome }.forEach { DocumentsContract.deleteDocument(cr, it.first) }
            val novo = DocumentsContract.createDocument(cr, pai, "application/json", nome) ?: error("não criou")
            cr.openOutputStream(novo, "wt")?.bufferedWriter()?.use { it.write(conteudo) } ?: error("não abriu")
            // guarda só os mais recentes
            filhos(ctx, arvore).filter { it.second.startsWith(PREFIXO) }
                .sortedByDescending { it.second }
                .drop(MANTER)
                .forEach { DocumentsContract.deleteDocument(cr, it.first) }
            val agora = System.currentTimeMillis()
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putLong(KEY_ULTIMO, agora) }
            NovidadesPreferences(ctx).registrarBackup(agora)
            true
        }.getOrDefault(false)
    }

    private fun filhos(ctx: Context, arvore: Uri): List<Pair<Uri, String>> {
        val lista = DocumentsContract.buildChildDocumentsUriUsingTree(arvore, DocumentsContract.getTreeDocumentId(arvore))
        val saida = mutableListOf<Pair<Uri, String>>()
        ctx.contentResolver.query(
            lista,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null, null, null
        )?.use { c ->
            while (c.moveToNext()) {
                saida += DocumentsContract.buildDocumentUriUsingTree(arvore, c.getString(0)) to (c.getString(1) ?: "")
            }
        }
        return saida
    }
}

class BackupAutomaticoWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result =
        if (BackupAutomatico.pasta(applicationContext) == null || BackupAutomatico.fazer(applicationContext)) Result.success()
        else Result.retry()
}
