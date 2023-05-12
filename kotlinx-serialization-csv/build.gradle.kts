plugins {
    mpp
    setup
    publish
}

kotlin {
    sourceSets {
        val serialization = "1.5.1"
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
