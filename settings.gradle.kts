pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
    id("de.fayard.refreshVersions") version "0.60.3"
}

rootProject.name = "EduzzAPI"