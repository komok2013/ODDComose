package ru.edinros.agitatoroffline

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.Forest.plant
@HiltAndroidApp
class MainApp:Application() {

    override fun onCreate() {

        if (BuildConfig.DEBUG) {
            plant(Timber.DebugTree())
        }

        super.onCreate()

    }
}