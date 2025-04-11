plugins {
    kotlin("multiplatform")
}

kotlin {
    jvmToolchain(8)

    explicitApi()
    compilerOptions {
        allWarningsAsErrors.set(true)
        progressiveMode.set(true)
    }

    jvm()
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
}

val java9 by java.sourceSets.registering

tasks.named("jvmJar", Jar::class) {
    into("META-INF/versions/9") {
        from(java9.map { it.output })
    }

    manifest.attributes("Multi-Release" to true)
}

tasks.named<JavaCompile>("compileJava9Java") {
    javaCompiler.set(javaToolchains.compilerFor {})
    options.release.set(9)
}
