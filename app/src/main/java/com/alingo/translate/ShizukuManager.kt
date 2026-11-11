package com.alingo.translate

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

object ShizukuManager {
    private const val PERMISSION_REQUEST_CODE = 1001

    // چیک کریں کہ شیزوکو انسٹال اور چل رہا ہے یا نہیں
    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    // چیک کریں کہ ہمیں اجازت مل چکی ہے یا نہیں
    fun hasPermission(): Boolean {
        if (!isShizukuAvailable()) return false
        return try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    // اجازت طلب کریں
    fun requestPermission(onResult: (Boolean) -> Unit) {
        if (!isShizukuAvailable()) {
            onResult(false)
            return
        }

        if (hasPermission()) {
            onResult(true)
            return
        }

        // شیزوکو سے اجازت مانگیں
        Shizuku.addRequestPermissionResultListener(object : Shizuku.OnRequestPermissionResultListener {
            override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                if (requestCode == PERMISSION_REQUEST_CODE) {
                    onResult(grantResult == PackageManager.PERMISSION_GRANTED)
                }
            }
        })
        Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
    }
}
