plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugins.kotlin.multiplatform.toDep())
    implementation(libs.plugins.kotlin.serialization.toDep())
    implementation(libs.plugins.licensee.toDep())
    implementation(libs.plugins.mavencentral.toDep())
    implementation(libs.plugins.dokka.toDep())
    implementation(libs.plugins.detekt.toDep())
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
