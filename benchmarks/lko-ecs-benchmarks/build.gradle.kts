import org.jetbrains.kotlin.allopen.gradle.AllOpenExtension

plugins {
    alias(libs.plugins.kotlinxBenchmark)
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.pluginAllopen)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

configure<AllOpenExtension> {
    annotation("org.openjdk.jmh.annotations.State")
}

dependencies {
    implementation(projects.libs.lkoEcs)
    implementation(libs.kotlinx.benchmark.runtime)
}

benchmark {
    configurations {
        named("main") {
            exclude("jvmTesting")
            warmups = 3
            iterations = 3
            iterationTime = 5
            iterationTimeUnit = "sec"
        }
    }
    targets {
        register("main") {

        }
    }
}