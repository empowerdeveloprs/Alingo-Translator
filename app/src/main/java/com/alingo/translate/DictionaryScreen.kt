package com.alingo.translate

import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class DictionaryScreen : AppCompatActivity() {

    private lateinit var engine: DictionaryEngine
    private lateinit var ttsManager: TtsManager
    private var sourceLang: String = "en"
    private var targetLang: String = "ur"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        engine = DictionaryEngine(this)
        ttsManager = TtsManager(this)

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 60)
        }
        scroll.addView(layout)

        // زبانوں کے اسپنر
        val langRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        val sourceSpinner = Spinner(this)
        val targetSpinner = Spinner(this)

        val langNames = Languages.ALL.map { "${it.urduName} (${it.englishName})" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, langNames)
        sourceSpinner.adapter = adapter
        targetSpinner.adapter = adapter

        sourceSpinner.setSelection(Languages.ALL.indexOfFirst { it.code == "en" })
        targetSpinner.setSelection(Languages.ALL.indexOfFirst { it.code == "ur" })

        sourceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                sourceLang = Languages.ALL[pos].code
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        targetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                targetLang = Languages.ALL[pos].code
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        langRow.addView(sourceSpinner, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        langRow.addView(TextView(this).apply { text = " → "; textSize = 22f })
        langRow.addView(targetSpinner, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val inputWord = EditText(this).apply {
            hint = "لفظ لکھیں (Hello, کتاب)"
            textSize = 18f
        }

        val searchBtn = Button(this).apply {
            text = "🔍 تلاش کریں (ترجمہ + تلفظ)"
            textSize = 16f
        }

        // نتیجے کا باکس
        val resultBox = TextView(this).apply {
            textSize = 18f
            setPadding(20, 30, 20, 30)
            setBackgroundColor(0xFFF0F0F0.toInt())
            text = "نتیجہ یہاں ظاہر ہوگا..."
        }

        // 🔊 اسپیکر بٹن
        val speakBtn = Button(this).apply {
            text = "🔊 آواز سنیں (Play Pronunciation)"
            visibility = android.view.View.GONE
        }
        
        var currentTranslationToSpeak = ""
        var currentLangToSpeak = ""

        searchBtn.setOnClickListener {
            val word = inputWord.text.toString().trim()
            if (word.isEmpty()) {
                resultBox.text = "⚠️ پہلے کوئی لفظ لکھیں"
                return@setOnClickListener
            }
            resultBox.text = "⏳ ترجمہ ہو رہا ہے..."
            speakBtn.visibility = android.view.View.GONE
            
            lifecycleScope.launch {
                val result = engine.lookup(word, sourceLang, targetLang)
                resultBox.text = buildResult(result)
                
                // ترجمے کو آواز میں سنانے کے لیے تیار کریں
                currentTranslationToSpeak = result.translation
                currentLangToSpeak = result.targetLang
                speakBtn.visibility = android.view.View.VISIBLE
            }
        }

        speakBtn.setOnClickListener {
            if (currentTranslationToSpeak.isNotEmpty()) {
                ttsManager.speak(currentTranslationToSpeak, currentLangToSpeak)
            }
        }

        layout.addView(langRow)
        layout.addView(TextView(this).apply { text = "\n" })
        layout.addView(inputWord)
        layout.addView(searchBtn)
        layout.addView(TextView(this).apply { text = "\n" })
        layout.addView(resultBox)
        layout.addView(speakBtn)

        setContentView(scroll)
    }

    override fun onDestroy() {
        ttsManager.shutdown()
        super.onDestroy()
    }

    private fun buildResult(r: DictionaryResult): String {
        val sourceLangName = Languages.findByCode(r.sourceLang)?.urduName ?: r.sourceLang
        val targetLangName = Languages.findByCode(r.targetLang)?.urduName ?: r.targetLang
        return buildString {
            appendLine("📖 لفظ: ${r.sourceWord} ($sourceLangName)")
            appendLine("➡️ ترجمہ: ${r.translation} ($targetLangName)")
            if (r.pronunciation.isNotBlank()) {
                appendLine("🔊 تلفظ: ${r.pronunciation}")
            }
            if (r.ipaPronunciation != null) {
                appendLine("🎯 IPA (انگریزی): ${r.ipaPronunciation}")
            }
        }
    }
}
