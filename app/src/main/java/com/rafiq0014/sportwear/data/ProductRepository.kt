package com.rafiq0014.sportwear.data

import android.content.Context
import com.rafiq0014.sportwear.BuildConfig
import com.rafiq0014.sportwear.data.local.ProductDao
import com.rafiq0014.sportwear.data.local.toProduct
import com.rafiq0014.sportwear.data.model.Product
import com.rafiq0014.sportwear.data.model.toEntity
import com.rafiq0014.sportwear.data.remote.ApiService
import com.rafiq0014.sportwear.data.remote.ImgBBService
import com.rafiq0014.sportwear.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class ProductRepository(
    private val apiService: ApiService,
    private val imgBBService: ImgBBService,
    private val productDao: ProductDao,
    private val context: Context
) {
    private fun isOnline(): Boolean = NetworkUtils.isNetworkAvailable(context)

    fun saveImageLocally(imageBytes: ByteArray): String? {
        return try {
            val filename = "local_${java.util.UUID.randomUUID()}.jpg"
            val directory = File(context.filesDir, "local_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, filename)
            file.writeBytes(imageBytes)
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

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

    suspend fun syncUnsyncedProducts() {
        withContext(Dispatchers.IO) {
            if (!isOnline()) return@withContext
            val unsynced = productDao.getUnsyncedProducts()
            for (entity in unsynced) {
                try {
                    var imageUrl = entity.imageUrl
                    // If image is stored locally (absolute path to app directory)
                    if (!imageUrl.startsWith("http")) {
                        val file = File(imageUrl)
                        if (file.exists()) {
                            val imageBytes = file.readBytes()
                            val remoteUrl = uploadImage(imageBytes, file.name)
                            if (remoteUrl != null) {
                                imageUrl = remoteUrl
                                file.delete() // Clean up local file
                            } else {
                                continue // Image upload failed, skip this product sync for now
                            }
                        }
                    }
                    
                    val product = entity.toProduct().copy(imageUrl = imageUrl, isSynced = true)
                    val responseProduct = apiService.createProduct(product)
                    
                    // Mark as synced locally by replacing the ID if it changed
                    if (responseProduct.id != entity.id) {
                        productDao.deleteProduct(entity)
                    }
                    productDao.insertProduct(responseProduct.toEntity().copy(isSynced = true))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    suspend fun refreshProducts(userId: String) {
        withContext(Dispatchers.IO) {
            if (isOnline()) {
                // First sync any unsynced local products to MockAPI
                syncUnsyncedProducts()

                // Fetch remote products
                val remoteProducts = apiService.getProducts(userId)
                val entities = remoteProducts.map { it.toEntity().copy(isSynced = true) }
                
                // Only delete already synced products, keep unsynced local creations
                productDao.deleteSyncedProducts()
                productDao.insertProducts(entities)
            } else {
                throw IOException("No internet connection")
            }
        }
    }

    suspend fun createProduct(product: Product) {
        withContext(Dispatchers.IO) {
            if (isOnline()) {
                // Attempt to sync online immediately
                try {
                    val syncedProduct = product.copy(isSynced = true)
                    val responseProduct = apiService.createProduct(syncedProduct)
                    if (responseProduct.id != product.id) {
                        productDao.deleteProduct(product.toEntity())
                    }
                    productDao.insertProduct(responseProduct.toEntity().copy(isSynced = true))
                } catch (e: Exception) {
                    // If remote sync fails, save as unsynced locally
                    e.printStackTrace()
                    productDao.insertProduct(product.copy(isSynced = false).toEntity())
                }
            } else {
                // Save locally as unsynced
                productDao.insertProduct(product.copy(isSynced = false).toEntity())
            }
        }
    }

    suspend fun updateProduct(product: Product) {
        withContext(Dispatchers.IO) {
            if (!isOnline()) {
                throw IOException("Cannot update product while offline")
            }
            // Update Room first
            productDao.updateProduct(product.copy(isSynced = true).toEntity())

            // Then synchronize with MockAPI
            apiService.updateProduct(product.id, product)
        }
    }

    suspend fun deleteProduct(product: Product) {
        withContext(Dispatchers.IO) {
            if (!isOnline()) {
                throw IOException("Cannot delete product while offline")
            }
            // Update Room first
            productDao.deleteProduct(product.toEntity())

            // Then synchronize with MockAPI
            apiService.deleteProduct(product.id)
        }
    }
}
