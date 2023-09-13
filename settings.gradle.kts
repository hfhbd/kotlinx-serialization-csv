pluginManagement {
    includeBuild("gradle/build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("MyRepos")
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
    id("com.gradle.enterprise") version "3.15"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        if (System.getenv("CI") != null) {
            publishAlways()
            tag("CI")
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

rootProject.name = "kotlinx-serialization-csv-flf"

include("kotlinx-serialization-csv")
include("kotlinx-serialization-flf")
