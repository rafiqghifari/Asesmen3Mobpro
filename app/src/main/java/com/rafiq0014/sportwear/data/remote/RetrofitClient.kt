package com.rafiq0014.sportwear.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://6a3e1bef0443193a1a0b7119.mockapi.io/api/v1/" // Replace with actual MockAPI URL

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }

    val imgBBService: ImgBBService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.imgbb.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ImgBBService::class.java)
    }
}
