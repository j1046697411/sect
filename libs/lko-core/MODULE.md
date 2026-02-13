# lko-core

**Module:** lko-core

## Description

高性能 Kotlin 多平台核心工具库，提供专门优化的集合实现和位操作工具。

## Responsibilities

- 提供高性能的原始类型列表实现（FastList 系列）
- 提供位操作工具（Bits、BitSet）
- 作为其他业务模块的基础依赖

## Public API surface

### 集合
- `IntFastList` / `IntMutableFastList` - 整型列表
- `LongFastList` / `LongMutableFastList` - 长整型列表
- `FloatFastList` / `FloatMutableFastList` - 浮点列表
- `DoubleFastList` / `DoubleMutableFastList` - 双精度列表
- `ShortFastList` / `ShortMutableFastList` - 短整型列表
- `ByteFastList` / `ByteMutableFastList` - 字节列表
- `CharFastList` / `CharMutableFastList` - 字符列表
- `ObjectFastList<T>` / `ObjectMutableFastList<T>` - 对象列表

### 位操作
- `Bits` - 位操作工具集
- `BitSet` - 位集合实现

### 其他
- `SortSet<T>` - 排序集合

## Dependencies

- Kotlin 标准库（无外部依赖）

## Testing approach

- 单元测试覆盖核心功能
- 目标覆盖率 80% 以上

## Code style guidelines

- 遵循项目通用 Kotlin 编码规范
- 包名：`cn.jzl.core`
- 类/接口：PascalCase
- 函数/属性：camelCase
- 常量：UPPER_SNAKE_CASE

## Migration/Compatibility

- 当前版本为初始版本，无迁移需求

## Contributing notes

- 新增集合类型需考虑 Multiplatform 支持
- 性能敏感代码需添加基准测试
