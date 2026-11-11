package com.alingo.translate

/**
 * مختلف زبانوں کے متن کو رومن (Roman) میں بدلنے کا سادہ نظام۔
 * یہ صارف کو تلفظ پڑھنے میں مدد دیتا ہے۔
 */
object TransliterationHelper {

    // اردو حروف تہجی کی رومن میپنگ
    private val urduMap = mapOf(
        'ا' to "a", 'آ' to "aa", 'ب' to "b", 'پ' to "p", 'ت' to "t", 'ٹ' to "t",
        'ث' to "s", 'ج' to "j", 'چ' to "ch", 'ح' to "h", 'خ' to "kh", 'د' to "d",
        'ڈ' to "d", 'ذ' to "z", 'ر' to "r", 'ڑ' to "r", 'ز' to "z", 'ژ' to "zh",
        'س' to "s", 'ش' to "sh", 'ص' to "s", 'ض' to "z", 'ط' to "t", 'ظ' to "z",
        'ع' to "a", 'غ' to "gh", 'ف' to "f", 'ق' to "q", 'ک' to "k", 'گ' to "g",
        'ل' to "l", 'م' to "m", 'ن' to "n", 'ں' to "n", 'و' to "o", 'ہ' to "h",
        'ھ' to "h", 'ء' to "", 'ی' to "i", 'ے' to "e", 'ئ' to "i",
        'َ' to "a", 'ِ' to "i", 'ُ' to "u", 'ٰ' to "a", 'ّ' to "",
        '٠' to "0", '١' to "1", '٢' to "2", '٣' to "3", '٤' to "4",
        '٥' to "5", '٦' to "6", '٧' to "7", '٨' to "8", '٩' to "9",
    )

    // ہندی/دیوناگری حروف کی رومن میپنگ (سادہ)
    private val devanagariMap = mapOf(
        'अ' to "a", 'आ' to "aa", 'इ' to "i", 'ई' to "ee", 'उ' to "u", 'ऊ' to "oo",
        'ए' to "e", 'ऐ' to "ai", 'ओ' to "o", 'औ' to "au",
        'क' to "k", 'ख' to "kh", 'ग' to "g", 'घ' to "gh", 'च' to "ch",
        'छ' to "chh", 'ज' to "j", 'झ' to "jh", 'ट' to "t", 'ठ' to "th",
        'ड' to "d", 'ढ' to "dh", 'ण' to "n", 'त' to "t", 'थ' to "th",
        'द' to "d", 'ध' to "dh", 'न' to "n", 'प' to "p", 'फ' to "ph",
        'ब' to "b", 'भ' to "bh", 'म' to "m", 'य' to "y", 'र' to "r",
        'ल' to "l", 'व' to "v", 'श' to "sh", 'ष' to "sh", 'स' to "s",
        'ह' to "h", '्' to "", 'ं' to "n", 'ः' to "h",
        'ा' to "aa", 'ि' to "i", 'ी' to "ee", 'ु' to "u", 'ू' to "oo",
        'े' to "e", 'ै' to "ai", 'ो' to "o", 'ौ' to "au", 'ँ' to "n",
    )

    fun toRoman(text: String, langCode: String): String {
        return when (langCode) {
            "ur" -> transliterate(text, urduMap)
            "hi", "mr", "ne" -> transliterate(text, devanagariMap)
            else -> text // دوسری زبانوں کے لیے اصل متن واپس
        }
    }

    private fun transliterate(text: String, map: Map<Char, String>): String {
        val sb = StringBuilder()
        for (ch in text) {
            sb.append(map[ch] ?: ch)
        }
        return sb.toString().replace("  ", " ").trim()
    }

    /**
     * کسی بھی زبان کے ٹیکسٹ کا تلفظ (Pronunciation) دکھانے کا فنکشن۔
     */
    fun getPronunciation(word: String, langCode: String): String {
        return when (langCode) {
            "en" -> "IPA: /$word/ (See dictionary for exact)" // انگریزی کے لیے API استعمال کریں گے
            "ur" -> toRoman(word, "ur")
            "hi", "mr", "ne" -> toRoman(word, "hi")
            "bn" -> word // بنگالی کے لیے آگے بہتر کریں گے
            else -> toRoman(word, langCode)
        }
    }
}
