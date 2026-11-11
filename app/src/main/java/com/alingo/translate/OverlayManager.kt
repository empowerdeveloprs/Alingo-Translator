package com.alingo.translate

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlin.math.abs

data class TranslationOverlay(val text: String, val x: Int, val y: Int, val width: Int, val height: Int)

class OverlayManager(private val context: Context) {
    private var windowManager: WindowManager? = null
    private var floatingBall: ImageView? = null
    private var ballParams: WindowManager.LayoutParams? = null
    private val translationViews = mutableListOf<TextView>()
    private var ttsManager: TtsManager? = null

    var onBallClicked: (() -> Unit)? = null
    var onWordSelectClicked: (() -> Unit)? = null
    private var wordSelectButton: TextView? = null

    @SuppressLint("ClickableViewAccessibility")
    fun showFloatingBall() {
        if (floatingBall != null) return
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        ttsManager = TtsManager(context)

        // یہاں ہم نے TextView کی جگہ ImageView استعمال کیا ہے
        floatingBall = ImageView(context).apply {
            setImageResource(R.drawable.ic_alingo_logo)
            // لوگو کو گول کرنے کے لیے
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.parseColor("#E6000000")) // نیم شفاف سیاہ بیک گراؤنڈ
            setPadding(15, 15, 15, 15)
        }

        ballParams = WindowManager.LayoutParams(
            140, 140, // تھوڑا بڑا سائز تاکہ لوگو صاف نظر آئے
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
        // ورڈ سلیکٹ کا چھوٹا بٹن
        wordSelectButton = TextView(context).apply {
            text = "🔍"
            textSize = 20f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            val bg = GradientDrawable()
            bg.shape = GradientDrawable.OVAL
            bg.setColor(Color.parseColor("#FFD700")) // سنہری
            this.background = bg
            setOnClickListener { onWordSelectClicked?.invoke() }
        }

        val wordParams = WindowManager.LayoutParams(
            100, 100,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100; y = 440 // بٹن کے نیچے
        }
        
        wordSelectButton?.let { wmView ->
            val wm = windowManager
            wm?.addView(wmView, wordParams)
            // اسے بھی ڈریگ کرنے کے قابل بنائیں
            wmView.setOnTouchListener(object : View.OnTouchListener {
                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f
                private var isDragging = false

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = wordParams.x; initialY = wordParams.y
                            initialTouchX = event.rawX; initialTouchY = event.rawY
                            isDragging = false; return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (abs(event.rawX - initialTouchX) > 10 || abs(event.rawY - initialTouchY) > 10) isDragging = true
                            wordParams.x = initialX + (event.rawX - initialTouchX).toInt()
                            wordParams.y = initialY + (event.rawY - initialTouchY).toInt()
                            wm?.updateViewLayout(wmView, wordParams); return true
                        }
                        MotionEvent.ACTION_UP -> {
                            if (!isDragging) v.performClick()
                            return true
                        }
                    }
                    return false
                }
            })
        }
        windowManager?.addView(floatingBall, ballParams)
    }

    /**
     * الفاظ منتخب کرنے کے لیے ایک باکس دکھائیں
     */
    fun showSelectionBox(onSelected: (x: Int, y: Int, w: Int, h: Int) -> Unit) {
        val wm = windowManager ?: return
        clearTranslationBlocks()

        val density = context.resources.displayMetrics.density
        val boxSize = (120 * density).toInt() // 120dp کا باکس

        val selectionView = android.widget.FrameLayout(context).apply {
            setBackgroundColor(Color.parseColor("#8000C853")) // نیم شفاف سبز رنگ
            // ایک سنہری بارڈر
            val border = GradientDrawable()
            border.setStroke(4, Color.parseColor("#FFD700"))
            border.cornerRadius = 10f
            background = border
        }

        val params = WindowManager.LayoutParams(
            boxSize, boxSize,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 200; y = 400
        }

        // باکس کو ڈریگ کرنے کا لاجک
        selectionView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        wm.updateViewLayout(selectionView, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        // جب صارف انگلی اٹھائے تو منتخب شدہ حصہ بھیجیں
                        onSelected(params.x, params.y, params.width, params.height)
                        wm.removeView(selectionView)
                        return true
                    }
                }
                return false
            }
        })

        wm.addView(selectionView, params)
    }

    fun showTranslationBlocks(blocks: List<TranslationOverlay>, targetLangCode: String) {
        val wm = windowManager ?: return
        clearTranslationBlocks()
        
        val density = context.resources.displayMetrics.density
        val fontScale = SettingsManager.getFontScale(context)
        val bgOpacity = SettingsManager.getBgOpacity(context)
        val bgColor = Color.argb(bgOpacity, 0, 0, 0)

        blocks.forEach { block ->
            if (block.text.isBlank()) return@forEach

            val tv = TextView(context).apply {
                text = block.text
                setTextColor(Color.WHITE)
                setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                setBackgroundColor(bgColor)
                setPadding(8, 6, 8, 6)
                gravity = Gravity.CENTER
                val baseSizeSp = (block.height / density * 0.55f).coerceIn(10f, 22f)
                textSize = baseSizeSp * fontScale
                maxLines = 6
                
                setOnClickListener { ttsManager?.speak(block.text, targetLangCode) }
                setOnLongClickListener {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("AlinGo", block.text))
                    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
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
