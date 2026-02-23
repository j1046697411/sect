## ECS架构模式

> 创建时间: 2026-02-23
> 分类: conventions
> 状态: active
> 标签: #ecs #架构 #模式

**问题/背景**:
项目中使用 ECS 架构，需要统一的设计模式。

**解决方案/内容**:

### 六大核心模式

| 模式 | 用途 | 继承类 |
|------|------|--------|
| Factory | 创建实体 | EntityRelationContext |
| Service | 业务逻辑 | EntityRelationContext |
| Query | 筛选实体 | EntityQueryContext |
| Batch | 批量操作 | 无 |
| Observer | 事件监听 | 无 |
| Relation | 实体关系 | 无 |

### 关键约定
- Service 必须无状态，状态存入组件
- Query Context 使用 `by component<T>()` 委托
- 单属性组件使用 `@JvmInline value class`
- 标签使用 `sealed class`，不含数据

**关联记忆**: #solutions:ecs-query-性能优化
