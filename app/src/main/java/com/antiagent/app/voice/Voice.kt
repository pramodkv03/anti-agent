package com.antiagent.app.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceInput(private val ctx: Context) {

    private var recognizer: SpeechRecognizer? = null
    private var onTextCb: ((String) -> Unit)? = null
    private var onErrorCb: ((String) -> Unit)? = null

    fun start(onText: (String) -> Unit, onError: (String) -> Unit) {
        onTextCb = onText
        onErrorCb = onError
        stopInternal()

        if (!SpeechRecognizer.isRecognitionAvailable(ctx)) {
            onError("Voice recognition is not available on this phone")
            return
        }

        val r = SpeechRecognizer.createSpeechRecognizer(ctx)
        recognizer = r
        r.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onError(error: Int) {
                onErrorCb?.invoke(errorText(error))
                stopInternal()
            }

            override fun onResults(results: Bundle?) {
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = list?.firstOrNull() ?: ""
                if (text.isNotBlank()) onTextCb?.invoke(text)
                stopInternal()
            }

            override fun onPartialResults(partialResults: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        r.startListening(intent)
    }

    fun stop() {
        stopInternal()
    }

    fun destroy() {
        stopInternal()
    }

    private fun stopInternal() {
        try {
            recognizer?.stopListening()
            recognizer?.destroy()
        } catch (_: Exception) {
        }
        recognizer = null
    }

    private fun errorText(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_AUDIO -> "Microphone error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is needed"
        SpeechRecognizer.ERROR_NO_MATCH -> "Did not catch that, please say it again"
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Voice service or internet not available"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Microphone is busy"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard"
        else -> "Voice error ($code)"
    }
}

class Speaker(ctx: Context) {

    private var ready = false
    private var pending: String? = null
    private lateinit var tts: TextToSpeech

    init {
        tts = TextToSpeech(ctx.applicationContext) { status -> onInit(status) }
    }

    private fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            ready = true
            try {
                tts.setLanguage(Locale.getDefault())
            } catch (_: Exception) {
            }
            pending?.let { p ->
                pending = null
                speakNow(p)
            }
        }
    }

    fun speak(text: String) {
        val clean = text
            .replace(Regex("[*#`_>\\[\\]]"), " ")
            .replace(Regex("\n{2,}"), "\n")
            .take(4000)
        if (clean.isBlank()) return
        if (!ready) {
            pending = clean
            return
        }
        speakNow(clean)
    }

    private fun speakNow(text: String) {
        try {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "antiagent")
        } catch (_: Exception) {
        }
    }

    fun stop() {
        try {
            tts.stop()
        } catch (_: Exception) {
        }
    }

    fun shutdown() {
        try {
            tts.stop()
            tts.shutdown()
        } catch (_: Exception) {
        }
    }
}
