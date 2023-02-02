import org.gradle.api.*
import org.jetbrains.kotlin.gradle.dsl.*

class MPP : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("org.jetbrains.kotlin.multiplatform")
        project.extensions.configure<KotlinMultiplatformExtension>("kotlin") {
            jvmToolchain(8)
            explicitApi()
            targets.configureEach {
                compilations.configureEach {
                    kotlinOptions {
                        allWarningsAsErrors = true
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

            linuxX64()

            macosX64()
            macosArm64()

            iosArm64()
            iosArm32()
            iosX64()
            iosSimulatorArm64()

            watchosArm32()
            watchosArm64()
            watchosSimulatorArm64()

            tvosArm64()
            tvosX64()
            tvosSimulatorArm64()

            mingwX64()
        }
    }
}
