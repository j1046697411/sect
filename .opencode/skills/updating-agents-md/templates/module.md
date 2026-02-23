# {{MODULE_NAME}} - 模块描述

## 模块定位
{{MODULE_DESCRIPTION}}

**层级**: {{LAYER}}

## 核心职责
{{CORE_RESPONSIBILITIES}}

## 目录结构
```
{{MODULE_NAME}}/src/commonMain/kotlin/cn/jzl/sect/{{MODULE_PATH}}/
├── {{ADDON_FILE}}.kt      # 模块入口
├── components/            # 组件定义
│   └── ...
└── services/              # 服务实现
    └── ...
```

## 关键 API

### 组件
| 组件 | 用途 | 属性 |
|------|------|------|
{{COMPONENTS_TABLE}}

### 服务
| 服务 | 用途 | 核心方法 |
|------|------|----------|
{{SERVICES_TABLE}}

## 使用方式

```kotlin
// 1. 安装 Addon
world.install({{ADDON_NAME}})

// 2. 获取服务
val {{SERVICE_NAME}} by world.di.instance<{{SERVICE_CLASS}}>()

// 3. 使用服务
{{USAGE_EXAMPLE}}
```

## 依赖关系

```kotlin
// build.gradle.kts
dependencies {
{{DEPENDENCIES}}
}
```

```
依赖图：
{{MODULE_NAME}}
{{DEPENDENCY_GRAPH}}
```

## AI 开发指引

### 开发原则
{{DEVELOPMENT_PRINCIPLES}}

### 添加新功能检查清单
- [ ] 是否遵循模块职责？
- [ ] 是否需要新的组件/服务？
- [ ] 测试覆盖率是否达标？
- [ ] 是否遵循 TDD 开发模式？

## 禁止事项
{{FORBIDDEN_PATTERNS}}

## 测试要求
{{TEST_REQUIREMENTS}}
