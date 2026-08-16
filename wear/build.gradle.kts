plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dev.seyone.quotatracker.wear"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.seyone.quotatracker"
        minSdk = 30
        targetSdk = 37
        versionCode = 6
        versionName = "1.5.1"
        vectorDrawables {
            useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // The deprecated kotlinOptions block has been removed from here.

    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// [NEW] Modern Kotlin compiler options DSL
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Play Services Wearable Data Layer API
    implementation(libs.play.services.wearable)

    // Wear Compose (Material 3 & Foundation)
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.navigation)

    // Horologist
    implementation(libs.horologist.compose.layout)

    // Glance Wear Tiles
    implementation(libs.androidx.glance.wear.tiles)

    // Wear Complications DataSource
    implementation(libs.androidx.wear.complications.datasource)

    // Coroutines & Serialization
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.gson)
}