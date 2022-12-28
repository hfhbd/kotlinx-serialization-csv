import org.gradle.api.*
import org.gradle.api.initialization.*

class MyRepos : Plugin<Settings> {
    override fun apply(settings: Settings) {
        settings.dependencyResolutionManagement {
            repositories {
                mavenCentral()
            }
        }
    }
}
