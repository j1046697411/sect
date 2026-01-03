package cn.jzl.ecs.query

import cn.jzl.ecs.WorldOwner
import cn.jzl.ecs.family.Family

interface QueryStreamScope : WorldOwner {
    val family: Family
}