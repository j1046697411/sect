# updating-agents-md Skill 使用指南

## 快速开始

### 全量更新
```
用户: 更新所有 AGENTS.md 文件
```

### 指定目录更新
```
用户: 更新 libs 目录的 AGENTS.md
用户: 更新 business-modules 的 AGENTS.md
用户: 更新 docs 的 AGENTS.md
```

### 指定模块更新
```
用户: 更新 business-cultivation 的 AGENTS.md
用户: 更新 lko-ecs 的 AGENTS.md
```

## 执行流程

```
扫描阶段          模板匹配          内容填充          验证阶段          Git提交
    │                │                │                │                │
    ▼                ▼                ▼                ▼                ▼
查找AGENTS.md ──▶ 识别目录类型 ──▶ 填充模板变量 ──▶ 检查索引链接 ──▶ 按目录分组提交
扫描源代码        加载对应模板      AI智能填充        验证格式一致性
提取依赖关系
```

## 模板变量

| 变量 | 说明 | 来源 |
|------|------|------|
| `{{MODULE_NAME}}` | 模块名称 | 目录名 |
| `{{MODULE_DESCRIPTION}}` | 模块描述 | AI 分析 |
| `{{LAYER}}` | 层级 | 目录类型 |
| `{{DIRECTORY_STRUCTURE}}` | 目录树 | 自动扫描 |
| `{{COMPONENTS_TABLE}}` | 组件表格 | 扫描 components/ |
| `{{SERVICES_TABLE}}` | 服务表格 | 扫描 services/ |
| `{{KEY_APIS_TABLE}}` | API 表格 | 扫描所有类 |
| `{{DEPENDENCIES}}` | 依赖列表 | build.gradle.kts |
| `{{USAGE_EXAMPLE}}` | 使用示例 | AI 生成 |
| `{{DEVELOPMENT_PRINCIPLES}}` | 开发原则 | AI 分析 |

## 目录类型与模板

| 目录 | 模板 | 特殊处理 |
|------|------|----------|
| `business-modules/*` | module.md | 提取 Addon、Service、Component |
| `libs/*` | lib.md | 提取依赖关系、性能要求 |
| `docs/*` | docs.md | 文档索引 |
| 根目录 | root.md | 项目概览、构建命令 |

## 扫描命令参考

```bash
# 查找所有 AGENTS.md 文件
find . -name "AGENTS.md" -type f

# 扫描 Kotlin 源文件
glob "**/src/commonMain/**/*.kt"

# 提取类/接口定义
grep -E "^(class|interface|object|data class|sealed class|enum class)" *.kt

# 查看依赖
cat build.gradle.kts | grep -A 20 "dependencies"
```

## 验证检查

### 子模块索引验证
```bash
# 检查索引中的链接是否存在
grep -oE '\[.*\]\(\./.*AGENTS\.md\)' AGENTS.md | while read link; do
    path=$(echo "$link" | sed 's/.*(\(.*\))/\1/' | sed 's|^\./||')
    [ -f "$path" ] || echo "Missing: $path"
done
```

### 格式一致性检查
- [ ] 章节顺序符合模板
- [ ] 表格格式正确
- [ ] 代码块有语法高亮

## 注意事项

1. **保留现有重要内容** - 不要覆盖用户自定义的重要信息
2. **增量更新** - 只更新变化的部分，减少 diff 噪音
3. **分步提交** - 便于 review 和回滚
4. **验证后再提交** - 确保链接有效、格式正确

## 常见问题

### Q: 如何只更新特定模块？
A: 指定模块名称，如 "更新 business-cultivation 的 AGENTS.md"

### Q: 如何保留自定义内容？
A: AI 会读取现有 AGENTS.md，保留重要的自定义内容

### Q: 更新后发现错误怎么办？
A: 使用 `git revert` 回滚，或手动修复
