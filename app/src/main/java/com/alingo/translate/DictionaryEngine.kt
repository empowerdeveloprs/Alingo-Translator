package com.alingo.translate

import android.content.Context
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

data class DictionaryResult(
    val sourceWord: String,
    val translation: String,
    val pronunciation: String,
    val ipaPronunciation: String?,
    val sourceLang: String,
    val targetLang: String
)

class DictionaryEngine(private val context: Context) {

    suspend fun lookup(word: String, sourceLang: String, targetLang: String): DictionaryResult = suspendCancellableCoroutine { cont ->
        if (sourceLang == targetLang) {
            cont.resume(DictionaryResult(word, word, TransliterationHelper.getPronunciation(word, sourceLang), null, sourceLang, targetLang))
            return@suspendCancellableCoroutine
        }

        val sourceMlKit = toMlKitCode(sourceLang)
        val targetMlKit = toMlKitCode(targetLang)

        if (sourceMlKit == null || targetMlKit == null) {
            cont.resume(DictionaryResult(word, "Not available offline", "", null, sourceLang, targetLang))
            return@suspendCancellableCoroutine
        }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceMlKit)
            .setTargetLanguage(targetMlKit)
            .build()
        val translator = Translation.getClient(options)

        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translator.translate(word)
                    .addOnSuccessListener { result ->
                        val pron = TransliterationHelper.getPronunciation(result, targetLang)
                        cont.resume(DictionaryResult(word, result, pron, null, sourceLang, targetLang))
                    }
                    .addOnFailureListener {
                        cont.resume(DictionaryResult(word, "Translation failed", "", null, sourceLang, targetLang))
                    }
            }
            .addOnFailureListener {
                cont.resume(DictionaryResult(word, "Model download failed", "", null, sourceLang, targetLang))
            }
    }

    private fun toMlKitCode(langCode: String): String? {
        return when (langCode) {
            "en" -> TranslateLanguage.ENGLISH; "ur" -> TranslateLanguage.URDU
            "hi" -> TranslateLanguage.HINDI; "bn" -> TranslateLanguage.BENGALI
            "pa" -> "pa"; "gu" -> "gu"; "ta" -> "ta"; "te" -> "te"
            "kn" -> "kn"; "ml" -> "ml"; "mr" -> "mr"; "ne" -> "ne"; "si" -> "si"
            else -> null
        }
    }
}
