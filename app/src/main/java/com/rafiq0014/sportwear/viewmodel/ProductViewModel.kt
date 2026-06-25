package com.rafiq0014.sportwear.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rafiq0014.sportwear.SportWearApp
import com.rafiq0014.sportwear.data.ProductRepository
import com.rafiq0014.sportwear.data.model.Product
import com.rafiq0014.sportwear.datastore.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.io.IOException

class ProductViewModel(
    private val repository: ProductRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val filteredProducts: StateFlow<List<Product>> = combine(
        repository.products,
        _searchQuery,
        sessionManager.userFlow
    ) { products, query, user ->
        val userEmail = user?.email
        val userProducts = if (userEmail != null) {
            products.filter { it.userId == userEmail }
        } else {
            emptyList()
        }

        if (query.isBlank()) {
            userProducts
        } else {
            userProducts.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private var currentUser: com.rafiq0014.sportwear.data.model.User? = null

    init {
        viewModelScope.launch {
            sessionManager.userFlow.collect { user ->
                currentUser = user
                if (user != null) {
                    refreshProducts(user.email)
                }
            }
        }
    }

    fun getProductById(id: String): Product? {
        return filteredProducts.value.find { it.id == id }
    }

    fun saveProduct(
        id: String?,
        name: String,
        description: String,
        price: Double,
        brand: String,
        category: String,
        stock: Int,
        size: String,
        imageBytes: ByteArray?,
        currentImageUrl: String?,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val user = currentUser
                if (user == null) {
                    _errorMessage.value = "User not authenticated"
                    return@launch
                }
                
                val finalImageUrl = if (imageBytes != null) {
                    repository.uploadImage(imageBytes) ?: throw Exception("Image upload failed. Check your API Key.")
                } else {
                    currentImageUrl ?: throw Exception("Image URL is missing")
                }

                val product = Product(
                    id = id ?: java.util.UUID.randomUUID().toString(),
                    userId = user.email,
                    name = name,
                    description = description,
                    price = price,
                    imageUrl = finalImageUrl,
                    brand = brand,
                    category = category,
                    stock = stock,
                    size = size
                )

                if (id == null) {
                    repository.createProduct(product)
                } else {
                    repository.updateProduct(product)
                }
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to save product"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProduct(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val product = getProductById(id) ?: throw Exception("Product not found")
                repository.deleteProduct(product)
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete product"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun refreshProducts(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.refreshProducts(userId)
                _isOffline.value = false
            } catch (e: retrofit2.HttpException) {
                if (e.code() != 404) {
                    _errorMessage.value = e.message ?: "An unexpected error occurred"
                }
            } catch (e: UnknownHostException) {
                _isOffline.value = true
            } catch (e: IOException) {
                _isOffline.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SportWearApp)
                ProductViewModel(
                    repository = application.container.productRepository,
                    sessionManager = application.container.sessionManager
                )
            }
        }
    }
}
