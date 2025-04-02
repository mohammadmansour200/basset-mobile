package com.basset

import android.app.Application
import com.basset.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BassetApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BassetApp)
            androidLogger()

            modules(appModule)
        }
    }
}