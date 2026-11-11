package com.alingo.translate

import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 60)
        }

        val title = TextView(this).apply {
            text = "Settings (ترتیبات)"
            textSize = 24f
            setPadding(0, 0, 0, 40)
        }

        val langNames = Languages.ALL.map { "${it.urduName} (${it.englishName})" }

        // سورس زبان کا انتخاب
        val sourceTitle = TextView(this).apply { text = "Translate FROM (کس زبان سے):" }
        val sourceSpinner = Spinner(this)
        sourceSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, langNames)
        val currentSourceCode = SettingsManager.getSourceLang(this)
        sourceSpinner.setSelection(Languages.ALL.indexOfFirst { it.code == currentSourceCode }.coerceAtLeast(0))
        
        // ٹارگٹ زبان کا انتخاب
        val targetTitle = TextView(this).apply { text = "\nTranslate TO (کس زبان میں):" }
        val targetSpinner = Spinner(this)
        targetSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, langNames)
        val currentTargetCode = SettingsManager.getTargetLang(this)
        targetSpinner.setSelection(Languages.ALL.indexOfFirst { it.code == currentTargetCode }.coerceAtLeast(0))

        val saveBtn = Button(this).apply {
            text = "💾 Save Settings (محفوظ کریں)"
            textSize = 16f
            setOnClickListener {
                val newSource = Languages.ALL[sourceSpinner.selectedItemPosition].code
                val newTarget = Languages.ALL[targetSpinner.selectedItemPosition].code
                
                if (newSource == newTarget) {
                    Toast.makeText(this@SettingsScreen, "Source and Target cannot be same!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                SettingsManager.setSourceLang(this@SettingsScreen, newSource)
                SettingsManager.setTargetLang(this@SettingsScreen, newTarget)
                
                Toast.makeText(this@SettingsScreen, "Settings Saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        layout.addView(title)
        layout.addView(sourceTitle)
        layout.addView(sourceSpinner)
        layout.addView(targetTitle)
        layout.addView(targetSpinner)
        layout.addView(TextView(this).apply { text = "\n" })
        layout.addView(saveBtn)
        
        setContentView(layout)
    }
}
