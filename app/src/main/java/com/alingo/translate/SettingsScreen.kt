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

        layout.addView(TextView(this).apply { text = "Settings (ترتیبات)"; textSize = 24f; setPadding(0,0,0,20) })

        val langNames = Languages.ALL.map { "${it.urduName} (${it.englishName})" }

        // ٹارگٹ زبان (جس میں ترجمہ کرنا ہے)
        layout.addView(TextView(this).apply { text = "Translate TO (کس زبان میں):" })
        val targetSpinner = Spinner(this)
        targetSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, langNames)
        targetSpinner.setSelection(Languages.ALL.indexOfFirst { it.code == SettingsManager.getTargetLang(this) }.coerceAtLeast(0))
        layout.addView(targetSpinner)

        // زبان منتخب کرنے پر فوری چیک
        targetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedLangCode = Languages.ALL[position].code
                // اگر زبان ڈاؤن لوڈ نہیں ہے تو ڈائیلاگ دکھائیں
                if (!LanguageModelManager.isLanguageDownloaded(this@SettingsScreen, selectedLangCode)) {
                    DownloadPermissionDialog.show(this@SettingsScreen, selectedLangCode) {
                        // صارف نے "ہاں" کہا، ڈاؤن لوڈ شروع کریں
                        Toast.makeText(this@SettingsScreen, "Downloading model...", Toast.LENGTH_SHORT).show()
                        LanguageModelManager.downloadLanguage(
                            this@SettingsScreen,
                            selectedLangCode,
                            onSuccess = {
                                runOnUiThread {
                                    Toast.makeText(this@SettingsScreen, "Download complete!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onError = { error ->
                                runOnUiThread {
                                    Toast.makeText(this@SettingsScreen, error, Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val saveBtn = Button(this).apply {
            text = "💾 Save Settings"
            setOnClickListener {
                SettingsManager.setTargetLang(this@SettingsScreen, Languages.ALL[targetSpinner.selectedItemPosition].code)
                Toast.makeText(this@SettingsScreen, "Saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        layout.addView(saveBtn)
        setContentView(ScrollView(this).apply { addView(layout) })
    }
}
