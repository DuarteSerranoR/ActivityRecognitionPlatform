plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = 32
    defaultConfig {
        applicationId = "pie.activityrecognition.platform.android"
        minSdk = 28
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")
    implementation("com.google.android.gms:play-services-location:18.0.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    //implementation("android.arch.lifecycle:extensions:1.1.1")
    //implementation("android.arch.lifecycle:viewmodel:1.1.1")
}