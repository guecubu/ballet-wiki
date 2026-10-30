package com.gf.ballet_wiki.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MultimediaDao {

    @Query("SELECT * FROM multimedia_items WHERE work_id = :workId")
    suspend fun getAllMediaForWork(workId: Int): List<MultimediaEntity>

    @Query("SELECT * FROM multimedia_items WHERE work_id = :workId AND media_type = :mediaType")
    suspend fun getMediaByWorkAndType(workId: Int, mediaType: String): List<MultimediaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaList(mediaList: List<MultimediaEntity>)

    @Query("DELETE FROM multimedia_items WHERE work_id = :workId")
    suspend fun deleteMediaForWork(workId: Int)
}
