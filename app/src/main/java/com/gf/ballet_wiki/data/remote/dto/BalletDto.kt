package com.gf.ballet_wiki.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BalletDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("composer")
    val composer: String,

    @SerializedName("original_choreographer")
    val originalChoreographer: String = "",

    @SerializedName("premiere_year")
    val premiereYear: Int,

    @SerializedName("premiere_location")
    val premiereLocation: String = "",

    @SerializedName("acts")
    val acts: Int = 0,

    @SerializedName("style")
    val style: String? = null,

    @SerializedName("epoch_period")
    val epochPeriod: String? = null,

    @SerializedName("synopsis")
    val synopsis: String = "",

    @SerializedName("history_and_evolution")
    val historyAndEvolution: String = "",

    @SerializedName("characters")
    val characters: List<String> = emptyList(),

    @SerializedName("key_variations")
    val keyVariations: List<String> = emptyList(),

    @SerializedName("historical_performances")
    val historicalPerformances: List<HistoricalPerformanceDto> = emptyList(),

    @SerializedName("prominent_companies")
    val prominentCompanies: List<ProminentCompanyDto> = emptyList(),

    @SerializedName("cover_image")
    val coverImage: String? = null,

    @SerializedName("multimedia")
    private val rawMultimedia: MultimediaDto? = null,
                    ) {
    val multimedia: MultimediaDto?
        get() = rawMultimedia ?: coverImage?.let { MultimediaDto(coverImageUrl = it) }
}

data class HistoricalPerformanceDto(
    @SerializedName("year")
    val year: Int,

    @SerializedName("company")
    val company: String,

    @SerializedName("theater")
    val theater: String,

    @SerializedName("choreographer")
    val choreographer: String,

    @SerializedName("relevance")
    val relevance: String,
)

data class ProminentCompanyDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("country")
    val country: String,

    @SerializedName("official_url")
    val officialUrl: String,
)

data class MultimediaDto(
    @SerializedName("cover_image_url")
    val coverImageUrl: String? = null,

    @SerializedName("europeana_search_term")
    val europeanaSearchTerm: String? = null,

    @SerializedName("youtube_query")
    val youtubeQuery: String? = null,
)
