import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatformLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinxBenchmark) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.pluginAllopen) apply false
    alias(libs.plugins.kover) apply false
}

// Kover 代码覆盖率配置 - 应用到所有子项目
subprojects {
    // 为所有 Kotlin 子项目应用 Kover
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        apply(plugin = "org.jetbrains.kotlinx.kover")
        
        // 配置 Kover
        extensions.configure<kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension> {
            reports {
                filters {
                    excludes {
                        // 排除测试类
                        classes("*Test*")
                        // 排除生成的代码
                        classes("*\$*")
                    }
                }
            }
        }
    }
    
    plugins.withId("org.jetbrains.kotlin.jvm") {
        apply(plugin = "org.jetbrains.kotlinx.kover")
        
        // 配置 Kover
        extensions.configure<kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension> {
            reports {
                filters {
                    excludes {
                        classes("*Test*")
                    }
                }
            }
        }
    }
}

subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<KotlinMultiplatformExtension>("kotlin") {
            compilerOptions {
                freeCompilerArgs.add("-Xcontext-parameters")
            }
        }
    }
}

// 便捷任务：运行所有可用库的测试和覆盖率
// 注意：lko-ecs-serialization 模块有编译错误，暂时排除
tasks.register("allCoverage") {
    group = "verification"
    description = "Run all library tests and generate coverage reports"
    
    val libraryModules = listOf(
        ":libs:lko-core",
        ":libs:lko-di", 
        ":libs:lko-ecs"
        // TODO: 添加 lko-ecs-serialization（需先修复编译错误）
    )
    
    // 为每个模块添加测试和报告任务
    libraryModules.forEach { module ->
        dependsOn("$module:test")
        dependsOn("$module:koverHtmlReportJvm")
    }
    
    doLast {
        println("")
        println("================================")
        println("所有模块覆盖率报告已生成")
        println("================================")
        libraryModules.forEach { module ->
            val moduleName = module.substringAfterLast(":")
            println("")
            println("$moduleName:")
            println("  HTML: libs/$moduleName/build/reports/kover/htmlJvm/index.html")
            println("  XML:  libs/$moduleName/build/reports/kover/reportJvm.xml")
        }
        println("")
    }
}

// ECS 模块覆盖率（保持向后兼容）
tasks.register("ecsCoverage") {
    group = "verification"
    description = "Run ECS tests and generate coverage report"
    dependsOn(":libs:lko-ecs:test", ":libs:lko-ecs:koverHtmlReportJvm")
    doLast {
        println("")
        println("================================")
        println("ECS 覆盖率报告已生成")
        println("================================")
        println("HTML 报告: libs/lko-ecs/build/reports/kover/htmlJvm/index.html")
        println("XML 报告: libs/lko-ecs/build/reports/kover/reportJvm.xml")
        println("")
    }
}
