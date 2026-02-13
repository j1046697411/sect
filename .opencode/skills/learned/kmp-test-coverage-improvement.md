# Kotlin Multiplatform 项目测试覆盖率提升

**提取时间：** 2026-02-14
**适用场景：** 在 Kotlin Multiplatform 项目中提升测试覆盖率

## 问题描述

需要为 lko-ecs (ECS 游戏引擎框架) 模块补全测试用例，保证各包覆盖率在 80% 以上。

## 解决方案

### 1. 模块级 MODULE.md 规范
每个 libs 模块需要创建 MODULE.md 文件，包含：
- Module、Description、Responsibilities
- Public API surface、Dependencies
- Testing approach、Code style guidelines

### 2. ECS 测试基类模式
```kotlin
class ComponentServiceTest : EntityRelationContext {
    override lateinit var world: World
    
    @BeforeTest
    fun setup() {
        world = world { install(testAddon) }
    }
    
    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<Position>()
        }
    }
}
```

### 3. 数据类命名规范
使用模块前缀避免冲突：
- `CompPosition`, `CompHealth` (ComponentTest)
- `ObserverPosition`, `ObserverHealth` (ObserverTest)

### 4. 覆盖率目标
- 总体目标：80%+
- 核心域：尽量 100%

## 示例

新增测试文件：
- `GeneralComponentStoreTest.kt` - 组件存储测试
- `BucketedLongArrayTest.kt` - 数组工具测试
- `BatchEntityEditorPoolTest.kt` - 编辑器池测试
- `EntitiesTest.kt` - 实体集合测试

## 使用时机

- 新增 Kotlin Multiplatform 模块时
- 需要补全测试覆盖率时
- 为 ECS 框架添加新功能时

## 注意事项

- Kotlin Multiplatform 测试使用 `commonTest` 源码集
- 运行命令：`./gradlew :libs:lko-ecs:jvmTest`
- 覆盖率报告：`./gradlew :libs:lko-ecs:koverHtmlReportJvm`
