package com.alingo.translate

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.ByteArrayOutputStream

class ScreenCaptureService : Service() {
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var overlayManager: OverlayManager? = null
    private var isProcessing = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        overlayManager = OverlayManager(this)
        overlayManager?.showOverlay("AlinGo: Processing...")

        val resultCode = intent?.getIntExtra("RESULT_CODE", 0) ?: 0
        val data = intent?.getParcelableExtra<Intent>("DATA")
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        
        if (data != null) {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
            startScreenCapture()
        }

        return START_NOT_STICKY
    }

    private fun startScreenCapture() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)

        // اسکرین ریزولوشن کو تھوڑا کم کرتے ہیں تاکہ پروسیسنگ تیز ہو
        val width = metrics.widthPixels / 2
        val height = metrics.heightPixels / 2

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "AlinGoCapture", width, height, metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )

        // ہر 2 سیکنڈ بعد تصویر پکڑ کر پروسیس کریں
        imageReader?.setOnImageAvailableListener({ reader ->
            if (isProcessing) {
                reader.acquireLatestImage()?.close()
                return@setOnImageAvailableListener
            }
            
            val image = reader.acquireLatestImage()
            if (image != null) {
                isProcessing = true
                val planes = image.planes
                val buffer = planes[0].buffer
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.copyPixelsFromBuffer(buffer)
                image.close()

                processImage(bitmap)
            }
        }, null)
    }

    private fun processImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                if (extractedText.isNotBlank()) {
                    // انگلش سے اردو میں ترجمہ کریں
                    translateText(extractedText)
                } else {
                    isProcessing = false
                }
            }
            .addOnFailureListener {
                isProcessing = false
            }
    }

    private fun translateText(text: String) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.URDU) // اردو میں ترجمہ
            .build()
        val translator = Translation.getClient(options)

        // ترجمہ کرنے سے پہلے ماڈل ڈاؤن لوڈ ہونا ضروری ہے (یہ ایک بار ہوگا)
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        overlayManager?.updateText(translatedText)
                        isProcessing = false
                    }
                    .addOnFailureListener {
                        isProcessing = false
                    }
            }
            .addOnFailureListener {
                isProcessing = false
            }
    }

    private fun startForegroundService() {
        val channel = NotificationChannel("AlinGoChannel", "AlinGo Service", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notification = Notification.Builder(this, "AlinGoChannel")
            .setContentTitle("AlinGo Translate")
            .setContentText("Running in background...")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        virtualDisplay?.release()
        mediaProjection?.stop()
    }
}
