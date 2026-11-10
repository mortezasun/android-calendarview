// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// Single source of truth for the published coordinates. JitPack resolves multi-module
// projects as com.github.USER.REPO:MODULE:VERSION.
allprojects {
    group = "com.github.mortezasun.android-calendarview"
    version = "2.0.0"
}
