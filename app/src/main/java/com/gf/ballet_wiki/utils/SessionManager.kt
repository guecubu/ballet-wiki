package com.gf.ballet_wiki.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "ballet_session_prefs"
        private const val KEY_FAVORITES = "key_favorite_ids"
        private const val KEY_LAST_VIEWED_ID = "key_last_viewed_work_id"

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
        prefs.edit().putStringSet(KEY_FAVORITES, currentFavorites).apply()
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
        prefs.edit().putStringSet(KEY_FAVORITES, currentFavorites).apply()
    }

    fun getLastViewedWorkId(): Int {
        return prefs.getInt(KEY_LAST_VIEWED_ID, -1)
    }

    fun setLastViewedWorkId(workId: Int) {
        prefs.edit().putInt(KEY_LAST_VIEWED_ID, workId).apply()
    }
}
