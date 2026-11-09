package com.gf.ballet_wiki.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.gf.ballet_wiki.R

class SessionManager(context: Context) {

    private val context: Context = context.applicationContext
    private val prefs: SharedPreferences =
            this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "ballet_session_prefs"
        private const val KEY_FAVORITES = "key_favorite_ids"
        private const val KEY_LAST_VIEWED_ID = "key_last_viewed_work_id"
        private const val KEY_SAVED_STYLES = "key_saved_styles"
        private const val KEY_STYLE_TRANSLATIONS = "key_style_translations"
        private const val KEY_TEXT_TRANSLATIONS = "key_text_translations"
        private const val KEY_EUROPEANA_TIMESTAMP = "key_europeana_time"

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun getFavoriteIds(): Set<String> = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()

    fun isFavorite(workId: Int): Boolean = getFavoriteIds().contains(workId.toString())

    fun toggleFavorite(workId: Int): Boolean {
        val currentFavorites = getFavoriteIds().toMutableSet()
        val idString = workId.toString()
        val isNowFavorite = !currentFavorites.remove(idString)
        if (isNowFavorite) currentFavorites.add(idString)
        prefs.edit { putStringSet(KEY_FAVORITES, currentFavorites) }
        return isNowFavorite
    }

    fun setFavorite(workId: Int, isFav: Boolean) {
        val currentFavorites = getFavoriteIds().toMutableSet()
        val idString = workId.toString()
        if (isFav) currentFavorites.add(idString) else currentFavorites.remove(idString)
        prefs.edit { putStringSet(KEY_FAVORITES, currentFavorites) }
    }

    fun getLastViewedWorkId(): Int {
        return prefs.getInt(KEY_LAST_VIEWED_ID, -1)
    }

    fun setLastViewedWorkId(workId: Int) {
        prefs.edit { putInt(KEY_LAST_VIEWED_ID, workId) }
    }

    fun getSavedStyles(): Set<String> {
        return prefs.getStringSet(KEY_SAVED_STYLES, emptySet()) ?: emptySet()
    }

    fun saveStyles(styles: Set<String>) {
        prefs.edit { putStringSet(KEY_SAVED_STYLES, styles) }
    }

    fun getStyleTranslations(): Map<String, String> {
        val set = prefs.getStringSet(KEY_STYLE_TRANSLATIONS, emptySet()) ?: emptySet()
        val map = mutableMapOf<String, String>()
        for (item in set) {
            val parts = item.split(":", limit = 2)
            if (parts.size == 2) {
                map[parts[0]] = parts[1]
            }
        }
        return map
    }

    fun saveStyleTranslation(rawStyle: String, translatedLabel: String) {
        val current = getStyleTranslations().toMutableMap()
        current[rawStyle.lowercase().trim()] = translatedLabel
        val set = current.map { "${it.key}:${it.value}" }.toSet()
        prefs.edit { putStringSet(KEY_STYLE_TRANSLATIONS, set) }
    }

    fun getStyleTranslation(rawStyle: String): String {
        val key = rawStyle.lowercase().trim()
        val cachedMap = getStyleTranslations()
        val cached = cachedMap[key]
        if (!cached.isNullOrBlank()) {
            return cached
        }
        val label = when (key) {
            "classical" -> context.getString(R.string.style_classical)
            "romantic" -> context.getString(R.string.style_romantic)
            "neoclassical" -> context.getString(R.string.style_neoclassical)
            "contemporary" -> context.getString(R.string.style_contemporary)
            "modern / avant-garde" -> context.getString(R.string.style_avant_garde)
            "modernist" -> context.getString(R.string.style_modernist)
            else -> rawStyle
        }
        saveStyleTranslation(rawStyle, label)
        return label
    }

    fun getCachedTranslation(sourceText: String, languageTag: String): String? {
        val map = getTextTranslations()
        val key = "${languageTag}_${sourceText.hashCode()}"
        return map[key]
    }

    fun cacheTranslation(sourceText: String, languageTag: String, translatedText: String) {
        val current = getTextTranslations().toMutableMap()
        val key = "${languageTag}_${sourceText.hashCode()}"
        current[key] = translatedText
        val set = current.map { "${it.key}:${it.value}" }.toSet()
        prefs.edit { putStringSet(KEY_TEXT_TRANSLATIONS, set) }
    }

    private fun getTextTranslations(): Map<String, String> {
        val set = prefs.getStringSet(KEY_TEXT_TRANSLATIONS, emptySet()) ?: emptySet()
        val map = mutableMapOf<String, String>()
        for (item in set) {
            val parts = item.split(":", limit = 2)
            if (parts.size == 2) {
                map[parts[0]] = parts[1]
            }
        }
        return map
    }

    fun getEuropeanaLastQueryTime(workId: Int): Long {
        return prefs.getLong("${KEY_EUROPEANA_TIMESTAMP}_$workId", 0L)
    }

    fun setEuropeanaLastQueryTime(workId: Int, timestamp: Long) {
        prefs.edit { putLong("${KEY_EUROPEANA_TIMESTAMP}_$workId", timestamp) }
    }
}
