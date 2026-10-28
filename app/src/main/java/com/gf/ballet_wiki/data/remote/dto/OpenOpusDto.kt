package com.gf.ballet_wiki.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OpenOpusComposerSearchResponse(
    @SerializedName("status")
    val status: OpenOpusStatusDto? = null,

    @SerializedName("composers")
    val composers: List<OpenOpusComposerDto> = emptyList()
)

data class OpenOpusStatusDto(
    @SerializedName("success")
    val success: String? = null,

    @SerializedName("rows")
    val rows: Int? = null
)

data class OpenOpusComposerDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("complete_name")
    val completeName: String,

    @SerializedName("birth")
    val birth: String? = null,

    @SerializedName("death")
    val death: String? = null,

    @SerializedName("epoch")
    val epoch: String? = null,

    @SerializedName("portrait")
    val portrait: String? = null
)
