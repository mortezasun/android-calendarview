plugins {
    kotlin("jvm")
    application
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("consumer.MainKt")
}

dependencies {
    implementation(platform("com.github.mortezasun.android-calendarview:calendar-bom:3.1.0-beta"))
    implementation("com.github.mortezasun.android-calendarview:calendar-chinese-native")
}
