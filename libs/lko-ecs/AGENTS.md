# lko-ecs - AI 指引

## 模块定位
高性能 Entity-Component-System (ECS) 游戏引擎核心框架，支持多平台（JVM、Android、JS、Wasm）。

## 核心职责
- 实体（Entity）管理
- 组件（Component）存储与查询
- 族（Family）过滤与匹配
- 观察者（Observer）事件系统
- 关系（Relation）父子/实例系统
- 查询（Query） DSL
- 渲染管线（Pipeline）调度

## AI 开发指引
- **性能优先**: 核心循环中避免内存分配。
- **类型安全**: 严格区分组件和标签接口。
- **测试强制**: 必须保持 95%+ 的测试覆盖率。
- **代码风格**: 包名 `cn.jzl.ecs`，组件为名词，标签为形容词+Tag。
- **文档参考**: 使用 ECS 框架前请先阅读 [docs/technology/ecs/AGENT.md](../../docs/technology/ecs/AGENT.md)

## 关键 API
- `World`: ECS 世界容器
- `Entity`: 实体操作接口
- `Query`: 强大的查询系统
- `Observer`: 事件监听系统
- `Relation`: 实体间关系管理
