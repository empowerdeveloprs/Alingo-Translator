package com.alingo.translate.agent

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.alingo.translate.MainActivity
import com.alingo.translate.R

class AgentService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // ایپ کھولنے کے لیے PendingIntent بنائیں
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "AgentChannel")
                .setContentTitle("AlinGo Agent Active")
                .setContentText("System control services are running.")
                .setSmallIcon(android.R.drawable.stat_sys_download_done) // ایک اچھا سا آئیکن
                .setContentIntent(pendingIntent)
                .setOngoing(true) // نوٹیفکیشن کو مستقل رکھیں (صارف اسے سوائپ کر کے ختم نہیں کر سکتا)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("AlinGo Agent Active")
                .setContentText("System control services are running.")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        }

        startForeground(1001, notification)

        // یہاں آپ کا ایجنٹ کا اصل کوڈ چلے گا
        // مثلاً: ConnectionManager.start(this)

        return START_STICKY // اگر سسٹم سروس کو مار دے تو یہ خود بخود دوبارہ شروع ہو جائے گی
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "AgentChannel",
                "AlinGo Agent Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps AlinGo Agent running for remote system control."
                setShowBadge(false) // لال ڈاٹ نہ دکھائیں
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
