package com.example

import android.app.Application
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.QonversionConfig
import com.qonversion.android.sdk.dto.QEnvironment
import com.qonversion.android.sdk.dto.QLaunchMode

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val qonversionKey = BuildConfig.QONVERSION_PROJECT_KEY
        val environment = if (BuildConfig.DEBUG) QEnvironment.Sandbox else QEnvironment.Production

        val config = QonversionConfig.Builder(this, qonversionKey, QLaunchMode.SubscriptionManagement)
            .setEnvironment(environment)
            .build()
        Qonversion.initialize(config)
    }
}
