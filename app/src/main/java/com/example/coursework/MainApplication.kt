package com.example.coursework

import android.app.Application
import com.example.coursework.util.textToSpeech.TtsHolder
import com.google.android.gms.maps.MapsInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {

    @Inject lateinit var ttsHolder: TtsHolder

    override fun onCreate() {
        super.onCreate()
        // Preload the Maps SDK at app launch.
        // the call is safe to make multiple times - subsequent calls are no-ops.
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST) { /* no-op */ }
        // Start a text-to-speech engine.
        ttsHolder.init(this)
    }
}
