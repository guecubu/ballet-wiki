package com.gf.ballet_wiki.data.remote.api

import com.gf.ballet_wiki.data.remote.dto.OpenOpusComposerSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenOpusApiService {

    @GET("composer/list/search/{query}.json")
    suspend fun searchComposer(@Path("query") query: String): OpenOpusComposerSearchResponse
}
