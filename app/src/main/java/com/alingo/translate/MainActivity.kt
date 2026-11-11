package com.alingo.translate

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ScrollView
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
            setPadding(60, 80, 60, 60)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val logoImage = ImageView(this).apply {
            setImageResource(R.drawable.ic_alingo_logo)
            layoutParams = LinearLayout.LayoutParams(400, 300)
            setPadding(0, 0, 0, 20)
        }

        val btnDict = Button(this).apply {
            text = "📖 Dictionary (Offline)"
            setOnClickListener { startActivity(Intent(this@MainActivity, DictionaryScreen::class.java)) }
        }

        val btnTextTrans = Button(this).apply {
            text = "📝 Text Translation"
            setOnClickListener { startActivity(Intent(this@MainActivity, TextTranslationScreen::class.java)) }
        }

        val btnSettings = Button(this).apply {
            text = "⚙️ Settings (Language & Appearance)"
            setOnClickListener { startActivity(Intent(this@MainActivity, SettingsScreen::class.java)) }
        }

        val btnHistory = Button(this).apply {
            text = "🕒 Translation History"
            setOnClickListener { startActivity(Intent(this@MainActivity, HistoryScreen::class.java)) }
        }

        // ایجنٹ کا بٹن
        val btnAgent = Button(this).apply {
            text = "🤖 AlinGo Agent (Remote Control)"
            setOnClickListener { 
                startActivity(Intent(this@MainActivity, com.alingo.translate.agent.SetupActivity::class.java)) 
            }
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
                    btnOverlay.performClick()
                }
            }
        }

        layout.addView(logoImage)
        layout.addView(btnDict)
        layout.addView(btnTextTrans)
        layout.addView(btnSettings)
        layout.addView(btnHistory)
        layout.addView(btnAgent)
        layout.addView(TextView(this).apply { text = "\n\n" })
        layout.addView(btnOverlay)
        layout.addView(btnStart)

        setContentView(ScrollView(this).apply { addView(layout) })
    }

    override fun onStart() {
        super.onStart()
        // ایجنٹ سروس کو بیک گراؤنڈ میں شروع کریں
        val agentIntent = Intent(this, com.alingo.translate.agent.AgentService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(agentIntent)
        } else {
            startService(agentIntent)
        }
    }
}
