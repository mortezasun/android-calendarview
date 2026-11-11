import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    `maven-publish`
}

// Single source of truth for the published coordinates. JitPack resolves multi-module
// projects as com.github.USER.REPO:MODULE:VERSION.
allprojects {
    group = "com.github.mortezasun.android-calendarview"
    version = "2.1.0"

    // Required by Maven Central for every published artifact. Signing, credentials and
    // namespace verification remain release-operator responsibilities.
    plugins.withId("maven-publish") {
        extensions.configure<PublishingExtension> {
            publications.withType<MavenPublication>().configureEach {
                pom {
                    name.set(project.name)
                    description.set("Android Compose calendar library module ${project.name}.")
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
