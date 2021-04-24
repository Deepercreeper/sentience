package org.deepercreeper.sentience.tagger

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.document.HasTags
import kotlin.reflect.KClass


//TODO This needs to be able to be saved to a single DB table row
//TODO One table (each factory type needs to provide some config map) or many tables (each factory needs an entity that manages exactly one factory)
fun interface TaggerConfig {
    fun create(document: Document): Tagger
}

open class SimpleTaggerConfig(private val constructor: (Document) -> Tagger) : TaggerConfig {
    override fun create(document: Document) = constructor(document)
}

abstract class Tagger(protected val document: Document) {
    private val tagRegistry = KeyRegistry<Tag, String> { it.key }

    private val registry = TypeRegistry()

    private lateinit var engine: TaggerEngine

    val tags: HasTags get() = engine.tags

    fun init(engine: TaggerEngine) {
        this.engine = engine
        init()
        engine.register(tagRegistry, registry)
    }

    protected abstract fun init()

    fun fire(event: Event) = engine.fire(event)

    fun register(key: String, listener: Listener<Tag>) = tagRegistry.register(key, listener)

    fun unregister(key: String, listener: Listener<Tag>) = tagRegistry.unregister(key, listener)

    fun unregister(key: String) = tagRegistry.unregister(key)

    fun <E : Event> register(type: KClass<E>, listener: Listener<E>) = registry.register(type, listener)

    fun <E : Event> unregister(type: KClass<E>, listener: Listener<E>) = registry.unregister(type, listener)

    fun <E : Event> unregister(type: KClass<E>) = registry.unregister(type)

    fun unregisterAll() {
        tagRegistry.unregisterAll()
        registry.unregisterAll()
    }
}

inline fun <reified E : Event> Tagger.register(listener: Listener<E>) = register(E::class, listener)

inline fun <reified E : Event> Tagger.unregister(listener: Listener<E>) = unregister(E::class, listener)

inline fun <reified E : Event> Tagger.unregister() = unregister(E::class)