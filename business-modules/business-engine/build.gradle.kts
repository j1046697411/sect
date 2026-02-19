import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    jvm()
    js {
        browser()
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":libs:lko-core"))
                api(project(":libs:lko-ecs"))
                api(project(":business-modules:business-core"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(project(":libs:lko-ecs"))
            }
        }
    }
}

android {
    namespace = "cn.jzl.sect.engine"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}

// JVM运行任务配置
tasks.register<JavaExec>("runDemo") {
    group = "application"
    description = "运行修炼系统Demo"
    mainClass.set("cn.jzl.sect.engine.demo.CultivationDemoKt")
    val jvmTarget = kotlin.targets["jvm"] as org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
    classpath = jvmTarget.compilations["main"].output.allOutputs.plus(
        jvmTarget.compilations["main"].runtimeDependencyFiles
    )
    standardInput = System.`in`
}
