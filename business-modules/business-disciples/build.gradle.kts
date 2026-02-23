import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget {
        compilerOptions {
            // 兼容当前目标 JVM 11
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
                implementation(libs.kodein.kaverit)
                implementation(project(":business-modules:business-core"))
                implementation(project(":business-modules:business-common"))
                implementation(project(":business-modules:business-cultivation"))
                implementation(project(":libs:lko-ecs"))
                implementation(projects.libs.lkoLog)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(project(":business-modules:business-engine"))
            }
        }
    }
}

android {
    namespace = "cn.jzl.sect.disciples"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release { isMinifyEnabled = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
