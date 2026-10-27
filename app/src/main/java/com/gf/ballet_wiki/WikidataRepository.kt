package com.gf.ballet_wiki

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

data class BalletWork(
        val title: String,
        val description: String,
        val composer: String,
        val titles: List<String>,
        val sourceUrl: String
                     )

private interface WikidataService {
    @Headers(
            "Accept: application/sparql-results+json",
            "User-Agent: BalletReference/0.1 (Android academic project)"
            )
    @GET("sparql")
    fun getWork(
            @Query("query") query: String, @Query("format") format: String = "json"
               ): Call<SparqlResponse>
}

private data class SparqlResponse(val results: SparqlResults)
private data class SparqlResults(val bindings: List<SparqlBinding>)
private data class SparqlBinding(
        val workLabel: SparqlValue?,
        val workDescription: SparqlValue?,
        val composerLabel: SparqlValue?
                                )

private data class SparqlValue(@SerializedName("value") val text: String)

internal data class WorkFields(val title: String, val description: String, val composer: String)

internal fun swanLakeFallback() = BalletWork(
        title = "Swan Lake",
        description = "Ballet by Pyotr Ilyich Tchaikovsky",
        composer = "Pyotr Ilyich Tchaikovsky",
        titles = listOf("El lago de los cisnes", "Le Lac des cygnes", "Lebedinoye ozero"),
        sourceUrl = "https://www.wikidata.org/wiki/Q199786"
                                            )

internal fun parseWorkResponse(json: String): WorkFields? {
    val response = Gson().fromJson(json, SparqlResponse::class.java)
    val binding = response.results.bindings.firstOrNull() ?: return null
    val title = binding.workLabel?.text?.takeIf(String::isNotBlank) ?: return null
    val description = binding.workDescription?.text?.takeIf(String::isNotBlank) ?: return null
    val composer = binding.composerLabel?.text?.takeIf(String::isNotBlank) ?: return null
    return WorkFields(title, description, composer)
}

class WikidataRepository {
    private val service = Retrofit.Builder()
            .baseUrl("https://query.wikidata.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WikidataService::class.java)

    fun loadSwanLake(onResult: (BalletWork, Boolean) -> Unit) {
        val fallback = swanLakeFallback()
        val query = """
            SELECT ?workLabel ?workDescription ?composerLabel WHERE {
              VALUES ?work { wd:Q199786 }
              OPTIONAL { ?work wdt:P86 ?composer }
              SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
            }
        """.trimIndent()

        service.getWork(query).enqueue(object : Callback<SparqlResponse> {
            override fun onResponse(
                    call: Call<SparqlResponse>, response: Response<SparqlResponse>
                                   ) {
                val body = response.body()
                val fields = if (response.isSuccessful && body != null) {
                    parseWorkResponse(Gson().toJson(body))
                }
                else {
                    null
                }
                if (fields != null) {
                    onResult(
                            fallback.copy(
                                    title = fields.title, description = fields.description,
                                    composer = fields.composer
                                         ), true
                            )
                }
                else {
                    onResult(fallback, false)
                }
            }

            override fun onFailure(call: Call<SparqlResponse>, error: Throwable) {
                onResult(fallback, false)
            }
        })
    }
}