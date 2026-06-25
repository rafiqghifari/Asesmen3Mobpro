package com.rafiq0014.sportwear.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val brand: String,
    val category: String,
    val stock: Int,
    val size: String
)

fun ProductEntity.toProduct() = com.rafiq0014.sportwear.data.model.Product(
    id = id,
    userId = userId,
    name = name,
    description = description,
    price = price,
    imageUrl = imageUrl,
    brand = brand,
    category = category,
    stock = stock,
    size = size
)
