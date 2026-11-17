import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.jvm.tasks.Jar as JvmJar
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    `maven-publish`
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    linuxX64()
    linuxArm64()
    mingwX64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosArm64()
    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadlessNoSandbox()
                }
            }
        }
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                useKarma {
                    useChromeHeadlessNoSandbox()
                }
            }
        }
        nodejs()
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

// Keep the published file names equal to the names recorded in Gradle Module Metadata.
// The Kotlin 2.4 defaults use the metadata/target compilation names for the files, while
// Maven publishing normalizes them to the publication artifactIds. Consumers resolve the
// names in the .module file, so a mismatch makes the native variant unresolvable.
tasks.named<Jar>("allMetadataJar") {
    archiveBaseName.set(project.name)
    archiveAppendix.set("")
}
tasks.named<JvmJar>("sourcesJar") {
    archiveAppendix.set("")
}
tasks.named<Zip>("linuxX64Klib") {
    archiveBaseName.set("${project.name}-linuxx64")
}
tasks.named<Zip>("linuxArm64Klib") {
    archiveBaseName.set("${project.name}-linuxarm64")
}
tasks.named<Zip>("mingwX64Klib") {
    archiveBaseName.set("${project.name}-mingwx64")
}
tasks.named<Zip>("iosX64Klib") {
    archiveBaseName.set("${project.name}-iosx64")
}
tasks.named<Zip>("iosArm64Klib") {
    archiveBaseName.set("${project.name}-iosarm64")
}
tasks.named<Zip>("iosSimulatorArm64Klib") {
    archiveBaseName.set("${project.name}-iossimulatorarm64")
}
tasks.named<Zip>("macosArm64Klib") {
    archiveBaseName.set("${project.name}-macosarm64")
}
