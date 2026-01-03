package cn.jzl.di

interface DIAware {
    val context: DIContext<*>
    val trigger: DITrigger
    val di: DI
}