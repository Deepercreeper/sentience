package org.deepercreeper.sentience.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass


object LogUtil

private val CACHE = mutableMapOf<KClass<*>, Logger>()

val Any.logger get() = this::class.logger

val KClass<*>.logger get() = synchronized(CACHE) { CACHE.computeIfAbsent(this) { computeLogger(java) } }

private tailrec fun computeLogger(type: Class<*>): Logger {
    if (type.simpleName == "Companion") return LoggerFactory.getLogger(type.enclosingClass)
    if (!type.isSynthetic && !type.isAnonymousClass) return LoggerFactory.getLogger(type)
    var parentType = type.superclass
    if (parentType == Any::class.java) parentType = type.interfaces.firstOrNull() ?: LogUtil::class.java
    return computeLogger(parentType)
}