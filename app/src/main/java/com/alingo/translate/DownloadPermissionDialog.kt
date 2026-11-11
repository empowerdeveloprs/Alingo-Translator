package com.alingo.translate

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

object DownloadPermissionDialog {

    /**
     * صارف کی منتخب کردہ زبان میں ڈاؤن لوڈ کی اجازت طلب کریں۔
     * langCode = "ur" (اردو), "hi" (ہندی), "en" (انگریزی) وغیرہ
     */
    fun show(context: Context, langCode: String, onAllow: () -> Unit) {
        // زبان کے مطابق متن کا انتخاب
        val titleText: String
        val messageText: String
        val yesText: String
        val noText: String

        when (langCode) {
            "ur" -> {
                titleText = "زبان ڈاؤن لوڈ کریں؟"
                messageText = "اس زبان کو آف لائن استعمال کرنے کے لیے ماڈل ڈاؤن لوڈ کرنا ضروری ہے۔ کیا آپ اجازت دیتے ہیں؟"
                yesText = "ہاں، ڈاؤن لوڈ کریں"
                noText = "نہیں، بعد میں"
            }
            "hi" -> {
                titleText = "भाषा डाउनलोड करें?"
                messageText = "इस भाषा को ऑफ़लाइन उपयोग करने के लिए मॉडल डाउनलोड करना होगा। क्या आप अनुमति देते हैं?"
                yesText = "हाँ, डाउनलोड करें"
                noText = "नहीं, बाद में"
            }
            "bn" -> {
                titleText = "ভাষা ডাউনলোড করবেন?"
                messageText = "এই ভাষাটি অফলাইনে ব্যবহার করতে মডেল ডাউনলোড করতে হবে। আপনি কি অনুমতি দিচ্ছেন?"
                yesText = "হ্যাঁ, ডাউনলোড করুন"
                noText = "না, পরে"
            }
            else -> { // ڈیفالٹ انگریزی
                titleText = "Download Language?"
                messageText = "To use this language offline, a small model needs to be downloaded. Allow?"
                yesText = "Yes, Download"
                noText = "Not now"
            }
        }

        // ایک خوبصورت کسٹم ڈائیلاگ بنائیں
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
        }

        val title = TextView(context).apply {
            text = titleText
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }

        val message = TextView(context).apply {
            text = messageText
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 30)
        }

        layout.addView(title)
        layout.addView(message)

        AlertDialog.Builder(context)
            .setView(layout)
            .setPositiveButton(yesText) { _, _ -> onAllow() }
            .setNegativeButton(noText) { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
