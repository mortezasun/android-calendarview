import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.jvm.tasks.Jar as JvmJar
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
