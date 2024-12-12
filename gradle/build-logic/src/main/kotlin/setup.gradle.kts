plugins {
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    id("app.cash.licensee")
}

licensee {
    allow("Apache-2.0")
}

dokka {
    val module = project.name
    dokkaSourceSets.configureEach {
        includes.from("README.md")
        reportUndocumented.set(true)
        val sourceSetName = name
        File("$module/src/$sourceSetName").takeIf { it.exists() }?.let {
            sourceLink {
                localDirectory.set(file("src/$sourceSetName/kotlin"))
                remoteUrl.set(uri("https://github.com/hfhbd/kotlinx-serialization-csv/tree/main/$module/src/$sourceSetName/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
        externalDocumentationLinks {
            register("kotlinx.serialization") {
                url("https://kotlinlang.org/api/kotlinx.serialization/")
            }
        }
    }
}
