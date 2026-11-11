package com.alingo.translate

import android.content.Context
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.TranslateLanguage
import java.io.File

object LanguageModelManager {

    // ML Kit کی 58 آف لائن زبانوں کا کوڈ
    private val mlKitLanguages = setOf(
        "en", "ur", "hi", "bn", "pa", "gu", "ta", "te", "kn", "ml", "mr", "ne", "si",
        "ar", "zh", "ja", "ko", "es", "fr", "de", "ru", "pt", "tr", "fa", "id", "th",
        "vi", "it", "nl", "pl", "uk", "ro", "el", "hu", "cs", "sv", "no", "da", "fi",
        "he", "ms", "tl", "sw", "am", "az", "be", "bg", "ca", "et", "eu", "gl", "ka",
        "lt", "lv", "mk", "sk", "sl", "sq", "sr", "hr", "bs", "is", "ga", "cy"
    )

    // NLLB-200 (سندھی، پشتو وغیرہ) کے لیے الگ کوڈز
    private val nllbLanguages = setOf("sd", "ps", "bal", "skr", "ks")

    /**
     * چیک کریں کہ آیا اس زبان کا ماڈل ڈیوائس پر موجود ہے یا نہیں۔
     */
    fun isLanguageDownloaded(context: Context, langCode: String): Boolean {
        if (langCode == "auto" || langCode == "en") return true // انگریزی اور آٹو بلٹ اِن ہیں

        val modelDir = File(context.filesDir, "models/$langCode")
        return modelDir.exists() && modelDir.listFiles()?.isNotEmpty() == true
    }

    /**
     * زبان کو ڈاؤن لوڈ کرنے کا طریقہ (ML Kit یا NLLB)
     */
    fun downloadLanguage(
        context: Context,
        langCode: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (mlKitLanguages.contains(langCode)) {
            // ML Kit سے ڈاؤن لوڈ کریں (تقریباً 30MB)
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(toMlKitCode(langCode) ?: TranslateLanguage.URDU)
                .build()
            val translator = Translation.getClient(options)

            translator.downloadModelIfNeeded()
                .addOnSuccessListener {
                    // ماڈل ڈاؤن لوڈ ہونے کے بعد ایک نشان (marker) بنائیں
                    val modelDir = File(context.filesDir, "models/$langCode")
                    modelDir.mkdirs()
                    File(modelDir, "mlkit_done").createNewFile()
                    onSuccess()
                }
                .addOnFailureListener {
                    onError("ML Kit download failed: ${it.message}")
                }
        } else if (nllbLanguages.contains(langCode)) {
            // NLLB-200 ماڈل ڈاؤن لوڈ کریں (تقریباً 1.14GB)
            // نوٹ: یہ ایک بھاری فائل ہے، اسے صرف اس وقت ڈاؤن لوڈ کریں جب صارف اجازت دے
            val nllbUrl = "https://huggingface.co/SivanKTR/nllb-200-distilled-600M-int8-android/resolve/main/model.onnx"
            // آپ کو یہاں ایک ڈاؤن لوڈ مینیجر (جیسے OkHttp) استعمال کرنا ہوگا
            // فی الحال ہم ایک سادہ پیغام دکھا رہے ہیں
            onError("NLLB model requires manual setup. Please contact support.")
        } else {
            onError("Language not supported offline yet.")
        }
    }

    private fun toMlKitCode(langCode: String): String? {
        return when (langCode) {
            "en" -> TranslateLanguage.ENGLISH; "ur" -> TranslateLanguage.URDU
            "hi" -> TranslateLanguage.HINDI; "bn" -> TranslateLanguage.BENGALI
            "pa" -> "pa"; "gu" -> "gu"; "ta" -> "ta"; "te" -> "te"
            "kn" -> "kn"; "ml" -> "ml"; "mr" -> "mr"; "ne" -> "ne"; "si" -> "si"
            "ar" -> TranslateLanguage.ARABIC; "zh" -> TranslateLanguage.CHINESE
            "ja" -> TranslateLanguage.JAPANESE; "ko" -> TranslateLanguage.KOREAN
            "es" -> TranslateLanguage.SPANISH; "fr" -> TranslateLanguage.FRENCH
            "de" -> TranslateLanguage.GERMAN; "ru" -> TranslateLanguage.RUSSIAN
            "tr" -> TranslateLanguage.TURKISH; "fa" -> TranslateLanguage.PERSIAN
            else -> null
        }
    }
}
