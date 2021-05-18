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

    override fun equals(other: Any?) = this === other || other is ValueMap && map == other.map || other is HasValues && keys == other.keys && keys.all { map[it] == other[it] }

    override fun hashCode() = map.hashCode()

    override fun toString() = map.toString()
}

inline operator fun <reified T : Any> HasValues.get(key: String): T? = get(key, T::class)