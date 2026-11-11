package com.alingo.translate

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 60, 40, 60) }
        layout.addView(TextView(this).apply { text = "Settings (ترتیبات)"; textSize = 24f; setPadding(0,0,0,20) })
        val langNames = Languages.ALL.map { "${it.urduName} (${it.englishName})" }

        // سورس زبان
        layout.addView(TextView(this).apply { text = "Translate FROM (کس زبان سے):" })
        val sourceSpinner = Spinner(this)
        sourceSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, langNames)
        sourceSpinner.setSelection(Languages.ALL.indexOfFirst { it.code == SettingsManager.getSourceLang(this) }.coerceAtLeast(0))
        layout.addView(sourceSpinner)

        // ٹارگٹ زبان
        layout.addView(TextView(this).apply { text = "\nTranslate TO (کس زبان میں):" })
        val targetSpinner = Spinner(this)
        targetSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, langNames)
        targetSpinner.setSelection(Languages.ALL.indexOfFirst { it.code == SettingsManager.getTargetLang(this) }.coerceAtLeast(0))
        layout.addView(targetSpinner)

        // OCR Script
        layout.addView(TextView(this).apply { text = "\nOCR Script:" })
        val ocrSpinner = Spinner(this)
        val ocrOptions = listOf("Latin (English)", "Chinese", "Japanese", "Korean", "Devanagari (Hindi)")
        val ocrCodes = listOf("latin", "chinese", "japanese", "korean", "devanagari")
        ocrSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ocrOptions)
        ocrSpinner.setSelection(ocrCodes.indexOf(SettingsManager.getOcrLang(this)).coerceAtLeast(0))
        layout.addView(ocrSpinner)

        val saveBtn = Button(this).apply {
            text = "💾 Save Settings"
            setOnClickListener {
                SettingsManager.setSourceLang(this@SettingsScreen, Languages.ALL[sourceSpinner.selectedItemPosition].code)
                SettingsManager.setTargetLang(this@SettingsScreen, Languages.ALL[targetSpinner.selectedItemPosition].code)
                SettingsManager.setOcrLang(this@SettingsScreen, ocrCodes[ocrSpinner.selectedItemPosition])
                Toast.makeText(this@SettingsScreen, "Saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        layout.addView(saveBtn)
        setContentView(ScrollView(this).apply { addView(layout) })
    }
}
