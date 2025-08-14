plugins {
    id("mpp")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.serialization.core)
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
            "app.softwork.serialization.csv=${kotlinClasses.get().asFile.absolutePath}"
        )
    }
}
