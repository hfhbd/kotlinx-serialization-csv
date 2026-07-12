@file:OptIn(ExperimentalAbiValidation::class)

import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("maven-publish")
    id("signing")
    id("io.github.hfhbd.mavencentral")
    id("org.jetbrains.dokka")
    id("app.cash.licensee")
}

kotlin {
    jvmToolchain(8)

    explicitApi()
    compilerOptions {
        allWarningsAsErrors = true
        progressiveMode = true
        extraWarnings = true

        optIn.add("kotlin.time.ExperimentalTime")
    }

    abiValidation {
        enabled = true
    }

    jvm {
        val main = compilations.getByName("main")
        val jvm9 = compilations.create("9Main") {
            associateWith(main)
        }
        tasks.named(artifactsTaskName, Jar::class) {
            from(jvm9.output.allOutputs) {
                into("META-INF/versions/9")
            }
            manifest {
                manifest.attributes("Multi-Release" to true)
            }
        }
    }

    js {
        nodejs()
    }
    wasmJs {
        nodejs()
    }
    wasmWasi {
        nodejs()
    }

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
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    mingwX64()
    watchosDeviceArm64()

    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.checkLegacyAbi)
}

tasks.named<JavaCompile>("compileJvm9MainJava") {
    javaCompiler = javaToolchains.compilerFor {}
    options.release = 9
}

plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin> {
    the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec>().downloadBaseUrl = null
}
plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsPlugin> {
    the<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsEnvSpec>().downloadBaseUrl = null
}

val emptyJar = tasks.register("emptyJar",Jar::class)

publishing {
    publications.withType(MavenPublication::class).configureEach {
        artifact(emptyJar) {
            classifier = "javadoc"
        }
        pom {
            name = "app.softwork CSV and FLF kotlinx.serialization"
            description = "A multiplatform Kotlin CSV and FLF kotlinx.serialization library"
            url = "https://github.com/hfhbd/kotlinx-serialization-csv"
            licenses {
                license {
                    name = "Apache-2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                }
            }
            developers {
                developer {
                    id = "hfhbd"
                    name = "Philip Wedemann"
                    email = "mybztg+mavencentral@icloud.com"
                }
            }
            scm {
                connection = "scm:git://github.com/hfhbd/kotlinx-serialization-csv.git"
                developerConnection = "scm:git://github.com/hfhbd/kotlinx-serialization-csv.git"
                url = "https://github.com/hfhbd/kotlinx-serialization-csv"
            }
        }
    }
}

signing {
    val signingKey = providers.gradleProperty("signingKey")
    if (signingKey.isPresent) {
        useInMemoryPgpKeys(signingKey.get(), providers.gradleProperty("signingPassword").get())
        sign(publishing.publications)
    }
}

// https://youtrack.jetbrains.com/issue/KT-46466
val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(signingTasks)
}

licensee {
    allow("Apache-2.0")
}

dokka {
    val module = project.name
    dokkaSourceSets.configureEach {
        includes.from("README.md")
        reportUndocumented = true
        val sourceSetName = name
        File("$module/src/$sourceSetName").takeIf { it.exists() }?.let {
            sourceLink {
                localDirectory = file("src/$sourceSetName/kotlin")
                remoteUrl = uri("https://github.com/hfhbd/kotlinx-serialization-csv/tree/main/$module/src/$sourceSetName/kotlin")
                remoteLineSuffix = "#L"
            }
        }
        externalDocumentationLinks {
            register("kotlinx.serialization") {
                url("https://kotlinlang.org/api/kotlinx.serialization/")
            }
        }
    }
}
