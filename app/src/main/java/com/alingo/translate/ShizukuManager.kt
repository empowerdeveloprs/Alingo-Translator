package com.alingo.translate

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.ByteArrayOutputStream
import java.io.InputStream

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

        Shizuku.addRequestPermissionResultListener(object : Shizuku.OnRequestPermissionResultListener {
            override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                if (requestCode == PERMISSION_REQUEST_CODE) {
                    onResult(grantResult == PackageManager.PERMISSION_GRANTED)
                }
            }
        })
        Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
    }

    /**
     * شیزوکو کے ذریعے براہ راست اسکرین شاٹ لیں (بغیر کسی پاپ اپ کے)
     */
    fun takeScreenshot(): Bitmap? {
        if (!isShizukuAvailable() || !hasPermission()) return null
        
        var process: ShizukuRemoteProcess? = null
        try {
            // سسٹم کی screencap کمانڈ چلائیں
            process = Shizuku.newProcess(arrayOf("screencap", "-p"), null, null)
            val inputStream: InputStream = process.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            process.waitFor()
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            process?.destroy()
        }
    }
}
