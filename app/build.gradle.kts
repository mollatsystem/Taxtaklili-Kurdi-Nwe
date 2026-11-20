plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "iq.kurd.keyboard"
    compileSdk = 35

    defaultConfig {
        applicationId = "iq.kurd.keyboard"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
}
