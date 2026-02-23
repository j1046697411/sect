## TDD测试策略

> 创建时间: 2026-02-23
> 分类: conventions
> 状态: active
> 标签: #测试 #tdd #bdd

**问题/背景**:
项目要求核心逻辑覆盖率 > 80%，需要统一的测试策略。

**解决方案/内容**:

### TDD 流程
```
🔴 红 → 🟢 绿 → 🔵 重构
1. 编写失败的测试
2. 编写最小实现
3. 优化代码
```

### BDD 风格
```kotlin
@Test
fun `given damaged entity when heal then health increases`() {
    // Given: 受伤的实体
    // When: 治疗
    // Then: 生命值增加
}
```

### 覆盖率要求
| 模块 | 最低覆盖率 |
|------|-----------|
| lko-ecs | 95%+ |
| lko-core | 90%+ |
| business-* | 80%+ |

### 测试原则
- 一个测试只验证一件事
- 每个测试独立，不依赖执行顺序
- 命名清晰描述场景

**关联记忆**: 无
