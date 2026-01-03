package cn.jzl.ecs.component

import cn.jzl.ecs.WorldOwner
import kotlin.reflect.KClassifier

interface ComponentProvider : WorldOwner {
    fun getOrRegisterEntityForClass(classifier: KClassifier): ComponentId
}

