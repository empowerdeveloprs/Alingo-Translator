package com.alingo.translate

data class Language(
    val code: String,           // ML Kit کا کوڈ (e.g., "ur", "hi", "bn")
    val nativeName: String,     // اصل رسم الخط (اردو، हिन्दी، বাংলা)
    val urduName: String,       // اردو میں نام (اردو، ہندی، بنگالی)
    val englishName: String,    // انگریزی نام (Urdu, Hindi)
    val isRtl: Boolean,         // دائیں سے بائیں (اردو، سندھی)
    val isSupportedOffline: Boolean // ML Kit آف لائن سپورٹ
)

object Languages {
    val ALL = listOf(
        // پاکستان
        Language("en", "English", "انگریزی", "English", false, true),
        Language("ur", "اردو", "اردو", "Urdu", true, true),
        
        // انڈیا
        Language("hi", "हिन्दी", "ہندی", "Hindi", false, true),
        Language("bn", "বাংলা", "بنگالی", "Bengali", false, true),
        Language("pa", "ਪੰਜਾਬੀ", "پنجابی", "Punjabi", false, true),
        Language("gu", "ગુજરાતી", "گجراتی", "Gujarati", false, true),
        Language("ta", "தமிழ்", "تامل", "Tamil", false, true),
        Language("te", "తెలుగు", "تیلگو", "Telugu", false, true),
        Language("kn", "ಕನ್ನಡ", "کنڑ", "Kannada", false, true),
        Language("ml", "മലയാളം", "ملیالم", "Malayalam", false, true),
        Language("mr", "मराठी", "مراٹھی", "Marathi", false, true),
        
        // بنگلہ دیش
        Language("bn", "বাংলা (বাংলাদেশ)", "بنگالی (بنگلہ دیش)", "Bengali (BD)", false, true),
        
        // نیپال، سری لنکا
        Language("ne", "नेपाली", "نیپالی", "Nepali", false, true),
        Language("si", "සිංහල", "سنہالا", "Sinhala", false, true),
        
        // علاقائی زبانیں (ML Kit میں نہیں، آن لائن یا آف لائن ماڈل کی ضرورت)
        Language("sd", "سنڌي", "سندھی", "Sindhi", true, false),
        Language("ps", "پښتو", "پشتو", "Pashto", true, false),
        Language("bal", "بلوچی", "بلوچی", "Balochi", true, false),
        Language("skr", "سرائیکی", "سرائیکی", "Saraiki", true, false),
        Language("ks", "کٲشُر", "کشمیری", "Kashmiri", true, false),
    )

    fun findByCode(code: String): Language? = ALL.find { it.code == code }
    
    fun supportedOffline(): List<Language> = ALL.filter { it.isSupportedOffline }
}
