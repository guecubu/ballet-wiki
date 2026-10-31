package com.gf.ballet_wiki.data.repository

import android.content.Context
import android.util.Log
import com.gf.ballet_wiki.BuildConfig
import com.gf.ballet_wiki.data.local.AppDatabase
import com.gf.ballet_wiki.data.local.ComposerDao
import com.gf.ballet_wiki.data.local.ComposerEntity
import com.gf.ballet_wiki.data.local.EuropeanaDao
import com.gf.ballet_wiki.data.local.EuropeanaEntity
import com.gf.ballet_wiki.data.local.MultimediaDao
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
    private val composerDao: ComposerDao,
    private val europeanaDao: EuropeanaDao,
    private val multimediaDao: MultimediaDao,
    private val sessionManager: SessionManager
) {

    companion object {
        private const val RAW_TAG = "BALLET_API_RAW"

        @Volatile
        private var instance: BalletRepository? = null

        fun getInstance(context: Context): BalletRepository {
            return instance ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                instance ?: BalletRepository(
                    balletApi = RetrofitClient.balletApiService,
                    europeanaApi = RetrofitClient.europeanaApiService,
                    openOpusApi = RetrofitClient.openOpusApiService,
                    noteDao = db.noteDao(),
                    composerDao = db.composerDao(),
                    europeanaDao = db.europeanaDao(),
                    multimediaDao = db.multimediaDao(),
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

    // --- Remote + Local Room: Europeana API ---

    suspend fun getEuropeanaImages(
            searchTerm: String, workId: Int = -1
                                  ): Result<List<EuropeanaItemDto>> = withContext(Dispatchers.IO) {
        if (searchTerm.isBlank()) return@withContext Result.success(emptyList())
        try {
            val apiKey = BuildConfig.EUROPEANA_API_KEY
            val filteredQuery = "$searchTerm AND (TYPE:IMAGE OR TYPE:VIDEO OR TYPE:SOUND)"
            val response = europeanaApi.searchEuropeana(apiKey = apiKey, query = filteredQuery)
            if (response.success) {
                val items = response.items
                Log.i(RAW_TAG, "EUROPEANA REMOTE RESPONSE (${items.size} items): $items")

                // Map and save to Room
                if (workId != -1 && items.isNotEmpty()) {
                    val entities = items.mapIndexed { idx, item ->
                        EuropeanaEntity(
                                europeanaId = item.id.ifBlank { "eur_${workId}_$idx" },
                                workId = workId,
                                title = item.title?.firstOrNull().orEmpty(),
                                creator = item.dcCreator?.firstOrNull(),
                                dataProvider = item.dataProvider?.firstOrNull()
                                               ?: item.provider?.firstOrNull(),
                                license = item.rights?.firstOrNull(),
                                sourceUrl = item.isShownAt?.firstOrNull(),
                                highResUrl = item.isShownBy?.firstOrNull(),
                                previewUrl = item.edmPreview?.firstOrNull(),
                                mediaType = item.type ?: "IMAGE",
                                year = item.year?.firstOrNull(),
                                country = item.country?.firstOrNull(),
                                europeanaPageUrl = item.guid
                                       )
                    }
                    europeanaDao.insertRecords(entities)
                    Log.i(RAW_TAG, "EUROPEANA SAVED TO ROOM (${entities.size} entities): $entities")
                }

                Result.success(items)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Log.e(RAW_TAG, "EUROPEANA FETCH ERROR: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getCachedEuropeanaRecords(workId: Int): List<EuropeanaEntity> = withContext(
            Dispatchers.IO
                                                                                           ) {
        val records = europeanaDao.getRecordsForWork(workId)
        Log.i(RAW_TAG, "EUROPEANA READ FROM ROOM CACHE (workId=$workId): $records")
        records
    }

    // --- Remote + Local Room: Open Opus API ---

    suspend fun searchComposer(composerName: String): Result<List<OpenOpusComposerDto>> = withContext(Dispatchers.IO) {
        if (composerName.isBlank()) return@withContext Result.success(emptyList())
        try {
            val response = openOpusApi.searchComposer(composerName)
            val composers = response.composers
            Log.i(RAW_TAG, "OPEN OPUS REMOTE RESPONSE (${composers.size} composers): $composers")

            // Map and save first composer to Room
            if (composers.isNotEmpty()) {
                val comp = composers.first()
                val entity = ComposerEntity(
                        composerId = comp.id,
                        name = comp.name,
                        completeName = comp.completeName,
                        birth = comp.birth,
                        death = comp.death,
                        epoch = comp.epoch,
                        portrait = comp.portrait
                                           )
                composerDao.insertComposer(entity)
                Log.i(RAW_TAG, "OPEN OPUS SAVED TO ROOM: $entity")
            }

            Result.success(composers)
        } catch (e: Exception) {
            Log.e(RAW_TAG, "OPEN OPUS FETCH ERROR: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getCachedComposer(composerName: String): ComposerEntity? = withContext(
            Dispatchers.IO
                                                                                      ) {
        val cached = composerDao.getComposerByName(composerName)
        Log.i(RAW_TAG, "OPEN OPUS READ FROM ROOM CACHE (query=$composerName): $cached")
        cached
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
        if (workId == -1) noteDao.getAllNotes() else noteDao.getNotesForWork(workId)
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
