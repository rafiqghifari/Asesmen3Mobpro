package com.rafiq0014.sportwear.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Product(
    val id: String,
    val userId: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val brand: String = "",
    val category: String = "",
    val stock: Int = 0,
    val size: String = "",
    val isSynced: Boolean = true
)

fun Product.toEntity() = com.rafiq0014.sportwear.data.local.ProductEntity(
    id = id,
    userId = userId,
    name = name,
    description = description,
    price = price,
    imageUrl = imageUrl,
    brand = brand,
    category = category,
    stock = stock,
    size = size,
    isSynced = isSynced
)
