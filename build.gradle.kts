import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.allopen) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.dokka)
    `maven-publish`
}

// Compose Multiplatform 1.13.0-beta02 still leaves `outputDirectory` unconfigured for the resource
// copy task of KMP device-test compilations. The instrumentation tests do not use Compose resources,
// so the task is disabled instead of failing every `compileAndroidDeviceTest` run.
subprojects {
    tasks.matching { it.name == "copyAndroidDeviceTestComposeResourcesToAndroidAssets" }
        .configureEach { enabled = false }
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
            compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
}

// Clear plugin-level Ivy URL registration so Kotlin/JS and Kotlin/Wasm use the repositories
// declared centrally in settings.gradle.kts without violating FAIL_ON_PROJECT_REPOS.
allprojects {
    plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin> {
        the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec>().downloadBaseUrl.set(null as String?)
    }
    plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsPlugin> {
        the<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsEnvSpec>().downloadBaseUrl.set(null as String?)
    }
    plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
        the<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootEnvSpec>().downloadBaseUrl.set(null as String?)
    }
    plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnPlugin> {
        the<org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnRootEnvSpec>().downloadBaseUrl.set(null as String?)
    }
    plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.binaryen.BinaryenPlugin> {
        the<org.jetbrains.kotlin.gradle.targets.wasm.binaryen.BinaryenEnvSpec>().downloadBaseUrl.set(null as String?)
    }
}

// Dokka is applied to the published KMP modules only. The root task aggregates their
// documentation into build/dokka/html; the sample, BOM and benchmarks are not API docs.
val documentedModules = setOf(
    "library",
    "calendar-gregorian",
    "calendar-hijri",
    "calendar-solarHijri",
    "calendar-solarHijri-native",
    "calendar-chinese-native",
    "calendar-chinese-compose",
)
subprojects {
    if (name in documentedModules) {
        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            apply(plugin = "org.jetbrains.dokka")
        }
    }
}

dependencies {
    documentedModules.forEach { module -> add("dokka", project(":$module")) }
}

// Single source of truth for the published coordinates. JitPack resolves multi-module
// projects as com.github.USER.REPO:MODULE:VERSION.
allprojects {
    group = "com.github.mortezasun.android-calendarview"
    version = "3.1.0-beta"

    // Required by Maven Central for every published artifact. Signing, credentials and
    // namespace verification remain release-operator responsibilities.
    plugins.withId("maven-publish") {
        extensions.configure<PublishingExtension> {
            publications.withType<MavenPublication>().configureEach {
                pom {
                    name.set(project.name)
                    description.set("Calendar library module ${project.name} for android-calendarview.")
                    url.set("https://github.com/mortezasun/android-calendarview")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("mortezasun")
                            name.set("mortezasun")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/mortezasun/android-calendarview.git")
                        developerConnection.set("scm:git:ssh://github.com/mortezasun/android-calendarview.git")
                        url.set("https://github.com/mortezasun/android-calendarview")
                    }
                }
            }
        }
    }
}
