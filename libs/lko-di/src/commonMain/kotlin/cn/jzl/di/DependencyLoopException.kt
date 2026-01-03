package cn.jzl.di

class DependencyLoopException(message: String?, cause: Throwable? = null) : DIException(message, cause)