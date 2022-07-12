plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()
    js(IR) {
        browser()
        nodejs()
    }

    linuxX64()
    linuxArm32Hfp()
    linuxArm64()

    macosX64()
    macosArm64()

    iosArm64()
    iosArm32()
    iosX64()
    iosSimulatorArm64()

    watchosX86()
    watchosX64()
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()

    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()

    mingwX64()
    mingwX86()

    sourceSets {
        val serialization = "1.3.3"
        commonMain {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:$serialization")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            }
        }
    }
}
