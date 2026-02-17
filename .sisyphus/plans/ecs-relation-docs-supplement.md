# 执行：补充 ECS 文档 Relation 内容

## 执行命令
```bash
/start-work ecs-relation-docs-supplement
```

## 任务清单

### 任务 1: 修改 01-core-concepts.md
**文件**: docs/technology/ecs/01-core-concepts.md
**操作**: 将第 89-107 行的 Relation 章节（约18行）替换为详细内容（约100行）

**替换内容**:
- 4.1 定义关系类型（sealed class）
- 4.2 注册关系（componentId { it.tag() }）
- 4.3 添加关系（addRelation）
- 4.4 父子关系（childOf）
- 4.5 预制体实例化（instanceOf）
- 4.6 关系使用场景表
- 4.7 常见错误（3个错误示例）

### 任务 2: 修改 04-templates.md
**文件**: docs/technology/ecs/04-templates.md
**操作**: 在第 311 行后添加 T-011~T-014 四个模板

**添加内容**:
- T-011: 定义关系类型
- T-012: 创建带关系的实体
- T-013: 使用父子关系
- T-014: 预制体实例化

### 任务 3: 修改 02-patterns.md
**文件**: docs/technology/ecs/02-patterns.md
**操作**: 在第 337 行（Observer模式结束）后添加第 7 节 Relation 关系模式

**添加内容**:
- 7.1 所有权模式（OwnerBy关系）
- 7.2 层级模式（childOf父子关系）
- 7.3 模板实例化模式（instanceOf预制体）

## 代码示例来源

所有代码示例基于测试文件：
- `libs/lko-ecs/src/commonTest/kotlin/cn/jzl/ecs/WorldTest.kt`
- `testRelationManagement()` - 基本关系管理
- `testEntityHierarchy()` - 父子关系（childOf）
- `testInstanceOfPattern()` - 预制体实例化
- `testMultipleRelations()` - 多重关系
- `testComplexScenario()` - 综合场景

## 验收标准

- ✅ 01-core-concepts.md 的 Relation 章节从 18 行扩展到约 100 行
- ✅ 04-templates.md 新增 4 个模板（T-011~T-014）
- ✅ 02-patterns.md 新增第 7 节 Relation 关系模式
- ✅ 所有内容使用中文
- ✅ 每个示例包含 ✅ 正确和 ❌ 错误示例
- ✅ 基于真实测试代码编写

## 修改后验证

```bash
# 检查文件行数
wc -l docs/technology/ecs/01-core-concepts.md
wc -l docs/technology/ecs/04-templates.md
wc -l docs/technology/ecs/02-patterns.md

# 检查新增内容
grep -n "4.1 定义关系类型" docs/technology/ecs/01-core-concepts.md
grep -n "T-011" docs/technology/ecs/04-templates.md
grep -n "7. Relation 关系模式" docs/technology/ecs/02-patterns.md
```
