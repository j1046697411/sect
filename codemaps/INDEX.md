# 代码地图索引

**最后更新**: 2026-02-14

## 概述

本目录包含宗门修真录项目的代码架构文档。这些文档描述了项目的整体结构、模块关系和关键 API。

## 代码地图

| 文档 | 说明 |
|------|------|
| [architecture.md](architecture.md) | 整体架构、项目概览和模块依赖 |
| [backend.md](backend.md) | ECS 框架核心、后端服务和 DSL |
| [frontend.md](frontend.md) | 应用层、前端架构和 UI |
| [data.md](data.md) | 数据模型、序列化和持久化 |

## 模块结构

### 核心库 (libs/)

| 模块 | 说明 | 代码地图 |
|------|------|----------|
| lko-core | 基础工具库 (集合、位运算) | - |
| lko-di | 依赖注入框架 | backend.md |
| lko-ecs | ECS 框架核心 | backend.md |
| lko-ecs-serialization | 序列化支持 | data.md |

### 业务模块 (business-modules/)

| 模块 | 说明 | 状态 |
|------|------|------|
| business-core | 核心业务 | 框架阶段 |
| business-disciples | 弟子系统 | 规划中 |
| business-cultivation | 修炼系统 | 规划中 |
| business-quest | 任务系统 | 规划中 |
| business-engine | 游戏引擎 | 规划中 |

### 应用层

| 模块 | 说明 | 代码地图 |
|------|------|----------|
| composeApp | 应用主模块 | frontend.md |
| androidApp | Android 应用 | frontend.md |

## 更新日志

- **2026-02-14**: 初始代码地图创建
  - 分析了所有核心模块
  - 生成了架构、后端、前端和数据模型文档

## 相关文档

- [ECS 架构详解](../docs/ecs-architecture.md)
- [游戏需求文档](../docs/文字游戏需求文档_GRD.md)
- [页面布局设计](../docs/pages/)
