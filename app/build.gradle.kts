plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.minimalisttodolist.pleasebethelastrecyclerview"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.minimalisttodolist.pleasebethelastrecyclerview"
        minSdk = 26
        targetSdk = 34
        versionCode = 57
        versionName = "16.1.1"

        // Project > app > build > intermediates > merged_native_libs > release > mergeReleaseNativeLibs > out > lib > (compress everything in this folder)
        // zip -d Archive.zip "__MACOSX*"
        // https://www.youtube.com/watch?v=3ieKwhmQg7I&t=191s

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk.debugSymbolLevel = "FULL"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    ndkVersion = "27.0.12077973"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Sheets Compose Dialog
    implementation (libs.core)

    // CALENDAR
    implementation (libs.calendar)

    // Lottie Animation
    implementation (libs.lottie)

    // Extra Material Icons
    implementation (libs.androidx.material.icons.extended)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Splash APi
    implementation(libs.androidx.core.splashscreen)

    // Flow Row
    implementation (libs.androidx.foundation)

    // Downloadable Fonts
    implementation (libs.androidx.ui.text.google.fonts)

    // Firebase Analytics
    implementation (platform(libs.firebase.bom))
    implementation (libs.google.firebase.analytics)

    // In App Review
    implementation (libs.review)
    implementation (libs.review.ktx)
}