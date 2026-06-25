package com.rafiq0014.sportwear.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImgBBUploadResponse(
    val data: ImgBBData?,
    val success: Boolean,
    val status: Int
)

@JsonClass(generateAdapter = true)
data class ImgBBData(
    val url: String
)
