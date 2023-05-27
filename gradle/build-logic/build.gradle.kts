plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.kotlin.serialization)
    implementation(libs.licensee)
    implementation(libs.publish)
    implementation(libs.binary)
    implementation(libs.dokka)
    implementation(libs.kover)
    implementation(libs.detekt)
}

kotlin.jvmToolchain(11)
