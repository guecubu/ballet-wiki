package com.gf.ballet_wiki.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EuropeanaSearchResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("totalResults")
    val totalResults: Int = 0,

    @SerializedName("items")
    val items: List<EuropeanaItemDto> = emptyList()
)

data class EuropeanaItemDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: List<String>? = null,

    @SerializedName("edmPreview")
    val edmPreview: List<String>? = null,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("year")
    val year: List<String>? = null,

    @SerializedName("dataProvider")
    val dataProvider: List<String>? = null
)
