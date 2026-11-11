package com.alingo.translate

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

class MlKitTranslator {

    fun getTranslator(sourceLang: String, targetLang: String): Translator {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()
        return Translation.getClient(options)
    }

    fun translateText(text: String, sourceLang: String, targetLang: String, onResult: (String) -> Unit, onError: (Exception) -> Unit) {
        val translator = getTranslator(sourceLang, targetLang)
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        onResult(translatedText)
                    }
                    .addOnFailureListener { exception ->
                        onError(exception)
                    }
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}
