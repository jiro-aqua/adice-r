package jp.gr.aqua.adice

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager

class AdiceApplication : Application()
{
    override fun onCreate() {
        super.onCreate()

        appContext = this
        PreferenceManager.setDefaultValues(this, R.xml.preferences_general, false)
    }

    companion object{
        lateinit var appContext : Context
    }
}
