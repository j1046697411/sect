# 记忆库索引

> 更新时间: 2026-02-23
> 总计: 46 条记忆

---

## 按系统域检索

### #角色系统 (6)
| 标题 | 分类 | 内容摘要 |
|------|------|----------|
| [角色层级](conventions/角色层级.md) | conventions | 掌门→长老→亲传→内门，晋升条件与权限 |
| [AI控制范围](conventions/AI控制范围.md) | conventions | AI角色 vs 资源角色划分 |
| [晋升机制](conventions/晋升机制.md) | conventions | 外门→内门选拔机制 |
| [晋升机制-任务制](conventions/晋升机制-任务制.md) | conventions | 任务选拔制详细流程 |
| [属性体系](solutions/属性体系.md) | solutions | 角色属性设计 |
| [资质系统](solutions/资质系统.md) | solutions | 资质影响成长 |

### #经济系统 (5)
| 标题 | 分类 | 内容摘要 |
|------|------|----------|
| [灵石流动](solutions/灵石流动.md) | solutions | 灵石产出与消耗循环 |
| [贡献点](solutions/贡献点.md) | solutions | 贡献点获取与使用 |
| [兑换机制](solutions/兑换机制.md) | solutions | 资源兑换设计 |
| [经济循环](solutions/经济循环.md) | solutions | 经济平衡机制 |
| [玩家参与](solutions/玩家参与.md) | solutions | 玩家经济干预方式 |

### #战斗系统 (2)
| 标题 | 分类 | 内容摘要 |
|------|------|----------|
| [战斗机制](conventions/战斗机制.md) | conventions | 回合制战斗规则 |
| [战斗结果](solutions/战斗结果.md) | solutions | 战斗结果与AI关系 |

### #功法系统 (2)
| 标题 | 分类 | 内容摘要 |
|------|------|----------|
| [功法分类](solutions/功法分类.md) | solutions | 功法类型与学习 |
| [功法传承](solutions/功法传承.md) | solutions | 功法效果与传承 |

### #设施系统 (3)
| 标题 | 分类 | 内容摘要 |
|------|------|----------|
| [设施分类](solutions/设施分类.md) | solutions | 设施种类划分 |
| [设施互动](solutions/设施互动.md) | solutions | 设施功能与互动 |
| [设施价值](solutions/设施价值.md) | solutions | 设施价值平衡 |

### #可玩性 (8)
| 标题 | 分类 | 内容摘要 |
|------|------|----------|
| [政策体系](conventions/政策体系.md) | conventions | 三层政策设计 |
| [AI干预](solutions/AI干预.md) | solutions | 单个AI干预机制 |
| [信息分级](solutions/信息分级.md) | solutions | AI行为信息分级显示 |
| [事件频率](solutions/事件频率.md) | solutions | 事件触发频率控制 |
| [境界补偿](solutions/境界补偿.md) | solutions | 境界差距补偿 |
| [里程碑显示](solutions/里程碑显示.md) | solutions | 进度里程碑展示 |
| [难度曲线](solutions/难度曲线.md) | solutions | 隐形难度曲线 |
| [目标系统](solutions/目标系统.md) | solutions | 多样化目标设计 |

### #ecs (3)
| 标题 | 分类 | 内容摘要 |
|------|------|----------|
| [ECS架构](conventions/ECS架构.md) | conventions | 六大核心模式 |
| [Query性能优化](solutions/Query性能优化.md) | solutions | FastList优化GC压力 |
| [ECS反模式](lessons/ECS反模式.md) | lessons | 常见错误与正确做法 |

### #kotlin (2)
| 标题 | 分类 | 内容摘要 |
|------|------|----------|
| [Kotlin开发规范](conventions/Kotlin开发规范.md) | conventions | 多平台兼容规范 |
| [Java库兼容](lessons/Java库兼容.md) | lessons | commonMain禁用java.* |

### #compose (2)
| 标题 | 分类 | 内容摘要 |
|------|------|----------|
| [Compose初始化陷阱](lessons/Compose初始化陷阱.md) | lessons | LaunchedEffect vs remember |
| [筛选状态陷阱](lessons/筛选状态陷阱.md) | lessons | 筛选要保留原始数据 |

### #测试 (1)
| 标题 | 分类 | 内容摘要 |
|------|------|----------|
| [TDD策略](conventions/TDD策略.md) | conventions | 红→绿→重构，覆盖率要求 |

---

## 按分类检索

### conventions (14)
技术规范与约定

| 标题 | 标签 |
|------|------|
| [术语规范](conventions/术语规范.md) | #规范 |
| [MVP范围](conventions/MVP范围.md) | #规范 |
| [记忆库命名规范](conventions/记忆库命名规范.md) | #规范 |
| [ECS架构](conventions/ECS架构.md) | #ecs #规范 |
| [TDD策略](conventions/TDD策略.md) | #测试 #规范 |
| [Kotlin开发规范](conventions/Kotlin开发规范.md) | #kotlin #规范 |
| [角色层级](conventions/角色层级.md) | #角色系统 #规范 |
| [AI控制范围](conventions/AI控制范围.md) | #角色系统 #规范 |
| [晋升机制](conventions/晋升机制.md) | #角色系统 #规范 |
| [晋升机制-任务制](conventions/晋升机制-任务制.md) | #角色系统 #规范 |
| [选拔奖励](conventions/选拔奖励.md) | #角色系统 #规范 |
| [选拔执行机制](conventions/选拔执行机制.md) | #角色系统 #规范 |
| [战斗机制](conventions/战斗机制.md) | #战斗系统 #规范 |
| [政策体系](conventions/政策体系.md) | #可玩性 #规范 |

### solutions (26)
问题解决方案

| 标题 | 标签 |
|------|------|
| [记忆库检索优化方案](solutions/记忆库检索优化方案.md) | #记忆库 #方案 |
| [Query性能优化](solutions/Query性能优化.md) | #ecs #性能 #方案 |
| [需求检查](solutions/需求检查.md) | #规范 #方案 |

### lessons (5)
错误教训

| 标题 | 标签 |
|------|------|
| [ECS反模式](lessons/ECS反模式.md) | #ecs #陷阱 |
| [Compose初始化陷阱](lessons/Compose初始化陷阱.md) | #compose #陷阱 |
| [筛选状态陷阱](lessons/筛选状态陷阱.md) | #compose #陷阱 |
| [Java库兼容](lessons/Java库兼容.md) | #kotlin #陷阱 |
| [时间计算陷阱](lessons/时间计算陷阱.md) | #陷阱 |

### preferences (2)
用户偏好

| 标题 | 标签 |
|------|------|
| [代码风格](preferences/代码风格.md) | #偏好 |
| [回复风格](preferences/回复风格.md) | #偏好 |

---

## 快速搜索

**输入关键词**：
- 弟子/角色 → [#角色系统](#角色系统-6)
- 经济/灵石 → [#经济系统](#经济系统-5)
- 战斗 → [#战斗系统](#战斗系统-2)
- 功法 → [#功法系统](#功法系统-2)
- 设施 → [#设施系统](#设施系统-3)
- ecs → [#ecs](#ecs-3)
- kotlin → [#kotlin](#kotlin-2)
- compose → [#compose](#compose-2)
- bug/错误 → [lessons](#lessons-5)
