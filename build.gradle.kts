import io.gitlab.arturbosch.detekt.*
import org.jetbrains.dokka.gradle.*

plugins {
    kotlin("multiplatform") version "1.7.10" apply false
    kotlin("plugin.serialization") version "1.7.10" apply false
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.11.0"
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("org.jetbrains.dokka") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.5.1"
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
    id("app.cash.licensee") version "1.5.0" apply false
}

repositories {
    mavenCentral()
}

subprojects {
    plugins.apply("org.jetbrains.kotlin.multiplatform")
    plugins.apply("org.jetbrains.dokka")
    plugins.apply("app.cash.licensee")

    repositories {
        mavenCentral()
    }

    the<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>().apply {
        explicitApi()
        targets.all {
            compilations.all {
                kotlinOptions.allWarningsAsErrors = true
            }
        }
        sourceSets {
            all {
                languageSettings.progressiveMode = true
            }
        }
    }

    the<app.cash.licensee.LicenseeExtension>().apply {
        allow("Apache-2.0")
    }

    tasks.getByName<DokkaTaskPartial>("dokkaHtmlPartial") {
        val module = project.name
        dokkaSourceSets.configureEach {
            includes.from("$rootDir/README.md")
            reportUndocumented.set(true)
            val sourceSetName = name
            File("$module/src/$sourceSetName").takeIf { it.exists() }?.let {
                sourceLink {
                    localDirectory.set(file("src/$sourceSetName/kotlin"))
                    remoteUrl.set(uri("https://github.com/hfhbd/kotlinx-serialization-csv/tree/main/$module/src/$sourceSetName/kotlin").toURL())
                    remoteLineSuffix.set("#L")
                }
            }
            externalDocumentationLink("https://kotlin.github.io/kotlinx.serialization/")
        }
    }
}

tasks.dokkaHtmlMultiModule.configure {
    includes.from("README.md")
}

allprojects {
    plugins.apply("org.gradle.maven-publish")
    plugins.apply("org.gradle.signing")

    val emptyJar by tasks.creating(Jar::class) { }

    group = "app.softwork"

    publishing {
        publications.all {
            this as MavenPublication
            artifact(emptyJar) {
                classifier = "javadoc"
            }
            pom {
                name.set("app.softwork CSV and FLF kotlinx.serialization")
                description.set("A multiplatform Kotlin CSV and FLF kotlinx.serialization library")
                url.set("https://github.com/hfhbd/kotlinx-serialization-csv")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("hfhbd")
                        name.set("Philip Wedemann")
                        email.set("mybztg+mavencentral@icloud.com")
                    }
                }
                scm {
                    connection.set("scm:git://github.com/hfhbd/kotlinx-serialization-csv.git")
                    developerConnection.set("scm:git://github.com/hfhbd/kotlinx-serialization-csv.git")
                    url.set("https://github.com/hfhbd/kotlinx-serialization-csv")
                }
            }
        }
    }

    (System.getProperty("signing.privateKey") ?: System.getenv("SIGNING_PRIVATE_KEY"))?.let {
        String(java.util.Base64.getDecoder().decode(it)).trim()
    }?.let { key ->
        println("found key, config signing")
        signing {
            val signingPassword = System.getProperty("signing.password") ?: System.getenv("SIGNING_PASSWORD")
            useInMemoryPgpKeys(key, signingPassword)
            sign(publishing.publications)
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(System.getProperty("sonartype.apiKey") ?: System.getenv("SONARTYPE_APIKEY"))
            password.set(System.getProperty("sonartype.apiToken") ?: System.getenv("SONARTYPE_APITOKEN"))
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

detekt {
    source = files(rootProject.rootDir)
    parallel = true
    buildUponDefaultConfig = true
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.21.0")
}

tasks {
    fun SourceTask.config() {
        include("**/*.kt")
        exclude("**/*.kts")
        exclude("**/resources/**")
        exclude("**/generated/**")
        exclude("**/build/**")
    }
    withType<DetektCreateBaselineTask>().configureEach {
        config()
    }
    withType<Detekt>().configureEach {
        config()

        reports {
            sarif.required.set(true)
        }
    }
}

subprojects {
    tasks.koverVerify {
        rule {
            bound {
                minValue = 85
            }
        }
    }
}
