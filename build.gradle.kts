plugins {
    id("org.jetbrains.dokka")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

dependencies {
    for (s in subprojects) {
        dokka(s)
    }
}

dokka {
    dokkaPublications.configureEach {
        includes.from("README.md")
    }
}

detekt {
    source.from(fileTree(rootProject.rootDir) {
        include("**/*.kt")
        exclude("**/*.kts")
        exclude("**/resources/**")
        exclude("**/generated/**")
        exclude("**/build/**")
    })
    parallel = true
    autoCorrect = true
    buildUponDefaultConfig = true
    reports {
        sarif.required.set(true)
    }
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${detekt.toolVersion}")
}
