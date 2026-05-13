package com.example.coursework.util.textToSpeech

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared wrapper around Android's TextToSpeech. @Singleton because the engine is expensive to
 * create and there's no point standing up more than one — every caller talks to the same instance.
 */
@Singleton
class TtsHolder @Inject constructor() {
    private var engine: TextToSpeech? = null
    // Flips to true only after the engine reports SUCCESS from its status callback. Callers should
    // check this before calling speak — speaking before the engine is ready silently fails.
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready

    // Lazily creates the TTS engine on first call — the constructor has no Context, and the engine is
    // expensive enough that we'd rather not pay for it if the user never starts a run. Idempotent on
    // purpose: the early return stops a second call from leaking another instance on top of the first.
    fun init (context: Context) {
        if (engine != null) return
        engine = TextToSpeech(context.applicationContext) { status ->
            _ready.value = status == TextToSpeech.SUCCESS
        }
    }

    // Says the given text. Defaults to QUEUE_FLUSH so a new line replaces whatever was speaking
    // before — for a runner who's watching the road, the freshest message is the one that matters.
    fun speak(text: String, flush: Boolean = true, utteranceId: String? = null) {
        val mode = if(flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        engine?.speak(text, mode, null, utteranceId)
    }

}