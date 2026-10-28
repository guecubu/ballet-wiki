package com.gf.ballet_wiki.data.repository

import android.content.Context
import com.gf.ballet_wiki.BuildConfig
import com.gf.ballet_wiki.data.local.AppDatabase
import com.gf.ballet_wiki.data.local.NoteDao
import com.gf.ballet_wiki.data.local.NoteEntity
import com.gf.ballet_wiki.data.remote.api.BalletApiService
import com.gf.ballet_wiki.data.remote.api.EuropeanaApiService
import com.gf.ballet_wiki.data.remote.api.OpenOpusApiService
import com.gf.ballet_wiki.data.remote.api.RetrofitClient
import com.gf.ballet_wiki.data.remote.dto.BalletDto
import com.gf.ballet_wiki.data.remote.dto.EuropeanaItemDto
import com.gf.ballet_wiki.data.remote.dto.OpenOpusComposerDto
import com.gf.ballet_wiki.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BalletRepository(
    private val balletApi: BalletApiService = RetrofitClient.balletApiService,
    private val europeanaApi: EuropeanaApiService = RetrofitClient.europeanaApiService,
    private val openOpusApi: OpenOpusApiService = RetrofitClient.openOpusApiService,
    private val noteDao: NoteDao,
    private val sessionManager: SessionManager
) {

    companion object {
        @Volatile
        private var instance: BalletRepository? = null

        fun getInstance(context: Context): BalletRepository {
            return instance ?: synchronized(this) {
                instance ?: BalletRepository(
                    balletApi = RetrofitClient.balletApiService,
                    europeanaApi = RetrofitClient.europeanaApiService,
                    openOpusApi = RetrofitClient.openOpusApiService,
                    noteDao = AppDatabase.getInstance(context).noteDao(),
                    sessionManager = SessionManager.getInstance(context)
                ).also { instance = it }
            }
        }
    }

    // --- Remote: Ballet API ---

    suspend fun getBallets(): Result<List<BalletDto>> = withContext(Dispatchers.IO) {
        try {
            val list = balletApi.getBallets()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBalletById(id: Int): Result<BalletDto> = withContext(Dispatchers.IO) {
        try {
            val item = balletApi.getBalletById(id)
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Remote: Europeana API (Extra Images) ---

    suspend fun getEuropeanaImages(searchTerm: String): Result<List<EuropeanaItemDto>> = withContext(Dispatchers.IO) {
        if (searchTerm.isBlank()) return@withContext Result.success(emptyList())
        try {
            val apiKey = BuildConfig.EUROPEANA_API_KEY
            val response = europeanaApi.searchEuropeana(apiKey = apiKey, query = searchTerm)
            if (response.success) {
                Result.success(response.items)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Remote: Open Opus API (Composer Info) ---

    suspend fun searchComposer(composerName: String): Result<List<OpenOpusComposerDto>> = withContext(Dispatchers.IO) {
        if (composerName.isBlank()) return@withContext Result.success(emptyList())
        try {
            val response = openOpusApi.searchComposer(composerName)
            Result.success(response.composers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Local: SessionManager (Favorites & Preferences) ---

    fun isFavorite(workId: Int): Boolean = sessionManager.isFavorite(workId)

    fun toggleFavorite(workId: Int): Boolean = sessionManager.toggleFavorite(workId)

    fun setFavorite(workId: Int, isFav: Boolean) = sessionManager.setFavorite(workId, isFav)

    fun getFavoriteIds(): Set<String> = sessionManager.getFavoriteIds()

    fun getLastViewedWorkId(): Int = sessionManager.getLastViewedWorkId()

    fun setLastViewedWorkId(workId: Int) = sessionManager.setLastViewedWorkId(workId)

    // --- Local: Room (Notes CRUD) ---

    suspend fun getNotesForWork(workId: Int): List<NoteEntity> = withContext(Dispatchers.IO) {
        noteDao.getNotesForWork(workId)
    }

    suspend fun getNoteById(noteId: Long): NoteEntity? = withContext(Dispatchers.IO) {
        noteDao.getNoteById(noteId)
    }

    suspend fun insertNote(note: NoteEntity): Long = withContext(Dispatchers.IO) {
        noteDao.insertNote(note)
    }

    suspend fun updateNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(note)
    }

    suspend fun deleteNoteById(noteId: Long) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(noteId)
    }
}
