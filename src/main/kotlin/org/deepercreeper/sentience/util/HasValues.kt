package org.deepercreeper.sentience.util

import kotlin.reflect.KClass
import kotlin.reflect.cast


interface HasValues {
    val keys: Set<String>

    fun keys(): Sequence<String>

    operator fun <T : Any> set(key: String, value: T?)

    operator fun <T : Any> get(key: String, type: KClass<T>): T?
}

class ValueMap(mappings: Map<String, Any>) : HasValues {
    private val map by lazy { mappings.toMutableMap() }

    constructor(vararg mappings: Pair<String, Any>) : this(mappings.toMap())

    override val keys get() = map.keys.toSet()

    override fun keys() = map.keys.asSequence()

    override operator fun <T : Any> set(key: String, value: T?) = if (value != null) map[key] = value else map -= key

    override operator fun <T : Any> get(key: String, type: KClass<T>): T? = map[key]?.let { type.cast(it) }
}

inline operator fun <reified T : Any> HasValues.get(key: String): T? = get(key, T::class)