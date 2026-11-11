package com.alingo.translate

import android.content.Context

object SettingsManager {
    private const val PREF_NAME = "alingo_settings"
    private const val KEY_SOURCE_LANG = "source_lang"
    private const val KEY_TARGET_LANG = "target_lang"
    private const val KEY_FONT_SCALE = "font_scale"
    private const val KEY_BG_OPACITY = "bg_opacity"

    fun getSourceLang(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_SOURCE_LANG, "en") ?: "en"
    }
    fun setSourceLang(context: Context, lang: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_SOURCE_LANG, lang).apply()
    }

    fun getTargetLang(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_TARGET_LANG, "ur") ?: "ur"
    }
    fun setTargetLang(context: Context, lang: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_TARGET_LANG, lang).apply()
    }

    // 1.0 = Normal, 1.5 = 50% bigger
    fun getFontScale(context: Context): Float {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getFloat(KEY_FONT_SCALE, 1.0f)
    }
    fun setFontScale(context: Context, scale: Float) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putFloat(KEY_FONT_SCALE, scale).apply()
    }

    // 0 to 255 (Alpha)
    fun getBgOpacity(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_BG_OPACITY, 230) // 230 = 90% opaque
    }
    fun setBgOpacity(context: Context, opacity: Int) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_BG_OPACITY, opacity).apply()
    }
}
