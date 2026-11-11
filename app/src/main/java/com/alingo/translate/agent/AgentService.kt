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
        // 1. نوٹیفکیشن آن رکھیں (تاکہ اینڈرائیڈ سروس کو مار نہ دے)
        startForeground(1001, buildNotification())

        // 2. ایجنٹ کو "سننے" (Listen) کے لیے تیار کریں
        // یہ تھریڈ صرف سگنل کا انتظار کرے گا، کوئی پروسیسنگ نہیں کرے گا
        if (serverSocket == null) {
            startListeningForCommands()
        }

        return START_STICKY
    }

    private fun startListeningForCommands() {
        thread(isDaemon = true) {
            try {
                // ایک پورٹ پر کنٹرولر کے سگنل کا انتظار کریں (مثلاً 8080)
                serverSocket = ServerSocket(8080)
                Log.d("AlinGoAgent", "Agent is silently listening on port 8080")

                while (isListening) {
                    // یہ لائن پروسیسر کو روکے رکھے گی (Sleep) جب تک کوئی کمانڈ نہ آئے
                    val client = serverSocket?.accept() ?: break 
                    
                    // اگر یہاں تک پہنچے تو اس کا مطلب ہے کمانڈ آئی ہے
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
        
        // یہاں آپ کی کمانڈز کا پروسیسنگ ہوگا
        when (command) {
            "PING" -> Log.d("AlinGoAgent", "PONG")
            "START_MIC" -> { /* MicHandler.start() */ }
            "STOP_MIC" -> { /* MicHandler.stop() */ }
            "GET_LOCATION" -> { /* LocationHandler.get() */ }
            // اور بھی کمانڈز...
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "AgentChannel")
                .setContentTitle("AlinGo Agent Ready")
                .setContentText("Waiting for controller commands...")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentIntent(pendingIntent)
                .setOngoing(true) // ہمیشہ آن رہے گا
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
            val serviceChannel = NotificationChannel(
                "AgentChannel",
                "AlinGo Agent",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        isListening = false
        try { serverSocket?.close() } catch (_: Exception) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
