package org.infa252.project

import android.app.Application

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}