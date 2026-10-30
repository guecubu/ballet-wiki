package com.gf.ballet_wiki.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "multimedia_items")
data class MultimediaEntity(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Long = 0L,

        @ColumnInfo(name = "work_id")
        val workId: Int,

        @ColumnInfo(name = "media_type")
        val mediaType: String, // "IMAGE", "VIDEO", "AUDIO"

        @ColumnInfo(name = "title")
        val title: String,

        @ColumnInfo(name = "media_url")
        val mediaUrl: String,

        @ColumnInfo(name = "thumbnail_url")
        val thumbnailUrl: String? = null,

        @ColumnInfo(name = "provider_ref_id")
        val providerRefId: String? = null // Link to europeana_id or external ref
                           )
