plugins {
    kotlin("multiplatform")
}

kotlin {
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

    // publishing error wasmJs()
    // no kotlinx.datetime support wasmWasi()

    // tier 1
    linuxX64()
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()

    // tier 2
    linuxArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()
    iosArm64()

    // tier 3
    // no kotlinx.datetime support androidNativeArm32()
    // no kotlinx.datetime support androidNativeArm64()
    // no kotlinx.datetime support androidNativeX86()
    // no kotlinx.datetime support androidNativeX64()
    mingwX64()
    watchosDeviceArm64()
}
