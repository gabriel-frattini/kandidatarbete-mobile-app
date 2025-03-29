plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0" apply true
}
android {
    namespace = "com.example.dat068_tentamina"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.dat068_tentamina"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }
    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.tracing.perfetto.handshake)
    implementation(libs.androidx.foundation.layout.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.okhttp)
    implementation ("androidx.compose.material:material-icons-extended:1.4.3") //Junyi background
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("androidx.activity:activity-compose:1.6.0")  // or the latest version
    implementation ("androidx.compose.ui:ui:1.4.0")  // or the latest version
    implementation ("androidx.compose.material3:material3:1.0.0")  // if using Material3
    implementation ("androidx.compose.runtime:runtime-livedata:1.4.0")
    implementation("androidx.compose.material:material:1.5.4") // NATTA LA TILL FÖR TOPBAR TABS
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.activity:activity-compose:1.6.0")  // or the latest version
    implementation("androidx.compose.ui:ui:1.4.0")  // or the latest version
    implementation("androidx.compose.material3:material3:1.0.0")  // if using Material3
    implementation("androidx.compose.runtime:runtime-livedata:1.4.0")
    implementation(libs.richeditor)
    implementation (libs.androidx.material.icons.extended)
}