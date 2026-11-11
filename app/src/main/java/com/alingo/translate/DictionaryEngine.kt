package com.alingo.translate

import android.content.Context
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class DictionaryResult(
    val sourceWord: String,
    val translation: String,
    val pronunciation: String,      // رومن میں تلفظ
    val ipaPronunciation: String?,  // IPA (صرف انگریزی)
    val sourceLang: String,
    val targetLang: String
)

class DictionaryEngine(private val context: Context) {

    suspend fun lookup(
        word: String,
        sourceLang: String,
        targetLang: String
    ): DictionaryResult = suspendCancellableCoroutine { cont ->
        
        if (sourceLang == targetLang) {
            cont.resume(
                DictionaryResult(
                    word, word, TransliterationHelper.getPronunciation(word, sourceLang),
                    null, sourceLang, targetLang
                )
            )
            return@suspendCancellableCoroutine
        }

        // اگر سورس زبان انگریزی ہے تو IPA اور آڈیو حاصل کریں
        var ipa: String? = null
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            if (sourceLang == "en") {
                val info = EnglishDictionaryAPI.fetchWordInfo(word)
                ipa = info?.phonetic
            }
        }

        val sourceMlKit = toMlKitCode(sourceLang)
        val targetMlKit = toMlKitCode(targetLang)

        if (sourceMlKit == null || targetMlKit == null) {
            cont.resume(
                DictionaryResult(
                    word, "Not available offline (need online/model)", "", ipa,
                    sourceLang, targetLang
                )
            )
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
                        cont.resume(
                            DictionaryResult(
                                sourceWord = word,
                                translation = result,
                                pronunciation = pron,
                                ipaPronunciation = ipa,
                                sourceLang = sourceLang,
                                targetLang = targetLang
                            )
                        )
                    }
                    .addOnFailureListener {
                        cont.resume(DictionaryResult(word, "Translation failed: ${it.message}", "", ipa, sourceLang, targetLang))
                    }
            }
            .addOnFailureListener {
                cont.resume(DictionaryResult(word, "Model download failed", "", ipa, sourceLang, targetLang))
            }
    }

    private fun toMlKitCode(langCode: String): String? {
        return when (langCode) {
            "en" -> TranslateLanguage.ENGLISH
            "ur" -> TranslateLanguage.URDU
            "hi" -> TranslateLanguage.HINDI
            "bn" -> TranslateLanguage.BENGALI
            "pa" -> TranslateLanguage.valueOf("PUNJABI")
            "gu" -> TranslateLanguage.valueOf("GUJARATI")
            "ta" -> TranslateLanguage.valueOf("TAMIL")
            "te" -> TranslateLanguage.valueOf("TELUGU")
            "kn" -> TranslateLanguage.valueOf("KANNADA")
            "ml" -> TranslateLanguage.valueOf("MALAYALAM")
            "mr" -> TranslateLanguage.valueOf("MARATHI")
            "ne" -> TranslateLanguage.valueOf("NEPALI")
            "si" -> TranslateLanguage.valueOf("SINHALA")
            else -> null
        }
    }
}
