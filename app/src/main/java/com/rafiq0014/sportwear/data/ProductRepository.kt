package com.rafiq0014.sportwear.data

import com.rafiq0014.sportwear.BuildConfig
import com.rafiq0014.sportwear.data.local.ProductDao
import com.rafiq0014.sportwear.data.local.toProduct
import com.rafiq0014.sportwear.data.model.Product
import com.rafiq0014.sportwear.data.model.toEntity
import com.rafiq0014.sportwear.data.remote.ApiService
import com.rafiq0014.sportwear.data.remote.ImgBBService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProductRepository(
    private val apiService: ApiService,
    private val imgBBService: ImgBBService,
    private val productDao: ProductDao
) {
    suspend fun uploadImage(imageBytes: ByteArray, filename: String = "image.jpg"): String? {
        return withContext(Dispatchers.IO) {
            try {
                val mediaType = "image/*".toMediaTypeOrNull()
                val requestFile = imageBytes.toRequestBody(mediaType)
                val body = MultipartBody.Part.createFormData("image", filename, requestFile)
                val response = imgBBService.uploadImage(BuildConfig.IMGBB_API_KEY, body)
                if (response.success && response.data != null) {
                    response.data.url
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    val products: Flow<List<Product>> = productDao.getAllProducts().map { entities ->
        entities.map { it.toProduct() }
    }

    suspend fun refreshProducts(userId: String) {
        withContext(Dispatchers.IO) {
            val remoteProducts = apiService.getProducts(userId)
            val entities = remoteProducts.map { it.toEntity() }
            productDao.clearAll()
            productDao.insertProducts(entities)
        }
    }

    suspend fun createProduct(product: Product) {
        withContext(Dispatchers.IO) {
            // Update Room first
            productDao.insertProduct(product.toEntity())

            // Then synchronize with MockAPI
            try {
                apiService.createProduct(product)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun updateProduct(product: Product) {
        withContext(Dispatchers.IO) {
            // Update Room first
            productDao.updateProduct(product.toEntity())

            // Then synchronize with MockAPI
            try {
                apiService.updateProduct(product.id, product)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteProduct(product: Product) {
        withContext(Dispatchers.IO) {
            // Update Room first
            productDao.deleteProduct(product.toEntity())

            // Then synchronize with MockAPI
            try {
                apiService.deleteProduct(product.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
