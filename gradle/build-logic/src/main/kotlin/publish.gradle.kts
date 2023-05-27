plugins {
    `maven-publish`
    signing
}

val emptyJar by tasks.registering(Jar::class) { }

publishing {
    publications.configureEach {
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

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey?.let { String(java.util.Base64.getDecoder().decode(it)).trim() }, signingPassword)
    sign(publishing.publications)
}

// https://youtrack.jetbrains.com/issue/KT-46466
val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(signingTasks)
}
