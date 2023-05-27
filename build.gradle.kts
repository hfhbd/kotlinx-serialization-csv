import io.gitlab.arturbosch.detekt.*

plugins {
    id("io.github.gradle-nexus.publish-plugin")
    id("org.jetbrains.kotlinx.kover")
    id("org.jetbrains.dokka")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

tasks.dokkaHtmlMultiModule {
    includes.from("README.md")
}

nexusPublishing {
    this.repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

detekt {
    source.from(files(rootProject.rootDir))
    parallel = true
    autoCorrect = true
    buildUponDefaultConfig = true
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${detekt.toolVersion}")
    kover(projects.kotlinxSerializationCsv)
    kover(projects.kotlinxSerializationFlf)
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

koverReport {
    defaults {
        verify {
            onCheck = true
            rule {
                bound {
                    minValue = 90
                }
            }
        }
    }
}
