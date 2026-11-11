package com.alingo.translate

import android.content.Context

object SettingsManager {
    private const val PREF_NAME = "alingo_settings"
    private const val KEY_SOURCE_LANG = "source_lang"
    private const val KEY_TARGET_LANG = "target_lang"
    private const val KEY_FONT_SCALE = "font_scale"
    private const val KEY_BG_OPACITY = "bg_opacity"
    private const val KEY_OCR_LANG = "ocr_lang"

    fun getSourceLang(context: Context): String = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_SOURCE_LANG, "auto") ?: "auto"
    fun setSourceLang(context: Context, lang: String) = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_SOURCE_LANG, lang).apply()

    fun getTargetLang(context: Context): String = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_TARGET_LANG, "ur") ?: "ur"
    fun setTargetLang(context: Context, lang: String) = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_TARGET_LANG, lang).apply()

    fun getFontScale(context: Context): Float = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getFloat(KEY_FONT_SCALE, 1.0f)
    fun setFontScale(context: Context, scale: Float) = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putFloat(KEY_FONT_SCALE, scale).apply()

    fun getBgOpacity(context: Context): Int = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_BG_OPACITY, 230)
    fun setBgOpacity(context: Context, opacity: Int) = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt(KEY_BG_OPACITY, opacity).apply()

    // OCR Language (Latin, Chinese, Japanese, Korean, Devanagari)
    fun getOcrLang(context: Context): String = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_OCR_LANG, "latin") ?: "latin"
    fun setOcrLang(context: Context, lang: String) = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_OCR_LANG, lang).apply()
}
