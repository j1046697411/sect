package cn.jzl.ecs

/**
 * ECS DSL 标记注解
 *
 * 用于标记 ECS 框架的 DSL（领域特定语言）构建器函数。
 * 此注解帮助 IDE 和编译器提供更好的代码提示和类型检查。
 *
 * 被标记的函数通常用于：
 * - 创建实体 ([world.entity])
 * - 编辑实体 ([world.editor])
 * - 执行查询 ([world.query])
 * - 配置插件 ([WorldSetup.install])
 *
 * ## 使用示例
 * ```kotlin
 * @ECSDsl
 * fun World.entity(configuration: EntityCreateContext.(Entity) -> Unit): Entity
 * ```
 */
@DslMarker
annotation class ECSDsl()
