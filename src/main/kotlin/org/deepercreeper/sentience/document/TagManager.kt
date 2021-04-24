package org.deepercreeper.sentience.document

import org.deepercreeper.sentience.tagger.Tag


interface HasTags {
    val tags: List<Tag>

    fun tags(): Sequence<Tag>

    operator fun plusAssign(tag: Tag)

    operator fun minusAssign(tag: Tag)

    operator fun get(key: String): Set<Tag>

    operator fun get(start: Int, end: Int): List<Tag>
}

class TagManager(vararg listeners: (Tag) -> Unit) : HasTags {
    private val listeners = listeners.toMutableList()

    private val _tags = mutableSetOf<Tag>()

    private val keys = mutableMapOf<String, MutableSet<Tag>>()

    override val tags get() = _tags.sorted()

    override fun tags() = _tags.asSequence()

    fun register(listener: (Tag) -> Unit) {
        listeners += listener
    }

    override operator fun plusAssign(tag: Tag) {
        _tags += tag
        keys.computeIfAbsent(tag.key) { mutableSetOf() } += tag
        listeners.forEach { it(tag) }
    }

    override operator fun minusAssign(tag: Tag) {
        _tags -= tag
        keys[tag.key]?.let { if (it.size > 1) it -= tag else keys -= tag.key }
    }

    override operator fun get(key: String) = keys[key] ?: emptySet()

    override operator fun get(start: Int, end: Int) = _tags.filter { start < it.end && it.start < end }
}