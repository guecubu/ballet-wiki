package com.gf.ballet_wiki.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ComposerDao {

    @Query("SELECT * FROM composer_records WHERE composer_id = :composerId")
    suspend fun getComposerById(composerId: String): ComposerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComposer(composer: ComposerEntity)
}
