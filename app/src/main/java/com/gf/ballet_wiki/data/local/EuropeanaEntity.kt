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

        @ColumnInfo(name = "license")
        val license: String? = null,

        @ColumnInfo(name = "source_url")
        val sourceUrl: String? = null,

        @ColumnInfo(name = "high_res_url")
        val highResUrl: String? = null,

        @ColumnInfo(name = "preview_url")
        val previewUrl: String? = null,

        @ColumnInfo(name = "media_type")
        val mediaType: String = "IMAGE",

        @ColumnInfo(name = "year")
        val year: String? = null,

        @ColumnInfo(name = "country")
        val country: String? = null,

        @ColumnInfo(name = "europeana_page_url")
        val europeanaPageUrl: String? = null
                          )
