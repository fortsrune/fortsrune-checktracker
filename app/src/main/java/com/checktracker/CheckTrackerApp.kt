package com.checktracker

import android.app.Application
import com.checktracker.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CheckTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CheckTrackerApp)
            modules(appModules)
        }
    }
}
