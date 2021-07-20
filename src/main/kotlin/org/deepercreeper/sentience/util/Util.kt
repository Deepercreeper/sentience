package org.deepercreeper.sentience.util

import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.context.annotation.Scope
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component


inline fun <T, K, V> Iterable<T>.associateAll(transform: (T) -> Pair<K, V>) = asSequence().associateAll(transform)

inline fun <T, K, V> Sequence<T>.associateAll(transform: (T) -> Pair<K, V>): Map<K, List<V>> {
    val map = mutableMapOf<K, MutableList<V>>()
    forEach {
        val (key, value) = transform(it)
        map.computeIfAbsent(key) { mutableListOf() } += value
    }
    return map
}

inline fun <T : Any, I : Any, R> JpaRepository<T, I>.update(id: I, operation: (T) -> R): R {
    val item = getOne(id)
    val result = operation(item)
    save(item)
    return result
}

inline fun <reified T> JsonNode.readValueAs(codec: ObjectCodec): T = traverse(codec).readValueAs(T::class.java)

@Target(AnnotationTarget.CLASS)
@Component
@Scope("prototype")
annotation class PrototypeComponent