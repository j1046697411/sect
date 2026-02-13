# 后端结构 - ECS 框架核心

**最后更新**: 2026-02-14
**入口点**: libs/lko-ecs/src/commonMain/kotlin/cn/jzl/ecs/

## ECS 核心模块

### 1. Entity (实体管理)

| 文件 | 用途 | 导出 |
|------|------|------|
| Entity.kt | 实体接口 | Entity |
| EntityService.kt | 实体创建/销毁 | create(), destroy() |
| EntityStore.kt | 实体存储 | get/set entity data |
| EntityEditor.kt | 实体编辑器 | addComponent, addTag |

### 2. Component (组件系统)

| 文件 | 用途 | 导出 |
|------|------|------|
| ComponentService.kt | 组件管理 | componentId, register |
| ComponentStore.kt | 组件存储 | get/set component data |
| ComponentProvider.kt | 组件供应 | 组件实例化 |

### 3. Archetype (原型系统)

| 文件 | 用途 | 导出 |
|------|------|------|
| Archetype.kt | 原型定义 | 组件组合标识 |
| ArchetypeService.kt | 原型管理 | 创建/查找原型 |
| Table.kt | 组件表 | 连续内存存储 |

### 4. Query (查询系统)

| 文件 | 用途 | 导出 |
|------|------|------|
| Query.kt | 查询定义 | Query<T> |
| QueryService.kt | 查询服务 | createQuery() |
| EntityQueryContext.kt | 查询上下文 | 组件访问代理 |

### 5. Family (家族匹配)

| 文件 | 用途 | 导出 |
|------|------|------|
| Family.kt | 家族定义 | 组件集合匹配 |
| FamilyService.kt | 家族管理 | 家族注册/匹配 |

### 6. Relation (关系系统)

| 文件 | 用途 | 导出 |
|------|------|------|
| Relation.kt | 关系定义 | Parent, Child, Instance |
| RelationService.kt | 关系管理 | addRelation, getRelated |
| RelationProvider.kt | 关系供应 | 关系查询 |

### 7. Observer (观察系统)

| 文件 | 用途 | 导出 |
|------|------|------|
| Observer.kt | 观察器接口 | Observer |
| ObserveService.kt | 事件分发 | onEntityCreated, onComponentAdded |
| ObserverBuilder.kt | 观察器构建 | DSL 构建器 |

### 8. Addon (扩展系统)

| 文件 | 用途 | 导出 |
|------|------|------|
| Addon.kt | 扩展定义 | 安装扩展 |
| AddonInstaller.kt | 扩展安装 | 依赖注入配置 |
| WorldSetup.kt | 世界配置 | DSL 配置 |

## 依赖注入架构

```
World
├── ArchetypeService (Singleton)
├── EntityStore (Singleton)
├── EntityService (Singleton)
├── RelationService (Singleton)
├── RelationProvider (Singleton)
├── FamilyService (Singleton)
├── ComponentService (Singleton)
├── QueryService (Singleton)
├── Pipeline (Singleton)
└── ObserveService (Singleton)
```

## 数据流

```
创建实体请求
    ↓
EntityService.create()
    ↓
EntityStore.alloc() → 分配实体ID
    ↓
ArchetypeService.getOrCreate() → 查找/创建原型
    ↓
ComponentStore.set() → 存储组件数据
    ↓
返回 Entity
```

## 关键 DSL

### 世界创建

```kotlin
val world = world {
    // 安装扩展
    install(archetypeAddon)
    install(entityAddon)
    
    // 配置组件
    components {
        componentId<Position>()
        componentId<Health> { tag() }
    }
    
    // 启动任务
    onStartup {
        // 初始化逻辑
    }
}
```

### 实体操作

```kotlin
// 创建
val player = world.entity {
    it.addComponent(Position(0, 0))
    it.addComponent(Health(100, 100))
    it.addTag<PlayerTag>()
}

// 更新
player.editor {
    it.addComponent(health.copy(current = 50))
}

// 查询
world.query { PlayerContext(this) }.forEach { ctx ->
    processPlayer(ctx.health)
}
```

## 外部依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| lko-di | (local) | 依赖注入 |
| lko-core | (local) | 集合工具 |
| kodein | 8.x | 类型标记 |
| androidx.collection | 1.3.x | 整型列表 |
| kotlinx.atomic | 0.22.x | 原子操作 |

## 相关代码地图

- [整体架构](architecture.md)
- [依赖注入](backend.md#依赖注入架构)
- [数据模型](data.md)
