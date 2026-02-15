# 宗门修真录 - Agent 项目规范

## 关键说明：仅限中文策略（切勿删除此部分）

> **此部分绝不可删除或修改**

### 所有项目交流必须使用中文

| 上下文 | 语言要求 |
|---------|---------------------|
| **GitHub Issues** | 仅限中文 |
| **Pull Requests** | 仅限中文（标题、描述、评论） |
| **提交信息** | 仅限中文 |
| **代码注释** | 仅限中文 |
| **文档** | 仅限中文 |
| **AGENTS.md 文件** | 仅限中文 |

**如果你不习惯用中文写作，请使用翻译工具。**

---

## 开发流程：测试驱动开发 (TDD) - 强制

本项目强制使用 TDD（测试驱动开发）模式。所有新功能开发必须遵循 **红-绿-重构** 循环：

1. **🔴 红 (Red)**: 
   - 编写失败的测试用例。
   - 运行测试 (`./gradlew test` 或 `./gradlew :module:test`) 确认失败。
2. **🟢 绿 (Green)**: 
   - 编写最小实现代码，只为通过测试。
   - 运行测试，确认通过。
3. **🔵 重构 (Refactor)**: 
   - 优化代码结构，保持测试通过。
   - 确保代码符合项目规范。

**禁止**: 在没有测试的情况下编写业务代码。
**禁止**: 一次性编写大量代码后再补测试。

---

## 项目概述

Kotlin/Gradle 多模块 ECS 游戏项目。核心架构基于 Entity-Component-System (ECS) 模式。

## 模块结构

| 模块 | 路径 | 说明 |
|------|------|------|
| composeApp | `composeApp/` | 应用主模块 (桌面/Android/Web/WASM) |
| androidApp | `androidApp/` | Android 原生应用 |
| lko-core | `libs/lko-core/` | 核心工具库 |
| lko-di | `libs/lko-di/` | 依赖注入框架 |
| lko-ecs | `libs/lko-ecs/` | ECS 框架核心 |
| lko-ecs-serialization | `libs/lko-ecs-serialization/` | ECS 序列化 |
| business-core | `business-modules/business-core/` | 游戏核心业务 |
| business-disciples | `business-modules/business-disciples/` | 弟子系统 |
| business-cultivation | `business-modules/business-cultivation/` | 修炼系统 |
| business-quest | `business-modules/business-quest/` | 任务系统 |
| business-engine | `business-modules/business-engine/` | 游戏引擎 |
| lko-ecs-benchmarks | `benchmarks/lko-ecs-benchmarks/` | 性能基准测试 |

## 常用命令

### 构建
```bash
./gradlew build                                    # 全项目构建
./gradlew :composeApp:run                          # 运行 JVM Demo
./gradlew :composeApp:lint                         # Android 静态检查
./gradlew :composeApp:assembleDebug                # 构建 Debug APK
./gradlew clean                                    # 清理构建
```

### 测试
```bash
./gradlew test                                     # 运行所有测试
./gradlew :libs:lko-ecs:test                       # ECS 模块测试
./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.WorldTest"           # 测试类
./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.WorldTest.testName"  # 单测试
./gradlew :libs:lko-ecs:test --continuous          # 持续测试
./gradlew :business-modules:business-core:test    # 业务模块测试
```

### 代码覆盖率
```bash
./gradlew allCoverage                              # 所有模块
./gradlew ecsCoverage                              # ECS 模块
./gradlew :libs:lko-ecs:koverHtmlReportJvm
open libs/lko-ecs/build/reports/kover/htmlJvm/index.html
```

### 开发模式
```bash
./gradlew :composeApp:run                          # 运行桌面版
./gradlew :composeApp:hotReloadJvmDev              # 热重载开发
```

### 基准测试
```bash
./gradlew benchmark                                # 运行所有基准测试
./gradlew :benchmarks:lko-ecs-benchmarks:mainBenchmark  # ECS 基准测试
```

## 代码风格规范

### 命名约定
- **类/接口**: PascalCase (如 `World`, `EntityService`)
- **函数/属性**: camelCase (如 `getComponent`, `entityId`)
- **常量**: UPPER_SNAKE_CASE (如 `MAX_ENTITIES`)
- **组件 (Component)**: 名词 (如 `Health`, `Position`)
- **标签 (Tag)**: 形容词+Tag (如 `ActiveTag`, `DeadTag`)
- **系统/服务**: 功能+System/Service (如 `MovementSystem`)

### 格式化
- **缩进**: 4 个空格
- **行宽**: 120 字符
- **文件编码**: UTF-8

### 导入顺序
1. Kotlin/Java 标准库
2. 第三方库 (Android/Compose 等)
3. 项目内部模块 (`cn.jzl.*`)

```kotlin
import kotlin.jvm.JvmInline
import androidx.collection.MutableIntList
import cn.jzl.ecs.World
```

### 错误处理
- 优先使用 Kotlin 标准异常 (`IllegalArgumentException` 等)。
- 避免吞掉异常，必须记录或重新抛出。
- 在 ECS 系统中，避免在 `update` 循环中抛出未捕获异常导致游戏崩溃。

## ECS 核心规范

### 组件设计
```kotlin
data class Health(val current: Int, val max: Int)    // 多属性组件
@JvmInline value class Level(val value: Int)        // 单属性组件
sealed class ActiveTag                              // 标签
```

### 实体操作
```kotlin
// 创建
world.entity {
    it.addComponent(Health(100, 100))
    it.addTag<ActiveTag>()
}

// 更新（不可变，使用 copy）
entity.editor {
    it.addComponent(health.copy(current = 50))
}

// 查询
world.query { Context(this) }
```

### 查询上下文
```kotlin
class Context(world: World) : EntityQueryContext(world) {
    val name by component<EntityName>()
    val health by component<Health>()
    val equipment by component<Equipment?>()  // 可选组件
}

// 标签过滤
class ActiveContext(world: World) : EntityQueryContext(world) {
    override fun FamilyBuilder.configure() {
        component<ActiveTag>()
    }
}
```

## 常见陷阱

### 组件 vs 标签
| 场景 | 正确 | 错误 |
|------|------|------|
| 存储数据 | `addComponent(Health(100))` | `addTag<Health>()` |
| 状态标记 | `addTag<ActiveTag>()` | `addComponent(ActiveTag)` |

### 遍历中修改实体
```kotlin
// 错误
world.query { Context(this) }.forEach { 
    it.entity.editor { ... }  // 可能异常
}

// 正确
val entities = world.query { Context(this) }.map { it.entity }.toList()
entities.forEach { it.editor { ... } }
```

### Lambda 参数名冲突
```kotlin
// 错误：it 冲突
repeat(10) {
    world.entity { it.addComponent(Pos(it, it)) }
}

// 正确：明确命名
repeat(10) { index ->
    world.entity { it.addComponent(Pos(index, index * 2)) }
}
```

### 导入冲突
```kotlin
import cn.jzl.ecs.family.component      // 用于查询
import cn.jzl.ecs.relation.component    // 用于创建 Relation
```

## 测试规范

### 测试结构
```kotlin
class ComponentTest : EntityRelationContext {
    override lateinit var world: World
    
    @BeforeTest
    fun setup() {
        world = world { install(testAddon) }
    }
    
    private val testAddon = createAddon<Unit>("test") {
        components {
            world.componentId<TestPosition>()
            world.componentId<TestActiveTag> { it.tag() }
        }
    }
    
    @Test
    fun testComponentAddition() {
        val entity = world.entity {
            it.addComponent(TestPosition(10, 20))
        }
        assertEquals(TestPosition(10, 20), entity.getComponent<TestPosition>())
    }
}
```

### 测试数据类命名
使用模块前缀避免冲突：
- `CompPosition`, `CompHealth` (ComponentTest)
- `QueryPosition`, `QueryName` (QuerySystemTest)

## 参考文档

- ECS 详细文档: `docs/ecs-architecture.md`
- 技能文档: `.opencode/skills/learned/`
