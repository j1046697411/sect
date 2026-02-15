# business-ai-goap - AI 指引

## 模块定位
基于 GOAP (Goal-Oriented Action Planning) 的自主决策 AI 系统。

## 核心职责
- 提供 AI 决策规划核心算法
- 管理动作库和世界状态表示
- 支持动作链搜索与优化

## AI 开发指引
- **性能优先**: GOAP 搜索需要限制深度和时间，避免性能瓶颈
- **状态简洁**: 世界状态表示应尽量紧凑高效
- **动作原子化**: 每个动作应只完成单一目标

## 关键 API
- `Planner`: GOAP 规划器核心
- `Action`: 动作定义接口
- `WorldState`: 世界状态表示
