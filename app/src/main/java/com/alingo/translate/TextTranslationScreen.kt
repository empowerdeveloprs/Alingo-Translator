package com.alingo.translate

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class TextTranslationScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 60)
        }

        val title = TextView(this).apply { text = "Text Translation (ٹیکسٹ ترجمہ)"; textSize = 20f }
        val inputText = EditText(this).apply { hint = "Type or paste text here (یہاں ٹائپ کریں)"; minLines = 3 }
        val translateBtn = Button(this).apply { text = "Translate (ترجمہ کریں)" }
        val resultText = TextView(this).apply { 
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 30, 0, 0) 
        }
        
        layout.addView(title)
        layout.addView(inputText)
        layout.addView(translateBtn)
        layout.addView(resultText)

        translateBtn.setOnClickListener {
            val word = inputText.text.toString().trim()
            if (word.isEmpty()) return@setOnClickListener
            
            resultText.text = "Translating..."
            
            val sourceCode = SettingsManager.getSourceLang(this)
            val targetCode = SettingsManager.getTargetLang(this)
            
            // ML Kit language mapping
            val sourceMl = if (sourceCode == "auto" || sourceCode == "en") TranslateLanguage.ENGLISH else toMlKit(sourceCode) ?: TranslateLanguage.ENGLISH
            val targetMl = toMlKit(targetCode) ?: TranslateLanguage.URDU

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceMl)
                .setTargetLanguage(targetMl)
                .build()
            val translator = Translation.getClient(options)

            translator.downloadModelIfNeeded()
                .addOnSuccessListener {
                    translator.translate(word)
                        .addOnSuccessListener { res -> resultText.text = res }
                        .addOnFailureListener { resultText.text = "Error: ${it.message}" }
                }
                .addOnFailureListener { resultText.text = "Model download failed" }
        }

        setContentView(layout)
    }

    private fun toMlKit(code: String): String? {
        return when (code) {
            "ur" -> TranslateLanguage.URDU; "hi" -> TranslateLanguage.HINDI
            "bn" -> TranslateLanguage.BENGALI; "pa" -> "pa"; "gu" -> "gu"
            "ta" -> "ta"; "te" -> "te"; "kn" -> "kn"; "ml" -> "ml"; "mr" -> "mr"
            else -> null
        }
    }
}
