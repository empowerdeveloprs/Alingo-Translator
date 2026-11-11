package com.alingo.translate

import android.os.Bundle
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
            setPadding(0, 0, 0, 20)
        }

        val langNames = Languages.ALL.map { "${it.urduName} (${it.englishName})" }

        // سورس
        layout.addView(TextView(this).apply { text = "Translate FROM (کس زبان سے):" })
        val sourceSpinner = Spinner(this)
        sourceSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, langNames)
        val currentSourceCode = SettingsManager.getSourceLang(this)
        sourceSpinner.setSelection(Languages.ALL.indexOfFirst { it.code == currentSourceCode }.coerceAtLeast(0))
        layout.addView(sourceSpinner)
        
        // ٹارگٹ
        layout.addView(TextView(this).apply { text = "\nTranslate TO (کس زبان میں):" })
        val targetSpinner = Spinner(this)
        targetSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, langNames)
        val currentTargetCode = SettingsManager.getTargetLang(this)
        targetSpinner.setSelection(Languages.ALL.indexOfFirst { it.code == currentTargetCode }.coerceAtLeast(0))
        layout.addView(targetSpinner)

        // فونٹ سائز
        layout.addView(TextView(this).apply { text = "\nFont Size (فونٹ سائز):" })
        val fontSeekBar = SeekBar(this).apply {
            max = 20 // 0.5 to 2.5
            progress = ((SettingsManager.getFontScale(this@SettingsScreen) - 0.5f) * 10).toInt()
        }
        layout.addView(fontSeekBar)

        // بیک گراؤنڈ شفافیت
        layout.addView(TextView(this).apply { text = "\nBackground Opacity (پس منظر کی شفافیت):" })
        val opacitySeekBar = SeekBar(this).apply {
            max = 255
            progress = SettingsManager.getBgOpacity(this@SettingsScreen)
        }
        layout.addView(opacitySeekBar)

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
                
                val newScale = 0.5f + (fontSeekBar.progress / 10f)
                SettingsManager.setFontScale(this@SettingsScreen, newScale)
                SettingsManager.setBgOpacity(this@SettingsScreen, opacitySeekBar.progress)
                
                Toast.makeText(this@SettingsScreen, "Settings Saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        layout.addView(TextView(this).apply { text = "\n" })
        layout.addView(saveBtn)
        
        setContentView(ScrollView(this).apply { addView(layout) })
    }
}
