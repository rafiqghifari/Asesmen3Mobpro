package com.rafiq0014.sportwear.di

import android.content.Context

import com.rafiq0014.sportwear.auth.GoogleAuthManager
import com.rafiq0014.sportwear.data.ProductRepository
import com.rafiq0014.sportwear.data.local.AppDatabase
import com.rafiq0014.sportwear.data.remote.RetrofitClient
import com.rafiq0014.sportwear.datastore.SessionManager

interface AppContainer {
    val sessionManager: SessionManager
    val googleAuthManager: GoogleAuthManager
    val productRepository: ProductRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val sessionManager: SessionManager by lazy {
        SessionManager(context)
    }
    
    override val googleAuthManager: GoogleAuthManager by lazy {
        GoogleAuthManager()
    }

    override val productRepository: ProductRepository by lazy {
        ProductRepository(
            apiService = RetrofitClient.apiService,
            imgBBService = RetrofitClient.imgBBService,
            productDao = AppDatabase.getDatabase(context).productDao()
        )
    }
}
