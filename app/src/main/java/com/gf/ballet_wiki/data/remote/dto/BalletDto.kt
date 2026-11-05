package com.gf.ballet_wiki.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class BalletDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("original_title")
    val originalTitle: String? = null,

    @SerializedName("composer")
    val composer: String,

    @SerializedName("original_choreographer")
    val originalChoreographer: String? = null,

    @SerializedName("premiere_year")
    val premiereYear: Int,

    @SerializedName("premiere_location")
    val premiereLocation: String? = null,

    @SerializedName("acts")
    val acts: Int = 0,

    @SerializedName("style")
    val style: String? = null,

    @SerializedName("epoch_period")
    val epochPeriod: String? = null,

    @SerializedName("difficulty_level")
    val difficultyLevel: String? = null,

    @SerializedName("schools")
    val schools: List<String>? = null,

    @SerializedName("traditions")
    val traditions: List<String>? = null,

    @SerializedName("genre")
    val genre: List<String>? = null,

    @SerializedName("synopsis")
    val synopsis: String? = null,

    @SerializedName("history_and_evolution")
    val historyAndEvolution: String? = null,

    @SerializedName("characters")
    val characters: List<String>? = null,

    @SerializedName("key_variations")
    val keyVariations: List<String>? = null,

    @SerializedName("historical_performances")
    val historicalPerformances: List<HistoricalPerformanceDto>? = null,

    @SerializedName("prominent_companies")
    val prominentCompanies: List<ProminentCompanyDto>? = null,

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

data class AttributionCoverDto(
        @SerializedName("creator")
        val creator: String? = null,

        @SerializedName("source")
        val source: String? = null,

        @SerializedName("source_url")
        val sourceUrl: String? = null,

        @SerializedName("license")
        val license: String? = null,

        @SerializedName("license_url")
        val licenseUrl: String? = null,

        @SerializedName("rights")
        val rights: String? = null,

        @SerializedName("credit")
        val credit: String? = null
)

data class MultimediaDto(
    @SerializedName("cover_image_url")
    val coverImageUrl: String? = null,

    @SerializedName("europeana_search_term")
    val europeanaSearchTerm: String? = null,

    @SerializedName("youtube_query")
    val youtubeQuery: String? = null,

    @SerializedName("youtube_url")
    val youtubeUrl: String? = null,

    @SerializedName("attribution_cover")
    private val rawAttributionCover: JsonElement? = null,
                        ) {
    val attributionCoverObj: AttributionCoverDto?
        get() {
            val element = rawAttributionCover ?: return null
            return try {
                if (element.isJsonObject) {
                    com.google.gson.Gson().fromJson(element, AttributionCoverDto::class.java)
                }
                else null
            }
            catch (e: Exception) {
                null
            }
        }

    val attributionCover: String?
        get() {
            val element = rawAttributionCover ?: return null
            return when {
                element.isJsonPrimitive -> element.asString
                element.isJsonObject -> {
                    val obj = attributionCoverObj
                    val textParts =
                            listOfNotNull(obj?.credit, obj?.rights, obj?.source, obj?.creator)
                                    .filter { it.isNotBlank() }
                    if (textParts.isNotEmpty()) textParts.first() else element.toString()
                }

                else -> null
            }
        }
}
