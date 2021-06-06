package org.deepercreeper.sentience.tagger.rule

import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.util.associateAll

interface Position {
    val start: Int

    val end: Int

    val length get() = end - start
}

class MutablePosition(override var start: Int = 0, override var end: Int = 0) : Position

interface Status {
    val position: Position

    operator fun get(key: String): List<Tag>

    fun with(tags: Iterable<Tag>) = object : Status {
        private val keys = tags.associateAll { it.key to it }

        override val position get() = this@Status.position

        override fun get(key: String) = keys[key] ?: emptyList()
    }

    fun with(vararg tags: Tag) = with(tags.toList())

    fun with(tags: List<Tag>) = SortedStatus(position, tags)
}

class SortedStatus(override val position: Position, val tags: List<Tag>) : Status {
    private val keys = tags.associateAll { it.key to it }

    val size get() = tags.size

    operator fun get(index: Int) = tags[index]

    override fun get(key: String) = keys[key] ?: emptyList()
}