package org.deepercreeper.sentience.tagger

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.document.HasTags
import org.deepercreeper.sentience.document.TagManager
import org.deepercreeper.sentience.util.logger


class TaggerEngine(private val document: Document, configs: List<TaggerConfig>) {
    private val tagRegistry = KeyRegistry<Tag, String> { it.key }

    private val registry = TypeRegistry()

    private val eventManager = EventManager()

    private val tagManager = TagManager()

    init {
        tagManager.register(this::fire)
        registry.register<Tag> { tagRegistry.handle(it) }
        configs.asSequence().map { it.create(document) }.forEach { it.init(this) }
    }

    val tags: HasTags get() = tagManager

    constructor(document: Document, vararg taggers: TaggerConfig) : this(document, taggers.toList())

    fun register(tagRegistry: KeyRegistry<Tag, String>, registry: TypeRegistry) {
        this.tagRegistry.register(tagRegistry)
        this.registry.register(registry)
    }

    fun fire(event: Event) {
        eventManager += event
    }

    fun process() {
        logger.info("==> Startup...")
        fire(StartupEvent())
        processEvents()
        logger.info("<== Startup finished")
        logger.info("==> Processing...")
        fire(ProcessEvent())
        processEvents()
        logger.info("<== Processing finished")
        logger.info("==> Shutdown...")
        fire(ShutdownEvent())
        processEvents()
        logger.info("<== Shutdown finished")
    }

    private fun processEvents() {
        var event = eventManager.poll()
        while (event != null) {
            registry.handle(event)
            event = eventManager.poll()
        }
    }

    fun print() {
        val starts = tags.tags().map { it.start }.sorted().toMutableList()
        val ends = tags.tags().map { it.end }.sorted().toMutableList()
        var index = 0
        val text = document.text
        val result = StringBuilder()
        while (starts.isNotEmpty() || ends.isNotEmpty()) {
            val start = starts.firstOrNull() ?: text.length < ends.firstOrNull() ?: text.length
            val next = if (start) starts.removeFirst() else ends.removeFirst()
            val infix = if (start) "<" else ">"
            if (next > index) result.append(text, index, next)
            result.append(infix)
            index = next
        }
        if (index < text.length) result.append(text, index, text.length)
        println(result)
    }
}