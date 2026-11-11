package com.alingo.translate

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object HistoryManager {
    private const val PREF_NAME = "alingo_history"
    private const val KEY_HISTORY = "history_list"

    fun addToHistory(context: Context, sourceText: String, translatedText: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val historyJson = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        
        try {
            val array = JSONArray(historyJson)
            val newItem = JSONObject().apply {
                put("source", sourceText)
                put("translated", translatedText)
                put("timestamp", System.currentTimeMillis())
            }
            // نیا آئٹم سب سے اوپر شامل کریں
            val newArray = JSONArray()
            newArray.put(newItem)
            for (i in 0 until minOf(array.length(), 99)) { // صرف آخری 100 محفوظ رکھیں
                newArray.put(array.get(i))
            }
            prefs.edit().putString(KEY_HISTORY, newArray.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getHistory(context: Context): List<Pair<String, String>> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val historyJson = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        val list = mutableListOf<Pair<String, String>>()
        
        try {
            val array = JSONArray(historyJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(Pair(obj.getString("source"), obj.getString("translated")))
            }
        } catch (_: Exception) {}
        return list
    }

    fun clearHistory(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_HISTORY, "[]").apply()
    }
}
