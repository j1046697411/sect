# business-engine - 游戏引擎

## 模块定位
引擎与调度相关的跨域协作组件，是游戏的核心入口和系统协调者。

## 核心职责
- 提供跨域协作的调度与引擎组件
- 负责游戏世界的初始化与主循环管理
- 协调各业务 Addon 的安装和初始化
- 管理游戏状态和循环

## 模块层级
```
┌─────────────────────────────────────────────────────────┐
│  应用层 (business-engine)                                │
│  - 依赖所有其他业务模块                                   │
│  - 负责组装和协调各系统                                   │
│  - 是唯一可以依赖所有模块的地方                           │
└─────────────────────────────────────────────────────────┘
```

## 目录结构
```
business-engine/src/commonMain/kotlin/cn/jzl/sect/engine/
├── SectAddon.kt           # 宗门组件集中注册
├── SectWorld.kt           # 游戏世界入口
├── DemoWorld.kt           # 演示世界
├── WorldProvider.kt       # 世界提供者
├── GameLoop.kt            # 游戏主循环
├── state/                 # 游戏状态
│   └── GameState.kt       # 游戏状态定义
├── service/               # 服务
│   └── WorldQueryService.kt  # 世界查询服务
├── systems/               # 系统实现
│   ├── TimeSystem.kt      # 时间系统
│   └── MovementSystem.kt  # 移动系统（Demo）
└── demo/                  # 演示程序
    └── CultivationDemo.kt # 修炼演示
```

## 关键 API

### 核心类
| 类 | 用途 | 说明 |
|-----|------|------|
| `SectWorld` | 游戏世界入口 | 世界创建和初始化 |
| `SectAddon` | 组件集中注册 | 统一注册所有组件 |
| `GameLoop` | 游戏主循环 | 帧更新管理 |
| `GameState` | 游戏状态 | 运行、暂停等状态 |

### 系统管理
| 系统 | 用途 | 执行时机 |
|------|------|----------|
| `TimeSystem` | 游戏时间推进 | 每帧 |
| `MovementSystem` | 实体移动 | 每帧（Demo） |

## 使用方式

### 1. 初始化游戏世界
```kotlin
// 创建世界
val world = SectWorld.initialize()

// 或使用演示世界
val demoWorld = DemoWorld.create()
```

### 2. 游戏主循环
```kotlin
// 创建游戏循环
val gameLoop = GameLoop(world)

// 启动循环
gameLoop.start()

// 或手动更新
world.update(deltaTime)
```

### 3. 查询世界状态
```kotlin
val queryService by world.di.instance<WorldQueryService>()
val entities = queryService.queryEntities(filter)
```

### 4. SectAddon 组件注册
```kotlin
// 在 SectAddon 中集中注册所有需要的组件
object SectAddon {
    val addon = createAddon<Unit>("sect") {
        components {
            world.componentId<Sect>()
            world.componentId<Realm>()
            // ... 其他组件
        }
    }
}
```

## 初始化流程

```
SectWorld.initialize()
  │
  ├── 1. 创建 World 实例
  │       world { install(SectAddon.addon) }
  │
  ├── 2. 安装各业务 Addon
  │       install(cultivationAddon)
  │       install(disciplesAddon)
  │       install(combatAddon)
  │       ...
  │
  ├── 3. 初始化系统
  │       TimeSystem(world)
  │       MovementSystem(world)
  │       ...
  │
  └── 4. 创建初始实体
          createInitialEntities()
```

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
    // 依赖所有业务模块
    implementation(projects.businessModules.businessCore)
    implementation(projects.businessModules.businessCultivation)
    implementation(projects.businessModules.businessDisciples)
    implementation(projects.businessModules.businessResource)
    implementation(projects.businessModules.businessFacility)
    implementation(projects.businessModules.businessCombat)
    implementation(projects.businessModules.businessSkill)
    implementation(projects.businessModules.businessQuest)
    implementation(projects.businessModules.businessAiGoap)
    implementation(projects.businessModules.businessBuilding)
    implementation(projects.businessModules.businessCommon)
    
    implementation(projects.libs.lkoEcs)
    implementation(projects.libs.lkoDi)
}
```

## AI 开发指引

### 开发原则
- **调度规范**: 确保 System 的执行顺序符合业务逻辑
- **世界组装**: 负责协调各业务 Addon 的安装
- **单一入口**: 所有初始化逻辑应通过统一入口管理

### 系统执行顺序
```kotlin
// 推荐的系统执行顺序
1. TimeSystem       // 时间推进
2. ResourceSystem   // 资源更新
3. CultivationSystem // 修炼更新
4. BehaviorSystem   // 行为决策
5. CombatSystem     // 战斗处理
6. QuestSystem      // 任务处理
7. FacilitySystem   // 设施更新
```

### 添加新系统检查清单
- [ ] 确定系统执行顺序
- [ ] 在 SectWorld 中注册系统
- [ ] 添加系统初始化逻辑
- [ ] 更新文档说明

## 禁止事项
- ❌ 禁止包含具体业务逻辑（业务逻辑应在对应模块中）
- ❌ 禁止在 System 中直接修改实体结构（应使用事件或命令队列）
- ❌ 禁止跳过初始化流程

## 测试要求
- 世界初始化测试
- 系统执行顺序测试
- 游戏循环测试
- 状态管理测试
