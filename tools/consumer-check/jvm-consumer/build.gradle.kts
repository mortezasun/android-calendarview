plugins {
    kotlin("jvm") version "2.4.21"
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
    implementation(platform("com.github.mortezasun.android-calendarview:calendar-bom:2.4.0"))
    implementation("com.github.mortezasun.android-calendarview:calendar-chinese-native")
}
