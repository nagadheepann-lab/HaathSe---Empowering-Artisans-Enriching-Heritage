package com.example.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.models.SupportedLanguage
import java.util.Locale

class AudioVoiceHelper(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isReady = false
    var isSpeakingCallback: ((Boolean) -> Unit)? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    isSpeakingCallback?.invoke(true)
                }

                override fun onDone(utteranceId: String?) {
                    isSpeakingCallback?.invoke(false)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    isSpeakingCallback?.invoke(false)
                }
            })
            Log.d("AudioVoiceHelper", "TTS Initialized successfully")
        } else {
            Log.e("AudioVoiceHelper", "TTS Initialization failed: $status")
        }
    }

    fun speak(text: String, language: SupportedLanguage) {
        if (!isReady || tts == null || text.isBlank()) return
        val localizedText = MultilingualManager.tr(text, language)
        val locale = when (language) {
            SupportedLanguage.HINDI -> Locale("hi", "IN")
            SupportedLanguage.TAMIL -> Locale("ta", "IN")
            SupportedLanguage.TELUGU -> Locale("te", "IN")
            SupportedLanguage.KANNADA -> Locale("kn", "IN")
            SupportedLanguage.MALAYALAM -> Locale("ml", "IN")
            SupportedLanguage.BENGALI -> Locale("bn", "IN")
            SupportedLanguage.MARATHI -> Locale("mr", "IN")
            SupportedLanguage.GUJARATI -> Locale("gu", "IN")
            SupportedLanguage.PUNJABI -> Locale("pa", "IN")
            SupportedLanguage.ODIA -> Locale("or", "IN")
            SupportedLanguage.ENGLISH -> Locale("en", "IN")
        }

        try {
            var result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Try fallback to generic language code without country
                result = tts?.setLanguage(Locale(language.code))
            }
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to Indian English or default if specific regional Indian language voice isn't pre-installed on device
                tts?.setLanguage(Locale("en", "IN"))
            }
            tts?.setSpeechRate(0.92f)
            tts?.stop()
            tts?.speak(localizedText, TextToSpeech.QUEUE_FLUSH, null, "HaathSeTTS_${System.currentTimeMillis()}")
            isSpeakingCallback?.invoke(true)
        } catch (e: Exception) {
            Log.e("AudioVoiceHelper", "Error speaking text", e)
            isSpeakingCallback?.invoke(false)
        }
    }

    fun stop() {
        try {
            tts?.stop()
            isSpeakingCallback?.invoke(false)
        } catch (_: Exception) {}
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {}
    }
}
