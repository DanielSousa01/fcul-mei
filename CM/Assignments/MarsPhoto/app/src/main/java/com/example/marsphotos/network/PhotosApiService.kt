package com.example.marsphotos.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET

private const val MARS_URL =
    "https://android-kotlin-fun-mars-server.appspot.com"

private const val RANDOM_IMG_URL = "https://picsum.photos"

private val marsRetrofit =
    Retrofit
        .Builder()
        .addConverterFactory(
            Json.asConverterFactory("application/json".toMediaType()),
        ).baseUrl(MARS_URL)
        .build()

private val randomRetrofit =
    Retrofit
        .Builder()
        .addConverterFactory(
            Json.asConverterFactory("application/json".toMediaType()),
        ).baseUrl(RANDOM_IMG_URL)
        .build()

interface PhotosApiService {
    @GET("photos")
    suspend fun getMarsPhotos(): List<MarsPhoto>

    @GET("v2/list")
    suspend fun getRandomPhotos(): List<RandomPhoto>
}

object MarsApi {
    val retrofitService: PhotosApiService by lazy {
        marsRetrofit.create(PhotosApiService::class.java)
    }
}

object RandomApi {
    val retrofitService: PhotosApiService by lazy {
        randomRetrofit.create(PhotosApiService::class.java)
    }
}
