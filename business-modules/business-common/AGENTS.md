# business-common - 公共模块

## 模块定位
公共工具和服务模块，提供跨模块复用的通用功能。

## 核心职责
- 时间系统（游戏时间追踪与累加）
- 倒计时系统（实体倒计时和事件触发）
- 其他公共工具和服务

## 模块层级
```
┌─────────────────────────────────────────────────────────┐
│  垂直业务域层 (business-common)                          │
│  - 依赖 business-core（可选）                            │
│  - 可被所有模块依赖                                      │
└─────────────────────────────────────────────────────────┘
```

## 目录结构
```
business-common/src/commonMain/kotlin/cn/jzl/sect/common/
├── time/                  # 时间系统
│   └── Timer.kt           # 时间服务和组件
└── countdown/             # 倒计时系统
    └── Countdown.kt       # 倒计时服务和组件
```

## 关键 API

### 时间系统
| 组件/服务 | 用途 | 说明 |
|-----------|------|------|
| `Timer` | 计时器组件 | 存储游戏运行时间 |
| `TimeService` | 时间服务 | 获取当前游戏时间 |
| `timeAddon` | 时间模块 | 时间系统 Addon |

### 倒计时系统
| 组件/服务 | 用途 | 说明 |
|-----------|------|------|
| `Countdown` | 倒计时组件 | 存储倒计时数据 |
| `OnCountdownComplete` | 倒计时完成事件 | 倒计时结束时触发 |
| `CountdownService` | 倒计时服务 | 管理倒计时 |
| `countdownAddon` | 倒计时模块 | 倒计时系统 Addon |

## 使用方式

### 时间系统
```kotlin
// 1. 安装 Addon
world.install(timeAddon)

// 2. 获取服务
val timeService by world.di.instance<TimeService>()

// 3. 获取当前游戏时间
val currentTime = timeService.getCurrentGameTime()

// 4. 时间服务会自动更新（实现 Updatable 接口）
// 每帧累加 deltaTime
```

### 倒计时系统
```kotlin
// 1. 安装 Addon（会自动安装 timeAddon）
world.install(countdownAddon)

// 2. 获取服务
val countdownService by world.di.instance<CountdownService>()

// 3. 设置倒计时
countdownService.countdown(entity, 5.seconds)

// 4. 监听倒计时完成事件
entity.observe<OnCountdownComplete>().exec {
    println("倒计时完成！")
    // 处理倒计时完成逻辑
}
```

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
    // business-common 可独立使用，不强制依赖 business-core
    implementation(projects.libs.lkoEcs)
    implementation(projects.libs.lkoDi)
}
```

## AI 开发指引

### 开发原则
- **独立性**: 公共模块应尽量独立，减少依赖
- **复用性**: 提供的功能应具有通用性
- **轻量化**: 避免引入过重的功能

### 时间系统特点
- 使用 `value class` 避免对象分配
- 自动创建全局计时器实体
- 实现 `Updatable` 接口自动更新

### 倒计时系统特点
- 基于 ECS 的倒计时组件
- 支持观察者模式监听完成事件
- 自动清理完成的倒计时

### 添加新功能检查清单
- [ ] 该功能是否具有通用性？
- [ ] 是否需要最小化依赖？
- [ ] 是否轻量且高效？
- [ ] 测试覆盖率是否达标？
- [ ] 是否遵循 TDD 开发模式？

## 禁止事项
- ❌ 禁止添加业务特定的功能
- ❌ 禁止引入重依赖
- ❌ 禁止在公共模块中包含业务逻辑

## 测试要求
- 时间服务测试
- 倒计时服务测试
- 事件触发测试
- 边界条件测试
