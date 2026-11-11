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

class ScreenCaptureService : Service() {
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var overlayManager: OverlayManager? = null
    private val scaleFactor = 2
    private var isProcessing = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        overlayManager = OverlayManager(this)
        
        overlayManager?.onBallClicked = {
            if (!isProcessing) {
                isProcessing = true
                overlayManager?.clearTranslationBlocks()
                
                if (ShizukuManager.isShizukuAvailable() && ShizukuManager.hasPermission()) {
                    captureWithMediaProjection()
                } else {
                    captureWithMediaProjection()
                }
            }
        }
        
        overlayManager?.onWordSelectClicked = {
            // صارف نے ورڈ سلیکٹ کا بٹن دبایا
            overlayManager?.showSelectionBox { x, y, w, h ->
                if (!isProcessing) {
                    isProcessing = true
                    overlayManager?.clearTranslationBlocks()
                    captureWordArea(x, y, w, h)
                }
            }
        }
        overlayManager?.onWordSelectClicked = {
            overlayManager?.showWordSelectorBox { x, y, w, h ->
                if (!isProcessing) {
                    isProcessing = true
                    overlayManager?.clearTranslationBlocks()
                    captureWordArea(x, y, w, h)
                }
            }
        }
        overlayManager?.showFloatingBall()

        // اگر میڈیا پروجیکشن کی اجازت پہلے سے موجود ہے
        setupMediaProjection(intent)

        return START_NOT_STICKY
    }

    private fun setupMediaProjection(intent: Intent?) {
        val resultCode = intent?.getIntExtra("RESULT_CODE", 0) ?: 0
        val data = intent?.getParcelableExtra<Intent>("DATA")
        val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        if (data != null) {
            mediaProjection = mpm.getMediaProjection(resultCode, data)
            setupVirtualDisplay()
        }
    }

    private fun captureWithMediaProjection() {
        if (imageReader == null) {
            isProcessing = false
            return
        }
        val image = imageReader?.acquireLatestImage()
        if (image == null) { isProcessing = false; return }

        val width = image.width
        val height = image.height
        val planes = image.planes
        val buffer = planes[0].buffer
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        image.close()

        processImage(bitmap)
    }

    private fun setupVirtualDisplay() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getRealMetrics(metrics)

        val width = metrics.widthPixels / scaleFactor
        val height = metrics.heightPixels / scaleFactor

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "AlinGoCapture", width, height, metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )
    }

    private fun processImage(bitmap: Bitmap) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { visionText ->
                val textBlocks = visionText.textBlocks.filter { it.boundingBox != null && it.text.isNotBlank() }
                if (textBlocks.isEmpty()) { isProcessing = false; return@addOnSuccessListener }
                translateBlocks(textBlocks)
            }
            .addOnFailureListener { isProcessing = false }
    }

    private fun translateBlocks(blocks: List<com.google.mlkit.vision.text.Text.TextBlock>) {
        val targetCode = SettingsManager.getTargetLang(this)
        val mlTarget = toMlKitCode(targetCode) ?: TranslateLanguage.URDU

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(mlTarget)
            .build()
        val translator = Translation.getClient(options)

        translator.downloadModelIfNeeded().addOnSuccessListener {
            val results = mutableListOf<TranslationOverlay>()
            var completed = 0
            val total = blocks.size
            blocks.forEach { block ->
                val rect = block.boundingBox!!
                translator.translate(block.text)
                    .addOnSuccessListener { translated ->
                        results.add(TranslationOverlay(translated, rect.left * scaleFactor, rect.top * scaleFactor, rect.width() * scaleFactor, rect.height() * scaleFactor))
                        completed++
                        if (completed == total) {
                            overlayManager?.showTranslationBlocks(results, targetCode)
                            isProcessing = false
                        }
                    }
                    .addOnFailureListener {
                        completed++
                        if (completed == total) {
                            overlayManager?.showTranslationBlocks(results, targetCode)
                            isProcessing = false
                        }
                    }
            }
        }.addOnFailureListener { isProcessing = false }
    }

    private fun toMlKitCode(langCode: String): String? {
        return when (langCode) {
            "en" -> TranslateLanguage.ENGLISH; "ur" -> TranslateLanguage.URDU
            "hi" -> TranslateLanguage.HINDI; "bn" -> TranslateLanguage.BENGALI
            "pa" -> "pa"; "gu" -> "gu"; "ta" -> "ta"; "te" -> "te"
            "kn" -> "kn"; "ml" -> "ml"; "mr" -> "mr"; "ne" -> "ne"; "si" -> "si"
            else -> null
        }
    }

    private fun startForegroundService() {
        val channel = NotificationChannel("AlinGoChannel", "AlinGo Translate", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        val notification = Notification.Builder(this, "AlinGoChannel")
            .setContentTitle("AlinGo Translate").setContentText("Tap floating logo to translate")
            .setSmallIcon(android.R.drawable.ic_menu_camera).setOngoing(true).build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        virtualDisplay?.release()
        mediaProjection?.stop()
        overlayManager?.removeOverlays()
    }
}
