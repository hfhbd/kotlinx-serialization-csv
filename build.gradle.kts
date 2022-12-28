import io.gitlab.arturbosch.detekt.*

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    org.jetbrains.kotlinx.kover
    org.jetbrains.dokka
    io.gitlab.arturbosch.detekt
}

tasks.dokkaHtmlMultiModule.configure {
    includes.from("README.md")
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
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.22.0")
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
        autoCorrect = true

        reports {
            sarif.required.set(true)
        }
    }
}

koverMerged {
    enable()
    verify {
        onCheck.set(true)
        rule {
            bound {
                minValue = 85
            }
        }
    }
}
