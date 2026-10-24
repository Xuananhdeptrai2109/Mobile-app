package com.moneycare.app

import android.app.Application
import com.moneycare.app.di.AppContainer
import com.moneycare.app.util.SessionManager

class MoneyCareApp : Application() {

    lateinit var sessionManager: SessionManager
        private set

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        sessionManager = SessionManager(applicationContext)
        container = AppContainer(applicationContext, sessionManager)
    }

    companion object {
        lateinit var instance: MoneyCareApp
            private set
    }
}
