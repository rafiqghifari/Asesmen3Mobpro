package com.rafiq0014.sportwear

import android.app.Application
import com.rafiq0014.sportwear.di.AppContainer
import com.rafiq0014.sportwear.di.DefaultAppContainer

class SportWearApp : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
