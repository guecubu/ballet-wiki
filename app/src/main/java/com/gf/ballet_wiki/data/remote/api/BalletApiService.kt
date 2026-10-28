package com.gf.ballet_wiki.data.remote.api

import com.gf.ballet_wiki.data.remote.dto.BalletDto
import retrofit2.http.GET
import retrofit2.http.Path

interface BalletApiService {

    @GET("api/ballets")
    suspend fun getBallets(): List<BalletDto>

    @GET("api/ballets/{id}")
    suspend fun getBalletById(@Path("id") id: Int): BalletDto
}
