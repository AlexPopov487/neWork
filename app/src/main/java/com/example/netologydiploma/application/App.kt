package com.example.netologydiploma.application

import android.app.Application
import com.example.netologydiploma.auth.AppAuth

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        AppAuth.initializeAppAuth(this)
    }
}