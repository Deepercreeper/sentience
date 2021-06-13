package org.deepercreeper.sentience.tagger

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.document.HasTags
import org.springframework.context.ApplicationContext
import kotlin.reflect.KClass


fun interface TaggerConfig {
    fun create(document: Document, context: ApplicationContext): Tagger
}

open class SimpleTaggerConfig(private val constructor: (Document, ApplicationContext) -> Tagger) : TaggerConfig {
    constructor(constructor: (Document) -> Tagger) : this({ document, _ -> constructor(document) })

    override fun create(document: Document, context: ApplicationContext) = constructor(document, context)
}

abstract class Tagger(protected val document: Document) {
    private val tagRegistry = KeyRegistry<Tag, String> { it.key }

    private val registry = TypeRegistry()

    lateinit var engine: TaggerEngine

    val tags: HasTags get() = engine.tags

    fun init(engine: TaggerEngine) {
        this.engine = engine
        init()
        engine.register(tagRegistry, registry)
    }

    protected abstract fun init()

    fun fire(event: Event) = engine.fire(event)

    fun register(keys: Iterable<String>, listener: Listener<Tag>) = keys.forEach { tagRegistry.register(it, listener) }

    fun register(vararg keys: String, listener: Listener<Tag>) = keys.forEach { tagRegistry.register(it, listener) }

    fun unregister(key: String, listener: Listener<Tag>) = tagRegistry.unregister(key, listener)

    fun unregister(key: String) = tagRegistry.unregister(key)

    fun <E : Event> register(type: KClass<E>, listener: Listener<E>) = registry.register(type, listener)

    fun <E : Event> unregister(type: KClass<E>, listener: Listener<E>) = registry.unregister(type, listener)

    fun <E : Event> unregister(type: KClass<E>) = registry.unregister(type)

    fun unregisterAll() {
        tagRegistry.unregisterAll()
        registry.unregisterAll()
    }

    object Key {
        const val VALUE = "value"
    }
}

inline fun <reified E : Event> Tagger.register(listener: Listener<E>) = register(E::class, listener)

inline fun <reified E : Event> Tagger.unregister(listener: Listener<E>) = unregister(E::class, listener)

inline fun <reified E : Event> Tagger.unregister() = unregister(E::class)