package org.deepercreeper.sentience.tagger

import kotlin.reflect.KClass


fun interface Listener<E : Event> : (E) -> Unit {
//TODO    val prio: Int get() = 0
}

class TypeRegistry {
    private val listeners = mutableMapOf<KClass<out Event>, MutableList<out Listener<*>>>()

    fun handle(event: Event) = get(event::class)?.asSequence()
//TODO        ?.sortedByDescending { it.prio }
        ?.forEach { it(event) } ?: Unit

    fun register(registry: TypeRegistry) {
        registry.listeners.keys.forEach { type -> addAll(type, registry.get(type)!!) }
    }

    fun <E : Event> register(type: KClass<E>, listener: Listener<E>) {
        get(type) ?: mutableListOf<Listener<E>>().also { listeners[type] = it } += listener
    }

    fun <E : Event> unregister(type: KClass<E>, listener: Listener<E>) {
        get(type)?.also {
            it -= listener
            if (it.isEmpty()) unregister(type)
        }
    }

    fun <E : Event> unregister(type: KClass<E>) {
        listeners -= type
    }

    fun unregisterAll() = listeners.clear()

    private fun <E : Event> addAll(type: KClass<out E>, listeners: List<Listener<E>>) {
        get(type) ?: mutableListOf<Listener<E>>().also { this.listeners[type] = it } += listeners
    }

    private fun <E : Event> get(type: KClass<out E>): MutableList<Listener<E>>? {
        @Suppress("UNCHECKED_CAST")
        return listeners[type] as MutableList<Listener<E>>?
    }
}

class KeyRegistry<E : Event, K>(private val key: (E) -> K) {
    private val listeners = mutableMapOf<K, MutableList<Listener<E>>>()

    fun handle(event: E) = listeners[key(event)]?.asSequence()
//TODO        ?.sortedByDescending { it.prio }
        ?.forEach { it(event) } ?: Unit

    fun register(registry: KeyRegistry<E, K>) {
        registry.listeners.forEach { (key, listeners) -> this.listeners.computeIfAbsent(key) { mutableListOf() } += listeners }
    }

    fun register(key: K, listener: Listener<E>) {
        listeners.computeIfAbsent(key) { mutableListOf() } += listener
    }

    fun unregister(key: K, listener: Listener<E>) {
        listeners[key]?.also {
            it -= listener
            if (it.isEmpty()) unregister(key)
        }
    }

    fun unregister(key: K) {
        listeners -= key
    }

    fun unregisterAll() = listeners.clear()
}

inline fun <reified E : Event> TypeRegistry.register(listener: Listener<E>) = register(E::class, listener)

inline fun <reified E : Event> TypeRegistry.unregister(listener: Listener<E>) = unregister(E::class, listener)

inline fun <reified E : Event> TypeRegistry.unregister() = unregister(E::class)