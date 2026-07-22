plugins {
    id("com.android.application") version "8.7.3"
    id("org.jetbrains.kotlin.android") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

android {
    namespace = "com.nichefinder.caddie"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nichefinder.caddie"
        minSdk = 26
        // targetSdk 35 (Android 15) meets Google Play's requirement for an app
        // to remain available to new users. Bump versionCode for the new
        // compliance release; must exceed the currently-live versionCode.
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1"
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
            // Minification off for the first release: no runtime surprises
            // from R8 on serialization/Compose. Revisit with tested rules.
            isMinifyEnabled = false
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
    implementation("com.nichefinder:golf-core:0.1.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material-icons-extended")
}
