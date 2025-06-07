package com.example.myapp.data

import retrofit2.http.GET

interface CatApi {
    @GET("https://api.thecatapi.com/v1/images/search")
    suspend fun getRandomCat(): List<CatImage>
}

data class CatImage(
    val id: String,
    val url: String
) 