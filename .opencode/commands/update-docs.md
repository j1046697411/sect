# 更新文档

从单一事实来源同步文档：

1. 读取 build.gradle.kts 的任务定义
   * 生成 Gradle 任务参考表
   * 包含来自注释的描述

2. 读取 gradle.properties
   * 提取所有配置属性
   * 记录其用途和格式

3. 生成 docs/CONTRIB.md，内容包含：
   * 开发工作流程
   * 可用 Gradle 任务
   * 环境设置
   * 测试流程

4. 生成 docs/RUNBOOK.md，内容包含：
   * 构建和部署流程
   * 监控和日志
   * 常见问题及修复
   * 回滚流程

5. 更新 AGENTS.md：
   * 同步模块结构
   * 更新命令列表
   * 验证 ECS 规范

6. 识别过时的文档：
   * 查找 90 天以上未修改的文档
   * 列出以供人工审查

7. 显示差异摘要

单一事实来源：build.gradle.kts 和 gradle.properties

## 文档格式示例

### CONTRIB.md

```markdown
# 贡献指南

## 开发环境

- JDK 17+
- Gradle 8.x

## 常用命令

| 命令 | 说明 |
|------|------|
| ./gradlew build | 构建项目 |
| ./gradlew test | 运行测试 |
| ./gradlew :libs:lko-ecs:test | 运行模块测试 |

## 测试流程

1. 编写测试用例
2. 运行 ./gradlew test
3. 检查覆盖率 ./gradlew koverVerify
```

### RUNBOOK.md

```markdown
# 运维手册

## 构建流程

./gradlew build

## 部署流程

./gradlew :composeApp:run

## 常见问题

### 构建失败
- 检查 JDK 版本
- 清理缓存：./gradlew clean

### 测试失败
- 查看测试报告
- 运行单个测试定位问题
```
