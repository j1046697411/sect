# lko-ecs

**Module:** lko-ecs

## Description

高性能 Entity-Component-System (ECS) 游戏引擎核心框架，支持多平台（JVM、Android、JS、Wasm）。

## Responsibilities

- 实体（Entity）管理
- 组件（Component）存储与查询
- 族（Family）过滤与匹配
- 观察者（Observer）事件系统
- 关系（Relation）父子/实例系统
- 查询（Query）DSL
- 渲染管线（Pipeline）调度

## Public API surface

### World
- `World` - ECS 世界容器
- `WorldSetup` - 世界构建配置

### Entity
- `Entity` - 实体
- `EntityService` - 实体服务
- `EntityCreateContext` - 实体创建上下文
- `EntityUpdateContext` - 实体更新上下文

### Component
- `ComponentService` - 组件服务
- `ComponentAddon` - 组件插件
- `Components` - 组件 DSL

### Family
- `Family` - 族
- `FamilyBuilder` - 族构建器
- `FamilyService` - 族服务

### Query
- `Query<E : EntityQueryContext>` - 查询
- `EntityQueryContext` - 查询上下文
- `QueryService` - 查询服务
- `QueryStream` - 查询流

### Observer
- `Observer` - 观察者
- `ObserverBuilder` - 观察者构建器
- `ObserverContext` - 观察者上下文
- `ObserveService` - 观察者服务

### Relation
- `Relation` - 关系
- `RelationService` - 关系服务
- `EntityType` - 实体类型

### Addon
- `Addon` - 插件
- `WorldSetup` - 世界配置
- `Phase` - 执行阶段

### DSL
- `world { }` - 创建世界
- `entity { }` - 创建实体
- `editor(entity) { }` - 编辑实体
- `query { }` - 查询

## Dependencies

- `lko-core` - 核心工具库
- `lko-di` - 依赖注入框架
- Kodein（第三方 DI 库）
- Kotlinx Atomics
- AndroidX Collection

## Testing approach

- 单元测试覆盖核心功能
- 集成测试验证系统交互
- 目标覆盖率 80% 以上

## Code style guidelines

- 遵循项目通用 Kotlin 编码规范
- 包名：`cn.jzl.ecs`
- 组件：名词（如 `Health`、`Position`）
- 标签：形容词+Tag（如 `ActiveTag`）
- 服务：功能+Service（如 `HealthService`）

## Migration/Compatibility

- 当前版本为初始版本，无迁移需求

## Contributing notes

- 新功能需添加对应的测试
- 公共 API 需添加 KDoc 文档
- 遵循 ECS 设计原则：组件存数据，标签标记状态，服务处理逻辑
