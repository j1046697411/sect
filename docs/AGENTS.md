# 文档管理 - AI 指引

## 目录定位
项目所有非代码文档的存放地，采用分类索引架构。

## 核心职责
- 维护项目需求、设计、技术和运维文档
- 提供全局及分类文档索引（index.md）
- 维护代码地图（codemaps）以保持代码与文档的同步

## AI 开发指引
- **仅限中文**: 所有文档内容必须使用中文。
- **索引优先**: 严禁直接引用子文档，必须通过目录下的 `index.md` 引用。
- **分类存放**: 新增文档必须归类到正确的子目录下。
- **同步更新**: 代码变更必须同步更新相关文档和代码地图。

## 目录结构

| 目录 | 职责 | 说明 |
|------|------|------|
| `requirements/` | 核心需求与规格说明 | 游戏需求、功能规格 |
| `design/` | 游戏玩法与 UI/UX 设计 | 设计文档、界面设计 |
| `technology/` | 技术架构与核心实现规范 | ECS 架构、技术规范 |
| `operations/` | 贡献指南与运维手册 | 部署、运维文档 |
| `codemaps/` | 代码模块与 API 映射地图 | 代码结构说明 |
| `memory-bank/` | 项目记忆库 | AI 记忆和上下文 |
| `plans/` | 实现计划 | 功能实现计划 |

## 子模块索引

- [requirements](./requirements/AGENTS.md): 核心需求与规格说明
- [design](./design/AGENTS.md): 游戏玩法与 UI/UX 设计
- [technology](./technology/AGENTS.md): 技术架构与核心实现规范
- [operations](./operations/AGENTS.md): 贡献指南与运维手册
- [codemaps](./codemaps/AGENTS.md): 代码模块与 API 映射地图
