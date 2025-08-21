plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.basset"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.basset"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        androidResources {
            localeFilters += listOf("en", "ar")
            noCompress += listOf("tflite")
        }

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        mlModelBinding = true
    }
}

dependencies {
    // itext
    implementation(libs.itext7.core)

    // Color picker
    implementation(libs.compose.color.picker.android)
    implementation(libs.compose.color.picker)

    // LiteRT
    implementation(libs.litert)
    implementation(libs.litert.support)
    implementation(libs.litert.metadata)

    // Navigation 3
    implementation(libs.nav3.runtime)
    implementation(libs.nav3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)

    // Amplituda & compose-audiowaveform
    implementation(libs.amplituda)
    implementation(libs.compose.audiowaveform)

    implementation(libs.coil.compose)

    // Media3
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.ui.compose)
    implementation(libs.androidx.media3.exoplayer)

    // FFmpegKIT
    implementation(files("../app/libs/ffmpeg-kit-full-gpl.aar"))
    implementation(libs.smart.exception.java)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.bundles.koin)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}