package com.gf.ballet_wiki.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "ballet_session_prefs"
        private const val KEY_FAVORITES = "key_favorite_ids"
        private const val KEY_LAST_VIEWED_ID = "key_last_viewed_work_id"
        private const val KEY_SAVED_STYLES = "key_saved_styles"
        private const val KEY_STYLE_TRANSLATIONS = "key_style_translations"

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun getFavoriteIds(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun isFavorite(workId: Int): Boolean {
        return getFavoriteIds().contains(workId.toString())
    }

    fun toggleFavorite(workId: Int): Boolean {
        val currentFavorites = getFavoriteIds().toMutableSet()
        val idString = workId.toString()
        val isNowFavorite = if (currentFavorites.contains(idString)) {
            currentFavorites.remove(idString)
            false
        } else {
            currentFavorites.add(idString)
            true
        }
        prefs.edit { putStringSet(KEY_FAVORITES, currentFavorites) }
        return isNowFavorite
    }

    fun setFavorite(workId: Int, isFav: Boolean) {
        val currentFavorites = getFavoriteIds().toMutableSet()
        val idString = workId.toString()
        if (isFav) {
            currentFavorites.add(idString)
        } else {
            currentFavorites.remove(idString)
        }
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
}
