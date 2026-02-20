package cn.jzl.ecs

/**
 * 世界所有者接口
 *
 * 标记一个类拥有对 [World] 的访问能力。实现此接口的类可以：
 * - 访问世界中的所有服务
 * - 使用世界相关的扩展函数
 * - 作为上下文接收者使用 ECS DSL
 *
 * ## 使用示例
 * ```kotlin
 * class MySystem : WorldOwner {
 *     override lateinit var world: World
 *
 *     fun doSomething() {
 *         // 可以直接访问 world
 *         val entity = world.entity { ... }
 *     }
 * }
 * ```
 *
 * @property world 关联的 ECS 世界实例
 */
interface WorldOwner {
    val world: World
}
