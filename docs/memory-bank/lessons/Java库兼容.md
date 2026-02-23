## Java库兼容

> 创建时间: 2026-02-23
> 分类: lessons
> 状态: active
> 标签: #kotlin #陷阱

**问题/背景**:
在 Kotlin 多平台项目中使用 `java.util.PriorityQueue`，导致编译失败。

**错误做法**:
```kotlin
import java.util.PriorityQueue  // ❌ commonMain 中不可用
```

**正确做法**:
- 使用 Kotlin 多平台兼容的库（如 kotlinx-collections）
- 或自行实现平台无关版本

**教训**:
- Kotlin 多平台 `commonMain` 不能使用任何 `java.*` 包
- 写代码前先确认依赖是否支持多平台
- 常见陷阱：`java.util.Date`、`java.io.File`、`java.util.UUID`

**关联记忆**: 无
