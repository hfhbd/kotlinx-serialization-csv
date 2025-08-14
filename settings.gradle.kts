pluginManagement {
    includeBuild("gradle/build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("MyRepos")
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

rootProject.name = "kotlinx-serialization-csv-flf"

include("kotlinx-serialization-csv")
include("kotlinx-serialization-flf")
