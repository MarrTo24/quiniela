package com.quiniela.app

import android.app.Application
import com.quiniela.app.util.NotificationHelper

class QuinielaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
