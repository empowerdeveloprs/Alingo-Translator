package com.alingo.translate

import android.content.Context

object SettingsManager {
    private const val PREF_NAME = "alingo_settings"
    private const val KEY_SOURCE_LANG = "source_lang"
    private const val KEY_TARGET_LANG = "target_lang"

    fun getSourceLang(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SOURCE_LANG, "en") ?: "en"
    }

    fun setSourceLang(context: Context, lang: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_SOURCE_LANG, lang).apply()
    }

    fun getTargetLang(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TARGET_LANG, "ur") ?: "ur"
    }

    fun setTargetLang(context: Context, lang: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_TARGET_LANG, lang).apply()
    }
}
