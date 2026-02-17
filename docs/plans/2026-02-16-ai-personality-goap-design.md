# AI 性格驱动 GOAP 系统 - 实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**目标:** 实现一个 8 维度性格模型（Personality8），并将其整合进 GOAP 决策引擎，使 AI 能够根据性格差异化地做出决策，并提供决策原因的解释。

**架构:**
1.  **数据模型**: `Personality8` 存储 8 个维度的倾向值（-1 到 1）。
2.  **归一化与模板**: 提供归一化方法和预设的原型模板（如守财奴、苦行僧）。
3.  **GOAP 整合**: GOAP Planner 在计算 Action Cost/Priority 时，读取 Personality 权重进行修正。
4.  **解释器**: 实现 `Why()` 方法记录决策链条中的权重评分，方便调试和观察。

**技术栈:** Kotlin, GOAP, lko-ecs

---

## Task 1: Personality8 数据模型与原型模板

**Files:**
- Create: `business-modules/business-core/src/commonMain/kotlin/cn/jzl/ecs/ai/Personality8.kt`
- Create: `libs/lko-ecs/src/commonTest/kotlin/cn/jzl/ecs/ai/Personality8Test.kt`

**Step 1: 编写失败测试**
验证归一化逻辑（所有维度取绝对值求和，然后比例分配）和预设模板的有效性。

**Step 2: 实现数据类及 normalized() 方法**
每个维度范围 [-1, 1]。

**Step 3: 实现预设模板**
- **守财奴 (Miser)**: 贪婪(Greed)高，道德(Morality)低。
- **苦行僧 (Ascetic)**: 勤奋(Diligence)高，贪婪(Greed)极低，合群(Sociability)低。

**Step 4: 运行测试并提交**
```bash
./gradlew :libs:lko-ecs:test --tests "cn.jzl.ecs.ai.Personality8Test"
```

---

## Task 2: 决策解释器 (Decision Explainer)

**Files:**
- Create: `business-modules/business-core/src/commonMain/kotlin/cn/jzl/ecs/ai/DecisionExplainer.kt`

**Step 1: 定义 Explainer 接口**
记录 Action、基础分、性格修正因子、最终评分。

**Step 2: 实现日志输出逻辑**
支持友好易读的文本输出。

**Step 3: 编写单元测试**
验证输出文本是否包含关键权重信息。

**Step 4: 提交**

---

## Task 3: GOAP 整合与性格权重修正

**Files:**
- Modify: 相关 GOAP 逻辑文件（需定位项目内的 GOAP 实现）

**Step 1: 定位 GOAP 评分逻辑位置**
**Step 2: 整合 Personality8**
将性格倾向映射到 Action 的 Cost 或 Priority 修正值。

**Step 3: 整合 Explainer**
在评分过程中注入解释器。

**Step 4: 编写集成测试**
验证同样环境下一群不同性格的 AI 最终产出了不同的 Action 序列。

**Step 5: 提交**

---

## Task 4: 状态压制 (Urgency Override)

**Step 1: 实现 Urgency 逻辑**
当健康值过低或受到致命威胁时，临时压制性格权重。

**Step 2: 完成最终验证**
运行所有业务逻辑测试。

**Step 3: 提交**
