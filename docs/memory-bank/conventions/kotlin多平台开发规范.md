## Kotlin多平台开发规范

> 创建时间: 2026-02-23
> 分类: conventions
> 状态: active
> 标签: #kotlin #多平台 #规范

**问题/背景**:
项目使用 Kotlin 多平台，需要遵守跨平台规范。

**解决方案/内容**:

### 禁止在 commonMain 使用 Java 库
```kotlin
// ❌ 错误：commonMain 中不可用
import java.util.PriorityQueue
import java.util.Date
import java.io.File
import java.util.UUID

// ✅ 正确：使用 Kotlin 多平台兼容库
// 或自行实现平台无关版本
```

### 常见替代方案
| Java 类 | 替代方案 |
|---------|----------|
| PriorityQueue | 自实现 MinHeap |
| Date | kotlinx-datetime |
| File | okio 或 expect/actual |
| UUID | 自实现或 kotlinx-uuid |

### 检查清单
- 写代码前确认依赖支持多平台
- commonMain 不能有任何 `java.*` 导入
- 平台特定代码放 `jvmMain`/`jsMain` 等

**关联记忆**: #lessons:kotlin多平台java库不兼容
