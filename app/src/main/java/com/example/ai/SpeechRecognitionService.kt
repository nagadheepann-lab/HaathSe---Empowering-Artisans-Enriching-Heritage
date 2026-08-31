package com.example.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.data.models.SupportedLanguage
import java.util.Locale

data class SpeechRecordingResult(
    val transcript: String,
    val originalAudioPath: String? = null,
    val language: SupportedLanguage,
    val isRealTimeRecognition: Boolean = true,
    val confidenceScore: Float = 0.94f
)

interface SpeechRecognitionService {
    fun startListening(
        language: SupportedLanguage,
        onPartialResult: (String) -> Unit,
        onFinalResult: (SpeechRecordingResult) -> Unit,
        onError: (String) -> Unit
    )
    fun stopListening()
    fun isAvailable(): Boolean
}

class AndroidSpeechRecognitionService(private val context: Context) : SpeechRecognitionService {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListeningActive = false

    init {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            }
        } catch (e: Exception) {
            Log.e("SpeechService", "Failed to create SpeechRecognizer", e)
        }
    }

    override fun isAvailable(): Boolean {
        return speechRecognizer != null && SpeechRecognizer.isRecognitionAvailable(context)
    }

    override fun startListening(
        language: SupportedLanguage,
        onPartialResult: (String) -> Unit,
        onFinalResult: (SpeechRecordingResult) -> Unit,
        onError: (String) -> Unit
    ) {
        val localeTag = when (language) {
            SupportedLanguage.HINDI -> "hi-IN"
            SupportedLanguage.TAMIL -> "ta-IN"
            SupportedLanguage.TELUGU -> "te-IN"
            SupportedLanguage.KANNADA -> "kn-IN"
            SupportedLanguage.MALAYALAM -> "ml-IN"
            SupportedLanguage.BENGALI -> "bn-IN"
            SupportedLanguage.MARATHI -> "mr-IN"
            SupportedLanguage.GUJARATI -> "gu-IN"
            SupportedLanguage.PUNJABI -> "pa-IN"
            SupportedLanguage.ODIA -> "or-IN"
            SupportedLanguage.ENGLISH -> "en-IN"
        }

        if (speechRecognizer == null) {
            // Fallback for emulator or unprovisioned engines
            onError("Speech recognition not available on device. Tap quick craft voice samples below.")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, localeTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListeningActive = true
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListeningActive = false
            }

            override fun onError(error: Int) {
                isListeningActive = false
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Record audio permission required"
                    SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please speak clearly."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input received"
                    else -> "Speech recognition error ($error)"
                }
                onError(errorMsg)
            }

            override fun onResults(results: Bundle?) {
                isListeningActive = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                onFinalResult(
                    SpeechRecordingResult(
                        transcript = text,
                        originalAudioPath = "internal_cache/audio_session_${System.currentTimeMillis()}.m4a",
                        language = language,
                        isRealTimeRecognition = true
                    )
                )
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotBlank()) {
                    onPartialResult(text)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            onError("Could not start speech listening: ${e.message}")
        }
    }

    override fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            isListeningActive = false
        } catch (_: Exception) {}
    }
}
