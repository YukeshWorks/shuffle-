plugins {
    id("com.android.application")
}

android {
    namespace = "com.yukeshworks.ringshift"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yukeshworks.ringshift"
        minSdk = 23
        targetSdk = 35
        versionCode = 3
        versionName = "1.1.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
