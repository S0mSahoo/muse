package com.example

import android.app.Application
import com.example.di.AppContainer

class MUSEApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
