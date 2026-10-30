package com.gf.ballet_wiki.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EuropeanaDao {

    @Query("SELECT * FROM europeana_records WHERE work_id = :workId")
    suspend fun getRecordsForWork(workId: Int): List<EuropeanaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<EuropeanaEntity>)

    @Query("DELETE FROM europeana_records WHERE work_id = :workId")
    suspend fun deleteRecordsForWork(workId: Int)
}
