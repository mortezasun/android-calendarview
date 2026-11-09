import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

extensions.configure<LibraryExtension> {
    namespace = "com.develotter.calendarview.calendars.solarHijri"
    compileSdk = 37
    defaultConfig {
        minSdk = 27
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
dependencies {
    api(project(":library"))
    // Explicit because JalaliCalendar is part of this module's public API.
    api(libs.jalali.calendar.huri)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
