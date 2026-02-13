# 业务域目录规范

本文件用于约束新增的业务域模块的目录结构与命名风格，确保跨域的一致性与自动化可检验性。

1. 顶层结构
- business-modules/ 作为所有业务域的父目录。
- 每个业务域独立成子模块，如 business-core、business-disciples、business-cultivation、business-quest、business-engine。
- 每个子模块包含 build.gradle.kts、MODULE.md、src/main/kotlin/、src/test/kotlin/。

2. MODULE.md 规范
- 统一字段：Module、Description、Responsibilities、Public API surface、Dependencies、Testing approach、Code style guidelines、Migration/Compatibility、Contributing notes。

3. 构建与依赖
- 子模块使用 JVM/Multiplatform 的最小必要配件，优先 JVM。
- 对外暴露通过 surface，降低对实现内部的耦合。

4. 流程
- 新域创建后，先补充 MODULE.md，再补充最小化的 build.gradle.kts 与一个简单的示例实现。
- 通过 settings.gradle.kts 注册新域。

5. 测试策略
- 每个域包含最小的单元测试骨架，必要时添加简单的集成测试。
- 覆盖率目标 80% 以上，核心域尽量 100%。

6. 变更与演化
- API 变更需在 MODULE.md 的 Migration/Compatibility 章节标注。
- 每次合并前执行一次简要的目录规则检查脚本。 
