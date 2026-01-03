package cn.jzl.sect

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform