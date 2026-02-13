@file:Suppress("UnstableApiUsage")

import java.nio.file.Files

rootProject.name = "sect"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/apache-snapshots")
        maven("https://s01.oss.sonatype.org")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://jitpack.io")
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/apache-snapshots")
        maven("https://s01.oss.sonatype.org")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://jitpack.io")
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":composeApp")
include(":androidApp")
includeProject("libs")
includeProject("benchmarks")
include(":business-modules:business-core")
include(":business-modules:business-disciples")
include(":business-modules:business-cultivation")
include(":business-modules:business-quest")
include(":business-modules:business-engine")
fun includeProject(path: String) = addPluginLibs(rootDir.toPath().resolve(path))
fun addPluginLibs(path: java.nio.file.Path) {
    if (!Files.isDirectory(path)) return
    val buildFile = path.resolve("build.gradle.kts")
    if (Files.exists(buildFile)) {
        println("addPluginLibs $path")
        val relativize = rootDir.toPath().relativize(path)
        include(relativize.joinToString(":", ":"))
        return
    }
    // 使用 Files.list 遍历目录（需要手动关闭资源）
    Files.list(path).use { stream ->
        stream.forEach { subPath ->
            if (Files.isDirectory(subPath)) {
                addPluginLibs(subPath)
            }
        }
    }
}
