package com.gf.ballet_wiki.data.remote.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BALLET_API_BASE_URL = "https://ballet-api.vercel.app/"
    private const val EUROPEANA_API_BASE_URL = "https://api.europeana.eu/"
    private const val OPEN_OPUS_API_BASE_URL = "https://api.openopus.org/"

    val balletApiService: BalletApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BALLET_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BalletApiService::class.java)
    }

    val europeanaApiService: EuropeanaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(EUROPEANA_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EuropeanaApiService::class.java)
    }

    val openOpusApiService: OpenOpusApiService by lazy {
        Retrofit.Builder()
            .baseUrl(OPEN_OPUS_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenOpusApiService::class.java)
    }
}
