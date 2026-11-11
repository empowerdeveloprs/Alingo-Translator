package com.alingo.translate

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class HistoryScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 60)
        }

        val title = TextView(this).apply {
            text = "Translation History (ہسٹری)"
            textSize = 24f
            setPadding(0, 0, 0, 20)
        }

        val clearBtn = Button(this).apply {
            text = "🗑️ Clear All History"
            setOnClickListener {
                HistoryManager.clearHistory(this@HistoryScreen)
                recreate() // سکرین ری فریش کریں
            }
        }

        layout.addView(title)
        layout.addView(clearBtn)
        layout.addView(TextView(this).apply { text = "\n" })

        val historyList = HistoryManager.getHistory(this)
        if (historyList.isEmpty()) {
            layout.addView(TextView(this).apply { text = "No history yet. Translate something!" })
        } else {
            historyList.forEach { pair ->
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, 10, 0, 20)
                    setBackgroundColor(0xFFF0F0F0.toInt())
                }
                val src = TextView(this).apply { text = "🔴 ${pair.first}"; textSize = 16f }
                val dst = TextView(this).apply { text = "🟢 ${pair.second}"; textSize = 18f; setTypeface(null, android.graphics.Typeface.BOLD) }
                card.addView(src)
                card.addView(dst)
                layout.addView(card)
                layout.addView(TextView(this).apply { text = "\n" })
            }
        }

        setContentView(ScrollView(this).apply { addView(layout) })
    }
}
