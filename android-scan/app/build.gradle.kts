plugins {
    id("com.android.application") version "8.7.3"
    id("org.jetbrains.kotlin.android") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.nichefinder.docstash"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nichefinder.docstash"
        minSdk = 26
        // targetSdk 35 (Android 15) meets Google Play's requirement for an app
        // to remain available to new users. Bump versionCode for the new
        // compliance release; must exceed the currently-live versionCode.
        targetSdk = 35
        versionCode = 2
        versionName = "0.1.1"
    }

    // Upload signing: supplied via environment so no secret ever enters the
    // repo. Set ANDROID_KEYSTORE (path), ANDROID_KEYSTORE_PASS to enable.
    val ksPath = System.getenv("ANDROID_KEYSTORE")
    val ksPass = System.getenv("ANDROID_KEYSTORE_PASS")
    if (ksPath != null && ksPass != null) {
        signingConfigs {
            create("release") {
                storeFile = file(ksPath)
                storePassword = ksPass
                keyAlias = "upload"
                keyPassword = ksPass
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            if (ksPath != null && ksPass != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("com.nichefinder:scan-core:0.1.0")

    // ML Kit document scanner: camera capture, edge detection, perspective correction and
    // multi-page capture are Google's own UI flow — dynamically delivered via Play services.
    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0")
    // ML Kit text recognition (Latin script, bundled model): on-device OCR, no Play services
    // download step needed — works the moment the app is installed.
    implementation("com.google.mlkit:text-recognition:16.0.1")
    // Play Billing: the one-time Pro unlock, never a subscription. Pinned to 8.0.0 (the first
    // release with the modern one-time-product PendingPurchasesParams API) rather than the
    // newest 9.x releases, which ship Kotlin metadata too new for this repo's Kotlin 2.0.21.
    implementation("com.android.billingclient:billing-ktx:8.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.exifinterface:exifinterface:1.4.2")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material-icons-extended")
}
