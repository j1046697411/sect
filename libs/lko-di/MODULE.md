# lko-di

**Module:** lko-di

## Description

基于 Kodein 的轻量级依赖注入框架，提供上下文感知的依赖管理和作用域控制。

## Responsibilities

- 依赖注入容器管理
- 上下文感知的依赖解析
- 作用域和生命周期管理
- 依赖循环检测

## Public API surface

### 核心接口
- `DI` - 依赖注入容器主接口
- `DIContainer` - 依赖容器
- `DIAware` - DI 感知标记接口
- `DirectDI<C>` - 上下文直接访问

### 构建
- `DI.Builder` - DI 构建器
- `DIMainBuilder` - 主构建器
- `DIModule<MC>` - 模块定义
- `DIMainBuilderImpl` - 构建器实现

### 绑定
- `Binding<T>` - 绑定定义
- `DIBinding` - 依赖绑定
- `BindBuilder` - 绑定构建器

### 作用域
- `Scope` - 作用域接口
- `NoScope` - 无作用域

### 异常
- `DIException` - DI 异常基类
- `DependencyLoopException` - 依赖循环异常
- `OverridingException` - 覆盖异常

## Dependencies

- Kodein（第三方 DI 库）

## Testing approach

- 单元测试覆盖核心功能
- 集成测试验证上下文传递
- 目标覆盖率 80% 以上

## Code style guidelines

- 遵循项目通用 Kotlin 编码规范
- 包名：`cn.jzl.di`
- 类/接口：PascalCase
- 函数/属性：camelCase
- 常量：UPPER_SNAKE_CASE

## Migration/Compatibility

- 当前版本为初始版本，无迁移需求

## Contributing notes

- 新增功能需考虑 Multiplatform 支持
- 公共 API 需添加 KDoc 文档
