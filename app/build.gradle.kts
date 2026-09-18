import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.eventsnowcielo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.eventsnowcielo"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // Added from sample file for Cielo SDK credentials
        buildConfigField("String", "CREDENTIALS_CLIENT_ID", "\"xxxxxxxxxx\"")
        buildConfigField("String", "CREDENTIALS_ACCESS_TOKEN", "\"xxxxxxxxxx\"")
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true // Enabled to support BuildConfig fields required by SDK
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE*"
            excludes += "META-INF/NOTICE*"
            excludes += "META-INF/licenses/**"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

configurations.all {
    exclude(group = "com.android.support")
}

dependencies {
    // 1. Cielo Local AARs & JARs
    implementation(files("libs/order-manager-2.7.2.aar"))
    implementation(files("libs/event-tracker-1.0.1.aar"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // 2. Cielo LIO Transitive Dependencies
    implementation("com.journeyapps:zxing-android-embedded:3.5.0")
    implementation("com.mapbox.mapboxsdk:mapbox-android-sdk:6.7.1") {
        exclude(group = "com.android.support")
        exclude(module = "appcompat-v7")
        exclude(module = "support-v4")
    }
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    // 3. Datadog SDK (Required by event-tracker.aar)
    implementation("com.datadoghq:dd-sdk-android-logs:2.16.0")
    implementation("com.datadoghq:dd-sdk-android-trace:2.16.0")
    implementation("com.datadoghq:dd-sdk-android-rum:2.16.0")

    // 4. Koin Dependency Injection
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp)

    // Moshi & KSP
    implementation(libs.moshi)
    ksp(libs.moshi.codegen)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp.client)
    implementation(libs.okhttp.logger)

    // AndroidX & Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutine.android)
    implementation(libs.kotlinx.coroutine.core)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Tests
    testImplementation(libs.junit)
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation(libs.okhttp.mockwebserver.junit4)

    // Android Tests
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation("io.mockk:mockk-android:1.13.10")

    // Debug Tools
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// Compile time check for Koin Annotations
ksp {
    arg("KOIN_CONFIG_CHECK", "true")
    arg("KOIN_ANDROID_SDK", "true")
}