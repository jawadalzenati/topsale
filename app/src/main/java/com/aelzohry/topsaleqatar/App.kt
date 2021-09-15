package com.aelzohry.topsaleqatar

import android.app.Application
import android.content.Context
import com.aelzohry.topsaleqatar.helper.Constants
import com.onesignal.OneSignal

class App : Application() {

    override fun onCreate() {
        instance = this
        super.onCreate()

        if (BuildConfig.DEBUG)
            OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        OneSignal.initWithContext(this)
        OneSignal.setAppId(Constants.ONESIGNAL_APP_ID)
    }

    companion object {
        private var instance: App? = null

        // or return instance.getApplicationContext();
        val context: Context
            get() = instance!!
    }
}