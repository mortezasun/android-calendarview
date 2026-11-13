import org.gradle.api.publish.maven.MavenPublication

plugins {
    `java-platform`
    `maven-publish`
}

dependencies {
    constraints {
        api("${project.group}:library:${project.version}")
        api("${project.group}:calendar-gregorian:${project.version}")
        api("${project.group}:calendar-hijri:${project.version}")
        api("${project.group}:calendar-solarHijri:${project.version}")
        api("${project.group}:calendar-solarHijri-native:${project.version}")
        api("${project.group}:calendar-chinese-native:${project.version}")
        api("${project.group}:calendar-chinese-compose:${project.version}")
    }
}

publishing {
    publications {
        create<MavenPublication>("calendarBom") {
            from(components["javaPlatform"])
        }
    }
}
