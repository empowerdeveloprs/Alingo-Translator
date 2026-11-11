plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.alingo.translate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.alingo.translate"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    
    // OCR (Text Recognition) - آف لائن
    implementation("com.google.mlkit:text-recognition:16.0.0")
    
    // Translation - آف لائن (Google کا فری انجن)
    implementation("com.google.mlkit:translate:17.0.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
}
