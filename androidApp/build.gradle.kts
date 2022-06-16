plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    signingConfigs {
        create("release") {
            storeFile =
                file("C:\\Users\\rapos\\OneDrive\\Desktop\\IT\\Dev\\Workspace\\ActivityRecognitionPlatform\\androidApp\\release\\output-metadata.json")
        }
    }
    compileSdk = 32
    defaultConfig {
        applicationId = "ActivityRecognitionPlatform"
        minSdk = 28
        targetSdk = 32
        versionCode = 10
        versionName = "Alpha-0.4"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-location:20.0.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    //implementation("android.arch.lifecycle:extensions:1.1.1")
    //implementation("android.arch.lifecycle:viewmodel:1.1.1")
}