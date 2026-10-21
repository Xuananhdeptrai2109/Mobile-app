package com.moneycare.app

import android.app.Application
import com.moneycare.app.util.SessionManager

class MoneyCareApp : Application() {

    lateinit var sessionManager: SessionManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        sessionManager = SessionManager(applicationContext)
    }

    companion object {
        lateinit var instance: MoneyCareApp
            private set
    }
}
