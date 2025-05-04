package com.develotter.calendarviewsample

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import com.develotter.calendarviewsample.MyApp.Companion.APP_LANGUAGE
import com.develotter.calendarviewsample.MyApp.Companion.CALENDAR_LANGUAGE
import com.develotter.calendarviewsample.MyApp.Companion.DEFAULT_LANGUAGE
import com.develotter.calendarviewsample.MyApp.Companion.DEFAULT_LANGUAGE_CALENDAR
import com.develotter.calendarviewsample.MyApp.Companion.SHARED_SETTING
import com.develotter.calendarviewsample.getCalendarLanguageEnumsWithBaseLanguage
import com.develotter.calendarviewsample.getCalendarLanguageLocale
import com.develotter.calendarviewsample.getLanguageEnumsWithBaseLanguage
import com.develotter.calendarviewsample.getLanguageLocale
import ir.huri.jcal.JalaliCalendar
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.util.Locale
import kotlin.system.exitProcess

fun Context.getBaseLanguage(): String {
    val sharedPreferences = this.getSharedPreferences(SHARED_SETTING, MODE_PRIVATE)
    return sharedPreferences.getString(APP_LANGUAGE, DEFAULT_LANGUAGE).toString()

}
fun Context.getCalendarBaseLanguage(): String {
    val sharedPreferences = this.getSharedPreferences(SHARED_SETTING, MODE_PRIVATE)
    return sharedPreferences.getString(CALENDAR_LANGUAGE, DEFAULT_LANGUAGE_CALENDAR).toString()

}
fun Context.getLanguageEnumsWithBaseLanguage(): LanguageEnums {
    val f = getBaseLanguage()
    return if (f=="0")  LanguageEnums.DefaultDevice else LanguageEnums.entries.map {
        it.title
    }.indexOf(f).let { LanguageEnums.entries[it] }

}
fun Context.getCalendarLanguageEnumsWithBaseLanguage(): LanguageEnums {
    val f = getCalendarBaseLanguage()
    return if (f=="0")  LanguageEnums.DefaultDevice else LanguageEnums.entries.map {
        it.title
    }.indexOf(f).let { LanguageEnums.entries[it] }

}
fun Context.getLanguage(): String {

    val lg= getBaseLanguage()
    return if (lg=="0")  this.resources.getDeviceDefaultLocale().toLanguageTag() else lg
}
fun Context.getLanguageLocale(): Locale {


    return Locale(getLanguage())
}
fun Context.getCalendarLanguage(): String {

    val lg= getCalendarBaseLanguage()
    return if (lg=="0")  this.resources.getDeviceDefaultLocale().toLanguageTag() else lg
}
fun Context.getCalendarLanguageLocale(): Locale {


    return Locale(getCalendarLanguage())
}
fun Context.setLanguage(locale:String) {

    val sharedPreferences = this.getSharedPreferences(SHARED_SETTING, MODE_PRIVATE)
    sharedPreferences.edit { putString(APP_LANGUAGE, locale) }
}
fun Context.setCalendarLanguage(locale:String) {

    val sharedPreferences = this.getSharedPreferences(SHARED_SETTING, MODE_PRIVATE)
    sharedPreferences.edit { putString(CALENDAR_LANGUAGE, locale) }
}
 fun Context.updateLocale( languageCode: String): Context {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = this.resources.configuration
    config.setLocale(locale)
    return this.createConfigurationContext(config)
}
fun Context.updateLocaleBase(): String{
    val myAppLanguage=this.getBaseLanguage()
    if (myAppLanguage!="0"){
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(myAppLanguage)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    return myAppLanguage
}
fun Context.restartApp() {
    val packageManager = packageManager
    val intent = packageManager.getLaunchIntentForPackage(packageName)
    val componentName = intent?.component
    val mainIntent = Intent.makeRestartActivityTask(componentName)
    startActivity(mainIntent)
    exitProcess(0)
}
fun Resources.getDeviceDefaultLocale(): Locale {
    val configuration: Configuration = configuration
    return configuration.locales[0] // Erste Locale in der Liste
}
fun Context.getSuperLocale():Locale{

    return  if (getCalendarLanguageEnumsWithBaseLanguage() == LanguageEnums.DefaultDevice) {
            when (getLanguageEnumsWithBaseLanguage()) {
                LanguageEnums.DefaultDevice -> {
                    resources.getDeviceDefaultLocale()
                }

                else -> {
                    getLanguageLocale()
                }

            }
        } else {
            when (getCalendarLanguageEnumsWithBaseLanguage()) {
                LanguageEnums.DefaultDevice -> {
                    resources.getDeviceDefaultLocale()
                }

                else -> {
                    getCalendarLanguageLocale()
                }

            }
        }
}
