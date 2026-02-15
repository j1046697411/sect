# client - AI 指引

## 模块定位
应用层主模块，基于 Compose Multiplatform 构建的多平台 UI 入口。

## 核心职责
- 跨平台 UI 组件实现
- 游戏状态（ECS World）到 UI 的渲染同步
- 平台相关逻辑适配（JVM, Android, Web, Wasm）
- 应用启动与依赖组装

## AI 开发指引
- **UI/UX 规范**: 严格遵循 `docs/design/` 下的页面设计文档。
- **状态读取**: 优先使用 `lko-di` 获取 `World` 实例，通过 `Query` 订阅状态变更。
- **响应式**: 确保 UI 操作不阻塞主循环，耗时逻辑放在协程中。

## 关键 API
- `SectApp`: 应用主入口
- `Platform`: 平台抽象接口
- `UI Components`: 各类游戏界面组件
