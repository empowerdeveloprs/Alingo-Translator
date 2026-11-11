package com.alingo.translate

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
        }
    }

    /**
     * متن کو بول کر سنائیں
     */
    fun speak(text: String, langCode: String) {
        if (!isReady || tts == null) return

        val locale = getLocaleForLang(langCode)
        tts?.language = locale

        // اگر زبان کا ڈیٹا انسٹال نہیں ہے تو اینڈرائیڈ کا ڈیفالٹ استعمال ہوگا
        if (tts?.isLanguageAvailable(locale) == TextToSpeech.LANG_MISSING_DATA || 
            tts?.isLanguageAvailable(locale) == TextToSpeech.LANG_NOT_SUPPORTED) {
            // اردو/عالمی زبانوں کے لیے اینڈرائیڈ سیٹنگز سے وائس ڈاؤن لوڈ کرنی پڑ سکتی ہے
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AlinGoTTS")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
    }

    private fun getLocaleForLang(langCode: String): Locale {
        return when (langCode) {
            "en" -> Locale.ENGLISH
            "ur" -> Locale("ur", "PK")
            "hi" -> Locale("hi", "IN")
            "bn" -> Locale("bn", "BD")
            "pa" -> Locale("pa", "IN")
            "gu" -> Locale("gu", "IN")
            "ta" -> Locale("ta", "IN")
            "te" -> Locale("te", "IN")
            "mr" -> Locale("mr", "IN")
            else -> Locale.getDefault()
        }
    }
}
