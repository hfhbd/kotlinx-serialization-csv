plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugins.kotlin.multiplatform.toDep())
    implementation(libs.plugins.kotlin.serialization.toDep())
    implementation(libs.plugins.licensee.toDep())
    implementation(libs.plugins.publish.toDep())
    implementation(libs.plugins.binary.toDep())
    implementation(libs.plugins.dokka.toDep())
    implementation(libs.plugins.kover.toDep())
    implementation(libs.plugins.detekt.toDep())
}

kotlin.jvmToolchain(11)

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
