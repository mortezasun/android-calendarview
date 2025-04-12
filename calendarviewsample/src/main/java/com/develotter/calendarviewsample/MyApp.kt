package com.develotter.calendarviewsample

import android.app.Application
import android.content.Context

class MyApp: Application() {
    companion object {

        const val SHARED_SETTING = "settings"
        const val APP_LANGUAGE = "app_language"
        const val DEFAULT_LANGUAGE = "0"



        const val CALENDAR_LANGUAGE = "Calendar_language"
        const val DEFAULT_LANGUAGE_CALENDAR= "0"
    }
    var myAppLanguage: String=""
    override fun onCreate() {
        super.onCreate()

        myAppLanguage=updateLocaleBase()
    }
    override fun attachBaseContext(newBase: Context) {
        if (myAppLanguage!="0") {
            val context = newBase.updateLocale(myAppLanguage)
            super.attachBaseContext(context)
        }else{
            super.attachBaseContext(newBase)
        }
    }

}
