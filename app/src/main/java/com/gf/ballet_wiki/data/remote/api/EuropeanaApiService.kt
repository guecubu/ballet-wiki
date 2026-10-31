package com.gf.ballet_wiki.data.remote.api

import com.gf.ballet_wiki.data.remote.dto.EuropeanaSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface EuropeanaApiService {

    @GET("record/v2/search.json")
    suspend fun searchEuropeana(
        @Query("wskey") apiKey: String,
        @Query("query") query: String,
        @Query("profile") profile: String = "rich",
        @Query("rows") rows: Int = 100
    ): EuropeanaSearchResponse
}
