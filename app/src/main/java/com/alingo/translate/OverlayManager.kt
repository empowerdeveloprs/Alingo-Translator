package com.alingo.translate

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import kotlin.math.abs

data class TranslationOverlay(val text: String, val x: Int, val y: Int, val width: Int, val height: Int)

class OverlayManager(private val context: Context) {
    private var windowManager: WindowManager? = null
    private var floatingBall: TextView? = null
    private var ballParams: WindowManager.LayoutParams? = null
    private val translationViews = mutableListOf<TextView>()
    private var ttsManager: TtsManager? = null

    var onBallClicked: (() -> Unit)? = null

    @SuppressLint("ClickableViewAccessibility")
    fun showFloatingBall() {
        if (floatingBall != null) return
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        ttsManager = TtsManager(context)

        floatingBall = TextView(context).apply {
            text = "A"
            textSize = 20f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            val bg = GradientDrawable()
            bg.shape = GradientDrawable.OVAL
            bg.setColor(Color.parseColor("#E91E63"))
            this.background = bg
        }

        ballParams = WindowManager.LayoutParams(
            120, 120,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100; y = 300
        }

        floatingBall?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0; private var initialY = 0
            private var initialTouchX = 0f; private var initialTouchY = 0f
            private var isDragging = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = ballParams!!.x; initialY = ballParams!!.y
                        initialTouchX = event.rawX; initialTouchY = event.rawY
                        isDragging = false; return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (abs(event.rawX - initialTouchX) > 10 || abs(event.rawY - initialTouchY) > 10) isDragging = true
                        ballParams!!.x = initialX + (event.rawX - initialTouchX).toInt()
                        ballParams!!.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(floatingBall, ballParams); return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) onBallClicked?.invoke()
                        return true
                    }
                }
                return false
            }
        })
        windowManager?.addView(floatingBall, ballParams)
    }

    fun showTranslationBlocks(blocks: List<TranslationOverlay>, targetLangCode: String) {
        val wm = windowManager ?: return
        clearTranslationBlocks()
        
        val density = context.resources.displayMetrics.density
        val fontScale = SettingsManager.getFontScale(context)
        val bgOpacity = SettingsManager.getBgOpacity(context)
        
        // Hex color with dynamic alpha for background
        val bgColor = Color.argb(bgOpacity, 0, 0, 0) // Black with custom transparency

        blocks.forEach { block ->
            if (block.text.isBlank()) return@forEach

            val tv = TextView(context).apply {
                text = block.text
                setTextColor(Color.WHITE)
                setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                setBackgroundColor(bgColor) // اپلائی کیا گیا شفاف رنگ
                setPadding(8, 6, 8, 6)
                gravity = Gravity.CENTER
                
                // اپلائی کیا گیا فونٹ سائز
                val baseSizeSp = (block.height / density * 0.55f).coerceIn(10f, 22f)
                textSize = baseSizeSp * fontScale
                maxLines = 6
                
                // ٹیپ پر آواز
                setOnClickListener {
                    ttsManager?.speak(block.text, targetLangCode)
                }

                // لانگ پریس پر کاپی
                setOnLongClickListener {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("AlinGo Translation", block.text)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                    true
                }
            }

            val params = WindowManager.LayoutParams(
                block.width, block.height,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = block.x; y = block.y
            }

            try {
                wm.addView(tv, params)
                translationViews.add(tv)
            } catch (_: Exception) {}
        }
    }

    fun clearTranslationBlocks() {
        val wm = windowManager ?: return
        translationViews.forEach { try { wm.removeView(it) } catch (_: Exception) {} }
        translationViews.clear()
    }

    fun removeOverlays() {
        floatingBall?.let { try { windowManager?.removeView(it) } catch (_: Exception) {} }
        floatingBall = null
        clearTranslationBlocks()
        ttsManager?.shutdown()
    }
}
