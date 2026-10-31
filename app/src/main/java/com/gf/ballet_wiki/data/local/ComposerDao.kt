package com.gf.ballet_wiki.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ComposerDao {

    @Query("SELECT * FROM composer_records WHERE composer_id = :composerId")
    suspend fun getComposerById(composerId: String): ComposerEntity?

    @Query("SELECT * FROM composer_records WHERE name LIKE '%' || :name || '%' OR complete_name LIKE '%' || :name || '%' LIMIT 1")
    suspend fun getComposerByName(name: String): ComposerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComposer(composer: ComposerEntity)
}
