# Kotlin Multiplatform 测试导入模式

**提取时间：** 2026-02-14
**适用场景：** 为 lko-di 等 Kotlin Multiplatform 模块编写测试时

## 问题描述

在 lko-di 模块中编写测试时，尝试使用 Kodein 库的 TypeToken 时遇到编译错误：

```kotlin
// 错误写法 - TypeToken 无法解析
val type = TypeToken.String  // 编译错误
val context1 = DIContext.ofType<String>()  // 编译错误

// 错误写法 - 无法推断类型
val ctx1 = DIContext(TypeToken<String>(), "value1")
```

## 解决方案

使用 Kodein 正确的 API：

```kotlin
// 使用正确的 TypeToken 创建方式
import org.kodein.type.TypeToken

// 对于简单的类型检查，直接使用 TypeToken.Any
val type = TypeToken.Any  // 可以工作

// 避免创建自定义 DIContext，使用默认的单例
val registry1 = scope.getRegistry(DIContext)  // 使用 DIContext 单例

// 测试 Scope 接口时，避免复杂的 DIContext 创建
val scope = NoScope()
val registry = scope.getRegistry(DIContext)  // 直接使用 DIContext
```

## 关键经验

1. **Kodein TypeToken API**：在测试中避免复杂的 TypeToken 创建，使用 `TypeToken.Any` 或直接使用 `DIContext` 单例

2. **DIContext 单例**：`DIContext` 有一个默认的单例实例 `DIContext`，可直接用于测试

3. **Scope 测试简化**：直接测试 Scope 接口行为，避免涉及复杂的 DI 容器初始化

## 使用时机

当为 lko-di 或其他依赖 Kodein 的模块编写测试时，应首先使用最简单的测试方式：
- 测试 Scope/ScopeRegistry 接口
- 使用 DIContext 单例
- 避免在测试中创建复杂的类型系统
