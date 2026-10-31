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

    @SerializedName("guid")
    val guid: String? = null,

    @SerializedName("title")
    val title: List<String>? = null,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("year")
    val year: List<String>? = null,

    @SerializedName("dataProvider")
    val dataProvider: List<String>? = null,

    @SerializedName("provider")
    val provider: List<String>? = null,

    @SerializedName("edmPreview")
    val edmPreview: List<String>? = null,

    @SerializedName("isShownAt")
    val isShownAt: List<String>? = null,

    @SerializedName("isShownBy")
    val isShownBy: List<String>? = null,

    @SerializedName("rights")
    val rights: List<String>? = null,

    @SerializedName("dcCreator")
    val dcCreator: List<String>? = null,

    @SerializedName("dcContributor")
    val dcContributor: List<String>? = null,

    @SerializedName("dcDescription")
    val dcDescription: List<String>? = null,

    @SerializedName("dcFormat")
    val dcFormat: List<String>? = null,

    @SerializedName("dcIdentifier")
    val dcIdentifier: List<String>? = null,

    @SerializedName("dcLanguage")
    val dcLanguage: List<String>? = null,

    @SerializedName("dcPublisher")
    val dcPublisher: List<String>? = null,

    @SerializedName("dcSource")
    val dcSource: List<String>? = null,

    @SerializedName("dcSubject")
    val dcSubject: List<String>? = null,

    @SerializedName("dcType")
    val dcType: List<String>? = null,

    @SerializedName("edmConcept")
    val edmConcept: List<String>? = null,

    @SerializedName("edmPlace")
    val edmPlace: List<String>? = null,

    @SerializedName("edmTimespan")
    val edmTimespan: List<String>? = null,

    @SerializedName("country")
    val country: List<String>? = null,

    @SerializedName("language")
    val language: List<String>? = null,

    @SerializedName("completeness")
    val completeness: Int? = null,

    @SerializedName("score")
    val score: Double? = null
)
