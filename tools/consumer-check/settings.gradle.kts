pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "calendarCandidate"
            url = uri(
                providers.gradleProperty("consumerRepo").getOrElse(
                    "${System.getProperty("user.home")}/.m2/repository",
                ),
            )
        }
    }
}

rootProject.name = "calendar-consumer-check"
include(":jvm-consumer")
include(":android-consumer")
