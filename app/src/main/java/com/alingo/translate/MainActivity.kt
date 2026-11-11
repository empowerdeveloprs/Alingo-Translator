package com.alingo.translate

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var mediaProjectionManager: MediaProjectionManager

    private val captureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val intent = Intent(this, ScreenCaptureService::class.java).apply {
                putExtra("RESULT_CODE", result.resultCode)
                putExtra("DATA", result.data)
            }
            startForegroundService(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 120, 60, 60)
        }

        val title = TextView(this).apply {
            text = "AlinGo Translate"
            textSize = 32f
            setPadding(0, 0, 0, 20)
        }

        val subtitle = TextView(this).apply {
            text = "Tap floating 'A' button on screen to translate."
            textSize = 16f
            setPadding(0, 0, 0, 40)
        }

        val btnDict = Button(this).apply {
            text = "📖 Dictionary (Offline)"
            setOnClickListener { startActivity(Intent(this@MainActivity, DictionaryScreen::class.java)) }
        }

        val btnSettings = Button(this).apply {
            text = "⚙️ Settings (Language & Appearance)"
            setOnClickListener { startActivity(Intent(this@MainActivity, SettingsScreen::class.java)) }
        }

        val btnHistory = Button(this).apply {
            text = "🕒 Translation History"
            setOnClickListener { startActivity(Intent(this@MainActivity, HistoryScreen::class.java)) }
        }

        val btnOverlay = Button(this).apply {
            text = "1️⃣ Allow Overlay Permission"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                }
            }
        }

        val btnStart = Button(this).apply {
            text = "2️⃣ Start Screen Translator"
            setOnClickListener {
                if (Settings.canDrawOverlays(this@MainActivity)) {
                    captureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
                } else {
                    title.text = "⚠️ Allow overlay permission first!"
                }
            }
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(btnDict)
        layout.addView(btnSettings)
        layout.addView(btnHistory)
        layout.addView(TextView(this).apply { text = "\n\n" })
        layout.addView(btnOverlay)
        layout.addView(btnStart)

        setContentView(layout)
    }
}
