# 验证循环技能

一个全面的代码会话验证系统。

## 何时使用

在以下情况下调用此技能：

* 完成功能或重大代码变更后
* 创建 PR 之前
* 当您希望确保质量门通过时
* 重构之后

## 验证阶段

### 阶段 1：构建验证

```bash
# 检查项目是否构建成功
./gradlew build 2>&1 | tail -20
```

如果构建失败，请停止并在继续之前修复。

### 阶段 2：类型检查

```bash
# Kotlin 项目类型检查在编译时进行
./gradlew compileKotlin 2>&1 | head -30
```

报告所有类型错误。在继续之前修复关键错误。

### 阶段 3：代码规范检查

```bash
# Kotlin 代码检查
./gradlew :<module>:lint 2>&1 | head -30

# Android 静态检查
./gradlew :composeApp:lint 2>&1 | head -30
```

### 阶段 4：测试套件

```bash
# 运行测试
./gradlew :<module>:test 2>&1 | tail -50

# 验证覆盖率阈值
./gradlew :<module>:koverVerify
# 目标：最低 80%
```

报告：

* 总测试数：X
* 通过：X
* 失败：X
* 覆盖率：X%

### 阶段 5：安全扫描

```bash
# 检查密钥泄露
grep -rn "sk-" --include="*.kt" . 2>/dev/null | head -10
grep -rn "api_key" --include="*.kt" . 2>/dev/null | head -10
grep -rn "password" --include="*.kt" . 2>/dev/null | head -10

# 检查 println 调试语句
grep -rn "println" --include="*.kt" src/ 2>/dev/null | head -10
```

### 阶段 6：差异审查

```bash
# 显示变更内容
git diff --stat
git diff HEAD~1 --name-only
```

审查每个更改的文件，检查：

* 意外更改
* 缺失的错误处理
* 潜在的边界情况

## 输出格式

运行所有阶段后，生成验证报告：

```
验证报告
==================

构建：     [通过/失败]
类型：     [通过/失败] (X 个错误)
检查：     [通过/失败] (X 个警告)
测试：     [通过/失败] (X/Y 通过，Z% 覆盖率)
安全：     [通过/失败] (X 个问题)
差异：     [X 个文件已更改]

总体：     [准备/未准备] 提交 PR

需要修复的问题：
1. ...
2. ...
```

## 持续模式

对于长时间会话，每 15 分钟或在重大更改后运行验证：

```markdown
设置一个心理检查点：
- 完成每个函数后
- 完成一个组件后
- 在移动到下一个任务之前

运行: /verify

```

## 与钩子的集成

此技能补充 PostToolUse 钩子，但提供更深入的验证。
钩子会立即捕获问题；此技能提供全面的审查。

## Kotlin/ECS 特定检查

### 组件不可变性检查

```bash
# 检查是否有直接修改组件属性的代码
grep -rn "\.current\s*=" --include="*.kt" src/ 2>/dev/null | head -10
```

### 遍历安全检查

```bash
# 检查遍历中是否有 editor 调用
grep -rn "query.*forEach.*editor" --include="*.kt" src/ 2>/dev/null | head -10
```

### 导入冲突检查

```bash
# 检查是否有同时导入 family.component 和 relation.component
grep -rn "import.*family.component" --include="*.kt" src/ 2>/dev/null | head -10
grep -rn "import.*relation.component" --include="*.kt" src/ 2>/dev/null | head -10
```
