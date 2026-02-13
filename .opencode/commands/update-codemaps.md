# 更新代码地图

分析代码库结构并更新架构文档：

1. 扫描所有 Kotlin 源文件的导入、类定义和依赖关系

2. 以以下格式生成简洁的代码地图：
   * codemaps/architecture.md - 整体架构
   * codemaps/ecs-core.md - ECS 框架核心
   * codemaps/components.md - 组件定义
   * codemaps/services.md - 服务层
   * codemaps/data.md - 数据模型

3. 计算与之前版本的差异百分比

4. 如果变更 > 30%，则在更新前请求用户批准

5. 为每个代码地图添加新鲜度时间戳

6. 将报告保存到 .reports/codemap-diff.txt

使用 Kotlin 语法进行分析。专注于高层结构，而非实现细节。

## 代码地图格式

### architecture.md

```markdown
# 项目架构

**更新时间**: YYYY-MM-DD HH:mm

## 模块结构

| 模块 | 路径 | 说明 |
|------|------|------|
| composeApp | composeApp/ | 应用主模块 |
| lko-ecs | libs/lko-ecs/ | ECS 框架核心 |

## 依赖关系

composeApp -> lko-ecs -> lko-core
```

### ecs-core.md

```markdown
# ECS 框架核心

**更新时间**: YYYY-MM-DD HH:mm

## 核心类

| 类 | 路径 | 说明 |
|------|------|------|
| World | World.kt | ECS 世界 |
| Entity | Entity.kt | 实体 |
| Component | Component.kt | 组件接口 |

## 关键函数

- `world.entity { }` - 创建实体
- `world.query { }` - 查询实体
- `entity.editor { }` - 修改实体
```

### components.md

```markdown
# 组件定义

**更新时间**: YYYY-MM-DD HH:mm

## 组件列表

| 组件 | 类型 | 属性 | 说明 |
|------|------|------|------|
| Health | data class | current, max | 生命值 |
| Position | data class | x, y | 位置 |
| ActiveTag | sealed class | - | 活跃标签 |

## 组件分布

- composeApp: 15 个组件
- lko-ecs: 0 个组件（框架层）
```

### services.md

```markdown
# 服务层

**更新时间**: YYYY-MM-DD HH:mm

## 服务列表

| 服务 | 路径 | 依赖组件 | 说明 |
|------|------|------|------|
| HealthService | HealthService.kt | Health | 伤害处理 |
| MovementService | MovementService.kt | Position | 移动处理 |

## 服务依赖关系

HealthService -> Health, DeadTag
MovementService -> Position, Velocity
```
