plugins {
    id("mpp")
    id("setup")
    id("publish")
}

kotlin {
    compilerOptions.optIn.add("kotlin.time.ExperimentalTime")

    sourceSets {
        commonMain {
            dependencies {
                api(libs.serialization.core)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.serialization.json)
            }
        }
    }
}

tasks.compileJvm9MainJava {
    options.compilerArgumentProviders += object : CommandLineArgumentProvider {

        @InputFiles
        @PathSensitive(PathSensitivity.RELATIVE)
        val kotlinClasses = tasks.compileKotlinJvm.flatMap { it.destinationDirectory }

        override fun asArguments(): List<String> = listOf(
            "--patch-module",
            "app.softwork.serialization.flf=${kotlinClasses.get().asFile.absolutePath}"
        )
    }
}
