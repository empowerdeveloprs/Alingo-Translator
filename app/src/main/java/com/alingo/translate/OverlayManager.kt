package com.alingo.translate

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView

class OverlayManager(private val context: Context) {
    private var windowManager: WindowManager? = null
    private var floatingView: TextView? = null
    private var params: WindowManager.LayoutParams? = null

    @SuppressLint("ClickableViewAccessibility")
    fun showOverlay(initialText: String) {
        if (floatingView != null) return

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        floatingView = TextView(context).apply {
            text = initialText
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#CC000000"))
            setPadding(30, 30, 30, 30)
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        // باکس کو ڈریگ (Move) کرنے کا لاجک
        floatingView?.setOnTouchListener(object : android.view.View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: android.view.View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params!!.x
                        initialY = params!!.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params!!.x = initialX + (event.rawX - initialTouchX).toInt()
                        params!!.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })

        windowManager?.addView(floatingView, params)
    }

    fun updateText(newText: String) {
        floatingView?.post { floatingView?.text = newText }
    }
}
