apply(plugin = "org.jetbrains.kotlin.multiplatform")

extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>("kotlin") {
    jvmToolchain(8)
    explicitApi()
    targets.configureEach {
        compilations.configureEach {
            compilerOptions.configure {
                allWarningsAsErrors.set(true)
            }
        }
    }
    sourceSets.configureEach {
        languageSettings.progressiveMode = true
    }

    jvm()
    js(IR) {
        browser()
        nodejs()
    }

    // tier 1
    linuxX64()
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()

    // tier 2
    // no kotlinx.datetime support linuxArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()
    iosArm64()

    // tier 3
    // no kotlinx.serialization support androidNativeArm32()
    // no kotlinx.serialization support androidNativeArm64()
    // no kotlinx.serialization support androidNativeX86()
    // no kotlinx.serialization support androidNativeX64()
    mingwX64()
    // no kotlinx.serialization support watchosDeviceArm64()
}
