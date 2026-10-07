package com.example.voiceassistant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

// ভয়েস শোনা ও টেক্সটে রূপান্তরের দায়িত্বে এই ক্লাস
class SpeechManager(
    private val context: Context,
    private val onFinalText: (String) -> Unit,
    private val onListening: (Boolean) -> Unit,
    private val onProblem: (String) -> Unit
) {
    private var recognizer: SpeechRecognizer? = null

    // bn-BD = বাংলা, en-US = ইংরেজি
    var languageTag: String = "bn-BD"

    fun start() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onProblem("এই ফোনে ভয়েস রিকগনিশন সার্ভিস পাওয়া যায়নি")
            return
        }
        destroy()

        val r = SpeechRecognizer.createSpeechRecognizer(context)
        r.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                onListening(true)
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onError(error: Int) {
                onListening(false)
                onProblem(errorText(error))
            }

            override fun onResults(results: Bundle?) {
                onListening(false)
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = list?.firstOrNull()
                if (text.isNullOrBlank()) {
                    onProblem("বুঝতে পারিনি, আবার বলুন")
                } else {
                    onFinalText(text)
                }
            }
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        recognizer = r
        r.startListening(intent)
    }

    fun stop() {
        recognizer?.stopListening()
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }

    private fun errorText(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_NO_MATCH -> "বুঝতে পারিনি, আবার বলুন"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "কোনো কথা শোনা যায়নি"
        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ইন্টারনেট সংযোগ দরকার"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "মাইক্রোফোনের অনুমতি দিন"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "একটু পরে আবার চেষ্টা করুন"
        else -> "ভয়েস এরর (কোড $code)"
    }
}
