package com.example.coursework

import android.app.Application
import com.google.android.gms.maps.MapsInitializer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Preload the Maps SDK at app launch.
        // the call is safe to make multiple times - subsequent calls are no-ops.
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST) { /* no-op */ }
    }
}
