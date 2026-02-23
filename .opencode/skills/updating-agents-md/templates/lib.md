# {{MODULE_NAME}} - 基础库描述

## 模块定位
{{MODULE_DESCRIPTION}}

**层级**: {{LAYER}}

## 核心职责
{{CORE_RESPONSIBILITIES}}

## 目录结构
```
{{MODULE_NAME}}/src/commonMain/kotlin/cn/jzl/{{MODULE_PATH}}/
├── {{CORE_FILE}}.kt       # 核心定义
├── {{SUBDIR_1}}/          # 子目录1
│   └── ...
└── {{SUBDIR_2}}/          # 子目录2
    └── ...
```

## 关键 API

### 核心接口
| 接口/类 | 用途 | 说明 |
|---------|------|------|
{{KEY_APIS_TABLE}}

### 扩展函数
```kotlin
{{EXTENSION_FUNCTIONS}}
```

## 使用方式

```kotlin
{{USAGE_EXAMPLE}}
```

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
{{DEPENDENCIES}}
}
```

## AI 开发指引

### 开发原则
{{DEVELOPMENT_PRINCIPLES}}

### 性能要求
{{PERFORMANCE_REQUIREMENTS}}

### 添加新功能检查清单
- [ ] 是否避免引入外部依赖？（lko-core）
- [ ] 是否有边界检查？
- [ ] 是否有性能测试？
- [ ] 测试覆盖率是否达标？

## 禁止事项
{{FORBIDDEN_PATTERNS}}

## 测试要求
{{TEST_REQUIREMENTS}}
