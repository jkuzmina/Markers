package ru.netology.markers.application

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import ru.netology.markers.BuildConfig

class MarkersApplication : Application() {
    override fun onCreate() {
        MapKitFactory.setApiKey(BuildConfig.API_KEY)
        super.onCreate()
    }

}