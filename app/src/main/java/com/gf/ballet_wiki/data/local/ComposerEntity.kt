package com.gf.ballet_wiki.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "composer_records")
data class ComposerEntity(
        @PrimaryKey
        @ColumnInfo(name = "composer_id")
        val composerId: String,

        @ColumnInfo(name = "name")
        val name: String,

        @ColumnInfo(name = "complete_name")
        val completeName: String,

        @ColumnInfo(name = "birth")
        val birth: String? = null,

        @ColumnInfo(name = "death")
        val death: String? = null,

        @ColumnInfo(name = "epoch")
        val epoch: String? = null,

        @ColumnInfo(name = "portrait")
        val portrait: String? = null
                         )
