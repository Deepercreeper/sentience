package org.deepercreeper.sentience.util


inline fun <T, K, V> Iterable<T>.associateAll(transform: (T) -> Pair<K, V>) = asSequence().associateAll(transform)

inline fun <T, K, V> Sequence<T>.associateAll(transform: (T) -> Pair<K, V>): Map<K, List<V>> {
    val map = mutableMapOf<K, MutableList<V>>()
    forEach {
        val (key, value) = transform(it)
        map.computeIfAbsent(key) { mutableListOf() } += value
    }
    return map
}