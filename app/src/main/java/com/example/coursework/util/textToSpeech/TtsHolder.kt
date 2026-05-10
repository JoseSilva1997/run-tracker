package com.example.coursework.util.textToSpeech

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsHolder @Inject constructor() {
    private var engine: TextToSpeech? = null
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready

    fun init (context: Context) {
        if (engine != null) return
        engine = TextToSpeech(context.applicationContext) { status ->
            _ready.value = status == TextToSpeech.SUCCESS
        }
    }

    fun speak(text: String, flush: Boolean = true, utteranceId: String? = null) {
        val mode = if(flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        engine?.speak(text, mode, null, utteranceId)
    }

    fun shutdown() {
        engine?.stop()
        engine?.shutdown()
        engine = null
        _ready.value = false
    }

}