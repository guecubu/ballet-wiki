package com.gf.ballet_wiki.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "europeana_records")
data class EuropeanaEntity(
        @PrimaryKey
        @ColumnInfo(name = "europeana_id")
        val europeanaId: String,

        @ColumnInfo(name = "work_id")
        val workId: Int,

        @ColumnInfo(name = "title")
        val title: String,

        @ColumnInfo(name = "creator")
        val creator: String? = null,

        @ColumnInfo(name = "data_provider")
        val dataProvider: String? = null,

        @ColumnInfo(name = "rights_statement")
        val rightsStatement: String? = null,

        @ColumnInfo(name = "is_shown_at_url")
        val isShownAtUrl: String? = null,

        @ColumnInfo(name = "is_shown_by_url")
        val isShownByUrl: String? = null,

        @ColumnInfo(name = "preview_url")
        val previewUrl: String? = null,

        @ColumnInfo(name = "media_type")
        val mediaType: String = "IMAGE"
                          )
