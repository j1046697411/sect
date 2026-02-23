# 宗门修真录 - Agent 项目规范

## 铁律（切勿删除）

1. **所有测试用例发现的问题，都必须修复，不能跳过或者忽略**
2. **修改任何代码文档后，都必须同步提交 git 到本地，每次提交只提交自己修改的部分**
3. **在实现业务框架的时候发现核心框架的问题，必须立即修复，不能延迟到后续版本**
4. **发现任何文档或者代码中的问题，必须立即修复，不能延迟到后续版本**
5. **实现业务逻辑的时候，必须使用 TDD 模式，先编写测试用例，再实现功能**
6. **组件必须做到单一职责**
7. **当一个类或者文件行数超过 500 行时，必须重构拆分，做到高内聚，低耦合**
8. **所有修改都必须使用 git 提交，提交信息必须包含：做了什么、遇到了什么问题、如何解决的**

---

## Git 提交规范

### 提交信息格式
```
<类型>: <简短描述>

## 做了什么
- 具体修改内容1
- 具体修改内容2

## 遇到的问题
- 问题描述（如无可省略）

## 解决方案
- 如何解决的（如无可省略）
```

### 提交类型
| 类型 | 说明 |
|------|------|
| feat | 新功能 |
| fix | 修复 bug |
| refactor | 重构代码 |
| test | 添加或修改测试 |
| docs | 文档修改 |
| style | 代码格式调整 |
| chore | 构建/工具修改 |

### 示例
```
feat: 添加弟子修炼系统

## 做了什么
- 新增 CultivationService 服务类
- 添加 CultivationProgress 组件
- 实现修炼进度计算逻辑

## 遇到的问题
- 测试中实体组件未正确初始化

## 解决方案
- 在 @BeforeEach 中统一初始化测试数据
```

---

## 构建与测试命令

```bash
# 构建
./gradlew build                              # 全量构建
./gradlew :composeApp:run                    # 运行桌面版 Demo

# 测试 - 全部
./gradlew test                               # 运行所有测试

# 测试 - 指定模块
./gradlew :libs:lko-ecs:test                 # ECS 核心测试
./gradlew :libs:lko-core:test                # 基础库测试
./gradlew :business-modules:business-engine:test

# 测试 - 单个测试类
./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.WorldTest"

# 测试 - 单个测试方法
./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.WorldTest.testBasicEntityCreation"

# 覆盖率
./gradlew allCoverage                        # 生成覆盖率报告（核心逻辑 > 80%）

# 静态检查
./gradlew check                              # 运行所有检查
```

---

## 代码风格规范

### 语言与编码
- **语言**: Kotlin (100%)，所有注释和文档使用**中文**
- **编码**: UTF-8，换行符使用 LF (Unix 风格)

### 包名命名
```kotlin
// 格式: cn.jzl.{模块名}.{子模块}
package cn.jzl.ecs.component
package cn.jzl.sect.cultivation.services
```

### 类命名
| 类型 | 规则 | 示例 |
|------|------|------|
| 组件 | 名词 | `Position`, `Health`, `CultivationProgress` |
| 标签 | 形容词+Tag | `AliveTag`, `IdleTag`, `CultivatingTag` |
| 系统/服务 | 名词+System/Service | `CultivationSystem`, `DiscipleInfoService` |
| 测试类 | {类名}Test | `WorldTest`, `CultivationServiceTest` |

### 格式化
- **缩进**: 4 空格（禁止 Tab）
- **行宽**: 120 字符
- **括号**: K&R 风格（左括号不换行）
- **空行**: 方法之间 1 个空行，类之间 2 个空行

### 导入规范
```kotlin
// ✅ 正确：明确导入每个类，按字母顺序排列
import cn.jzl.ecs.World
import cn.jzl.ecs.addon.createAddon
import cn.jzl.ecs.component.componentId
import cn.jzl.ecs.entity.EntityRelationContext
import kotlin.test.Test
import kotlin.test.assertEquals

// ❌ 错误：禁止通配符导入
import cn.jzl.ecs.*
import kotlin.test.*

// ⚠️ 警惕：区分不同包的同名类
import cn.jzl.ecs.family.component    // ECS 族组件
import cn.jzl.ecs.relation.component  // 关系组件
```

### 类型规范
```kotlin
// 组件: 使用 data class，所有字段必须是值类型或不可变类型
data class Position(val x: Int, val y: Int)
data class CultivationProgress(
    val realm: Realm,
    val layer: Int,
    val cultivation: Long,
    val maxCultivation: Long
)

// 标签: 使用 sealed class 或 object
sealed class AliveTag
object IdleTag

// 值类型: 使用 value class 避免装箱
@JvmInline
value class Timer(val duration: Duration)

// 服务: 纯逻辑类，不持有状态（状态存储在组件中）
class CultivationService(override val world: World) : EntityRelationContext {
    // 通过 world 访问数据，不持有业务状态
}
```

### 错误处理
```kotlin
// ✅ 参数校验
check(index >= 0) { "索引不能为负数" }
require(list.isNotEmpty()) { "列表不能为空" }

// ✅ 空安全
val value: String? = map["key"]
val result = value ?: "default"

// ❌ 避免使用 !!
val unsafe = value!!

// ✅ 使用 checkNotNull 替代
val safe = checkNotNull(value) { "值不能为空" }
```

---

## 模块依赖规范

### 依赖引入方式
```kotlin
// build.gradle.kts 中使用 projects 引用
dependencies {
    implementation(projects.libs.lkoCore)
    implementation(projects.libs.lkoEcs)
    implementation(projects.businessModules.businessCore)
}
```

### 依赖层级
```
┌─────────────────────────────────────────────────────┐
│  应用层 (composeApp, business-engine)               │
├─────────────────────────────────────────────────────┤
│  业务层 (business-cultivation, business-disciples)  │
├─────────────────────────────────────────────────────┤
│  共享内核 (business-core)                           │
├─────────────────────────────────────────────────────┤
│  基础设施 (lko-ecs, lko-di, lko-core)               │
└─────────────────────────────────────────────────────┘
```

---

## 测试规范

### 测试风格 (BDD)
```kotlin
@Test
fun testCultivationGain() {
    // Given: 创建一个修炼者实体
    val entity = world.entity {
        it.addComponent(CultivationProgress(...))
        it.addComponent(Talent(...))
    }

    // When: 推进修炼时间
    val breakthroughs = service.update(1)

    // Then: 修为应该增加
    assertTrue(cultivation.cultivation > 0, "修为应该增加")
}
```

### 测试命名
```kotlin
// 方法命名: test{测试场景} 或 should{预期行为}
@Test
fun testBasicEntityCreation() { ... }

@Test
fun shouldIncreaseCultivationWhenCultivating() { ... }
```

### TDD 流程
1. **🔴 红**: 编写失败的测试 → `./gradlew test` → 确认失败
2. **🟢 绿**: 编写最小实现 → 确认通过
3. **🔵 重构**: 优化代码结构 → 保持通过

---

## 反模式 (Anti-Patterns)

| 类别 | 禁止行为 | 替代方案 |
|------|----------|----------|
| **ECS** | `addComponent(Tag)` | 严格区分组件和标签接口 |
| **ECS** | 在 `query {}.forEach` 中修改实体结构 | 收集变更后统一处理 |
| **Kotlin** | 隐式 `it` 参数嵌套 | 显式命名 `forEach { entity -> ... }` |
| **Git** | 提交失败的测试 | 修复代码或测试 |
| **代码** | 使用 `println` 调试 | 使用日志框架或测试断言 |
| **代码** | 临时 `TODO`、未使用导入 | 提交前清理 |

---

## 关键文件位置

| 任务 | 位置 |
|------|------|
| 定义组件/标签 | `business-modules/business-core/` |
| 实现业务逻辑 | `business-modules/business-{模块}/services/` |
| 世界初始化 | `business-modules/business-engine/SectWorld.kt` |
| ECS 核心优化 | `libs/lko-ecs/` (谨慎修改) |
| 性能优化 | `libs/lko-core/` |
| 依赖版本 | `gradle/libs.versions.toml` |

---

## 文档参考

- ECS 架构: `docs/technology/ecs/AGENT.md`
- 模块规范: `business-modules/AGENTS.md`
- 基础库: `libs/AGENTS.md`
