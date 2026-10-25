package com.kalidorabr.bancodeminutagens.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kalidorabr.bancodeminutagens.MainActivity
import com.kalidorabr.bancodeminutagens.R
import com.kalidorabr.bancodeminutagens.model.EpisodioItem
import com.kalidorabr.bancodeminutagens.model.SerieItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/** Quais séries da aba "Em breve" estreiam (têm episódio indo ao ar) hoje. */
object Estreias {
    private val formatoBr = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun deHoje(series: List<SerieItem>, hoje: LocalDate): List<Pair<SerieItem, EpisodioItem>> =
        series.filter { !it.isDeleted && it.status.equals("Em breve", ignoreCase = true) }
            .mapNotNull { serie ->
                serie.temporadas.flatMap { it.episodios }
                    .firstOrNull { ep -> runCatching { LocalDate.parse(ep.dataExibicao.trim(), formatoBr) }.getOrNull() == hoje }
                    ?.let { serie to it }
            }
}

/** Confere as estreias do dia (roda em segundo plano, mesmo com o app fechado). */
class EstreiaWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val prefs = ctx.getSharedPreferences("estreias_prefs", Context.MODE_PRIVATE)
        val hoje = LocalDate.now()
        val avisadas = prefs.getStringSet("avisadas", emptySet()).orEmpty()
        val novas = Estreias.deHoje(SeriesRepository(ctx).carregarSeries(), hoje)
            .filter { (serie, _) -> "${serie.id}@$hoje" !in avisadas }
        if (novas.isEmpty()) return Result.success()
        if (EstreiaAgenda.podeNotificar(ctx)) {
            EstreiaAgenda.criarCanal(ctx)
            val abrir = PendingIntent.getActivity(
                ctx, 0, Intent(ctx, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            novas.forEach { (serie, ep) ->
                val n = NotificationCompat.Builder(ctx, EstreiaAgenda.CANAL)
                    .setSmallIcon(R.drawable.ic_notificacao)
                    .setContentTitle("Estreia hoje: ${serie.titulo}")
                    .setContentText("Episódio ${ep.numeroFormatado} · ${ep.titulo}")
                    .setContentIntent(abrir)
                    .setAutoCancel(true)
                    .build()
                runCatching { NotificationManagerCompat.from(ctx).notify(serie.id.hashCode(), n) }
            }
        }
        // guarda só os avisos dos últimos dias
        val manter = avisadas.filter { it.substringAfter('@') >= hoje.minusDays(7).toString() }.toSet()
        prefs.edit().putStringSet("avisadas", manter + novas.map { "${it.first.id}@$hoje" }).apply()
        return Result.success()
    }
}

object EstreiaAgenda {
    const val CANAL = "estreias"
    private const val TRABALHO = "aviso_estreias"

    fun podeNotificar(ctx: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun criarCanal(ctx: Context) {
        val canal = NotificationChannel(CANAL, "Estreias", NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "Aviso no dia em que uma série da aba Em breve estreia" }
        ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    fun ligar(ctx: Context) {
        val req = PeriodicWorkRequestBuilder<EstreiaWorker>(8, TimeUnit.HOURS).build()
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(TRABALHO, ExistingPeriodicWorkPolicy.KEEP, req)
    }

    fun desligar(ctx: Context) {
        WorkManager.getInstance(ctx).cancelUniqueWork(TRABALHO)
    }
}
