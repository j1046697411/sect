# ECS组件导入冲突解决

**提取时间：** 2026-02-14
**适用场景：** 在ECS框架中同时使用family查询和relation功能时遇到`component()`函数冲突

## 问题描述
`component()` 函数在 `cn.jzl.ecs.family` 和 `cn.jzl.ecs.relation` 包中都存在，但用途不同：
- `family.component()` - 用于 FamilyBuilder 查询配置
- `relation.component()` - 用于 RelationProvider 创建组件关系

当同时导入两个包时，会出现编译错误：
```
error: unresolved reference. None of the following candidates is applicable...
fun <reified C> FamilyBuilder.component(): Unit
```

## 解决方案

### 方案1：明确指定导入（推荐）
```kotlin
// 在测试文件中，根据需要选择正确的导入
import cn.jzl.ecs.family.component        // 用于查询
import cn.jzl.ecs.relation.component      // 用于创建relation

// 如果两者都需要，使用别名
import cn.jzl.ecs.family.component as familyComponent
import cn.jzl.ecs.relation.component as relationComponent
```

### 方案2：按需导入
```kotlin
// 仅导入需要的组件
import cn.jzl.ecs.family.component      // 用于 EntityQueryContext
import cn.jzl.ecs.relation.component    // 用于 archetype 测试
```

### 方案3：完全限定名
```kotlin
// 不使用导入，直接使用完全限定名
val family = world.familyService.family {
    cn.jzl.ecs.family.component<Position>()
}

val relation = world.relations.run {
    cn.jzl.ecs.relation.component<Position>()
}
```

## 示例

### 场景：Archetype测试需要同时使用两者
```kotlin
package cn.jzl.ecs

import cn.jzl.ecs.family.component        // 用于 family 查询
import cn.jzl.ecs.relation.component      // 用于 relation 创建
import kotlin.test.*

class ArchetypeTest : EntityRelationContext {
    @Test
    fun testArchetypeComponentIndex() {
        val entity = world.entity {
            it.addComponent(ArchePosition(10, 20))
        }
        
        // 使用 relation.component() 创建 Relation 对象
        val relation = world.relations.component<ArchePosition>()
        
        // 使用 family.component() 在查询中
        val family = world.familyService.family { 
            component<ArchePosition>() 
        }
    }
}
```

## 使用时机

- 当测试文件同时涉及 archetype 查询和 relation 操作时
- 当编译器报错 `component()` 函数不明确时
- 当从错误导入切换到正确导入时

## 决策树

```
需要在什么地方使用 component()?
├── EntityQueryContext / FamilyBuilder
│   └── 使用: cn.jzl.ecs.family.component
├── RelationProvider / 创建 Relation 对象
│   └── 使用: cn.jzl.ecs.relation.component
└── 两者都需要
    └── 使用别名或完全限定名
```
