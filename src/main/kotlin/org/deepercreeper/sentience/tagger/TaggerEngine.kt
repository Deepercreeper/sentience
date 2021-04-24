package org.deepercreeper.sentience.tagger

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.document.HasTags
import org.deepercreeper.sentience.document.TagManager
import org.deepercreeper.sentience.util.logger
import java.util.*


class TaggerEngine(private val document: Document, configs: List<TaggerConfig>) {
    private val tagRegistry = KeyRegistry<Tag, String> { it.key }

    private val registry = TypeRegistry()

    private val events: MutableList<Event> = LinkedList()

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
        events += event
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
        while (events.isNotEmpty()) {
            registry.handle(events.removeAt(0))
        }
    }
}