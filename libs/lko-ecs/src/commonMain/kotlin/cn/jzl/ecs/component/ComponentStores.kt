package cn.jzl.ecs.component

fun intStore(): ComponentStore<out Any> = IntComponentStore()

fun floatStore(): ComponentStore<out Any> = FloatComponentStore()

fun longStore(): ComponentStore<out Any> = LongComponentStore()

fun doubleStore(): ComponentStore<out Any> = DoubleComponentStore()

fun objectStore(): ComponentStore<out Any> = GeneralComponentStore()
