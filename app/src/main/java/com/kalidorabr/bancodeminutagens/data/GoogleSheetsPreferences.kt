package com.kalidorabr.bancodeminutagens.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

data class PendingClearItem(
    val tmdbId: String,
    val seriesTitle: String,
    val episodes: List<PendingClearEpisode>
)

data class PendingClearEpisode(
    val season: String,
    val seasonNumber: Int,
    val episode: String,
    val episodeNumber: Int,
    val title: String = ""
)

data class GoogleSheetsConfig(
    val spreadsheetId: String = "",
    val scriptUrl: String = "",
    val lastSyncDate: String? = null,
    val lastSyncStatus: String? = null,
    val isAutoSyncUnlocked: Boolean = false,
    val isAutoSyncEnabled: Boolean = false,
    val hasPendingExport: Boolean = false,
    val isConnectionValidated: Boolean = false
)

class GoogleSheetsPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getConfig(): GoogleSheetsConfig {
        return GoogleSheetsConfig(
            spreadsheetId = prefs.getString(KEY_SPREADSHEET_ID, "") ?: "",
            scriptUrl = prefs.getString(KEY_SCRIPT_URL, "") ?: "",
            lastSyncDate = prefs.getString(KEY_LAST_SYNC_DATE, null),
            lastSyncStatus = prefs.getString(KEY_LAST_SYNC_STATUS, null),
            isAutoSyncUnlocked = prefs.getBoolean(KEY_AUTO_SYNC_UNLOCKED, false),
            isAutoSyncEnabled = prefs.getBoolean(KEY_AUTO_SYNC_ENABLED, false),
            hasPendingExport = prefs.getBoolean(KEY_PENDING_EXPORT, false),
            isConnectionValidated = prefs.getBoolean(KEY_CONNECTION_VALIDATED, false)
        )
    }

    fun saveConfig(spreadsheetId: String, scriptUrl: String) {
        prefs.edit {
            putString(KEY_SPREADSHEET_ID, spreadsheetId.trim())
            putString(KEY_SCRIPT_URL, scriptUrl.trim())
        }
    }

    fun saveConnectionValidated(validated: Boolean) {
        prefs.edit {
            putBoolean(KEY_CONNECTION_VALIDATED, validated)
        }
    }

    fun unlockAndEnableAutoSync() {
        prefs.edit {
            putBoolean(KEY_AUTO_SYNC_UNLOCKED, true)
            putBoolean(KEY_AUTO_SYNC_ENABLED, true)
            putBoolean(KEY_CONNECTION_VALIDATED, true)
        }
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_AUTO_SYNC_ENABLED, enabled)
        }
    }

    /** [limparEpisodios] = false quando nada foi de fato enviado à planilha (os episódios continuam pendentes). */
    fun setPendingExport(pending: Boolean, limparEpisodios: Boolean = true) {
        prefs.edit {
            putBoolean(KEY_PENDING_EXPORT, pending)
            if (!pending && limparEpisodios) remove(KEY_EPISODIOS_PENDENTES)
        }
    }

    /** Episódios alterados depois da última sincronização (cartão "Planilha" da tela do episódio). */
    fun episodiosPendentes(): Set<String> = prefs.getStringSet(KEY_EPISODIOS_PENDENTES, emptySet()).orEmpty()

    fun marcarEpisodiosPendentes(ids: Collection<String>) {
        if (ids.isEmpty()) return
        prefs.edit { putStringSet(KEY_EPISODIOS_PENDENTES, episodiosPendentes() + ids) }
    }

    fun getPermanentlyDeletedTmdbIds(): Set<String> {
        return prefs.getStringSet(KEY_PERMANENTLY_DELETED_IDS, emptySet()) ?: emptySet()
    }

    fun addPermanentlyDeletedTmdbId(tmdbId: String) {
        val normalized = normalizeTmdbId(tmdbId)
        if (normalized.isBlank()) return
        val currentSet = getPermanentlyDeletedTmdbIds().toMutableSet()
        currentSet.add(normalized)
        prefs.edit { putStringSet(KEY_PERMANENTLY_DELETED_IDS, currentSet) }
    }

    fun removePermanentlyDeletedTmdbId(tmdbId: String) {
        val normalized = normalizeTmdbId(tmdbId)
        if (normalized.isBlank()) return
        val currentSet = getPermanentlyDeletedTmdbIds().toMutableSet()
        currentSet.remove(normalized)
        prefs.edit { putStringSet(KEY_PERMANENTLY_DELETED_IDS, currentSet) }
    }

    fun isPermanentlyDeleted(tmdbId: String): Boolean {
        val normalized = normalizeTmdbId(tmdbId)
        if (normalized.isBlank()) return false
        return getPermanentlyDeletedTmdbIds().contains(normalized)
    }

    fun saveLastSync(date: String, status: String) {
        prefs.edit {
            putString(KEY_LAST_SYNC_DATE, date)
            putString(KEY_LAST_SYNC_STATUS, status)
        }
    }

    fun getPendingClearOperations(): List<PendingClearItem> {
        val jsonStr = prefs.getString(KEY_PENDING_CLEAR_ITEMS, null) ?: return emptyList()
        if (jsonStr.isBlank()) return emptyList()

        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<PendingClearItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val tmdbId = obj.optString("tmdbId", "")
                val seriesTitle = obj.optString("seriesTitle", "")
                val epArr = obj.optJSONArray("episodes") ?: JSONArray()
                val epList = mutableListOf<PendingClearEpisode>()
                for (j in 0 until epArr.length()) {
                    val epObj = epArr.getJSONObject(j)
                    epList.add(
                        PendingClearEpisode(
                            season = epObj.optString("season", "T01"),
                            seasonNumber = epObj.optInt("seasonNumber", 1),
                            episode = epObj.optString("episode", "E01"),
                            episodeNumber = epObj.optInt("episodeNumber", 1),
                            title = epObj.optString("title", "")
                        )
                    )
                }
                if (tmdbId.isNotBlank()) {
                    list.add(PendingClearItem(tmdbId, seriesTitle, epList))
                }
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addPendingClearOperation(newItem: PendingClearItem) {
        val currentList = getPendingClearOperations().toMutableList()
        val index = currentList.indexOfFirst { normalizeTmdbId(it.tmdbId) == normalizeTmdbId(newItem.tmdbId) }
        if (index >= 0) {
            val existing = currentList[index]
            val mergedEps = (existing.episodes + newItem.episodes).distinctBy { "${it.seasonNumber}_${it.episodeNumber}" }
            currentList[index] = existing.copy(episodes = mergedEps)
        } else {
            currentList.add(newItem)
        }
        savePendingClearOperations(currentList)
        setPendingExport(true)
    }

    fun removePendingClearOperation(tmdbId: String) {
        val currentList = getPendingClearOperations().filter {
            normalizeTmdbId(it.tmdbId) != normalizeTmdbId(tmdbId)
        }
        savePendingClearOperations(currentList)
        if (currentList.isEmpty()) {
            setPendingExport(false, limparEpisodios = false)
        }
    }

    fun clearPendingClearOperations() {
        prefs.edit { remove(KEY_PENDING_CLEAR_ITEMS) }
    }

    private fun savePendingClearOperations(items: List<PendingClearItem>) {
        try {
            val arr = JSONArray()
            for (item in items) {
                val obj = JSONObject()
                obj.put("tmdbId", item.tmdbId)
                obj.put("seriesTitle", item.seriesTitle)
                val epArr = JSONArray()
                for (ep in item.episodes) {
                    val epObj = JSONObject()
                    epObj.put("season", ep.season)
                    epObj.put("seasonNumber", ep.seasonNumber)
                    epObj.put("episode", ep.episode)
                    epObj.put("episodeNumber", ep.episodeNumber)
                    epObj.put("title", ep.title)
                    epArr.put(epObj)
                }
                obj.put("episodes", epArr)
                arr.put(obj)
            }
            prefs.edit { putString(KEY_PENDING_CLEAR_ITEMS, arr.toString()) }
        } catch (_: Exception) {}
    }

    fun clearConfig() {
        prefs.edit { clear() }
    }

    private fun normalizeTmdbId(raw: String): String {
        var str = raw.trim()
        if (str.endsWith(".0")) {
            str = str.substring(0, str.length - 2)
        }
        return str
    }

    companion object {
        private const val PREFS_NAME = "google_sheets_config_prefs"
        private const val KEY_SPREADSHEET_ID = "spreadsheet_id"
        private const val KEY_SCRIPT_URL = "script_url"
        private const val KEY_LAST_SYNC_DATE = "last_sync_date"
        private const val KEY_LAST_SYNC_STATUS = "last_sync_status"
        private const val KEY_AUTO_SYNC_UNLOCKED = "auto_sync_unlocked"
        private const val KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled"
        private const val KEY_PENDING_EXPORT = "pending_export"
        private const val KEY_EPISODIOS_PENDENTES = "episodios_pendentes"
        private const val KEY_CONNECTION_VALIDATED = "connection_validated"
        private const val KEY_PERMANENTLY_DELETED_IDS = "permanently_deleted_ids"
        private const val KEY_PENDING_CLEAR_ITEMS = "pending_clear_items"
    }
}
