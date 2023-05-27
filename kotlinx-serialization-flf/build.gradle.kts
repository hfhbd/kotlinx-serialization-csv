plugins {
    id("mpp")
    id("setup")
    id("publish")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.serialization.core)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.datetime)
                implementation(libs.serialization.json)
            }
        }
    }
}
