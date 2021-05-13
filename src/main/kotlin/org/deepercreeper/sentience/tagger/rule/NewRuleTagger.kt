package org.deepercreeper.sentience.tagger.rule

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.ShutdownEvent
import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.tagger.Tagger
import org.deepercreeper.sentience.tagger.register
import org.deepercreeper.sentience.tagger.token.SubTokenTagger
import org.deepercreeper.sentience.tagger.token.TokenTagger
import kotlin.math.max


interface Position {
    val start: Int

    val end: Int

    val length get() = end - start
}

class MutablePosition(override var start: Int = 0, override var end: Int = 0) : Position

interface Status {
    val position: Position

    operator fun get(key: String): List<Tag>
}

abstract class AbstractRuleTagger(document: Document, private val length: Int) : Tagger(document), Status {
    init {
        require(length >= 0)
    }

    private val status = mutableMapOf<String, MutableList<Tag>>()

    private val _position = MutablePosition()

    override val position: Position get() = _position

    protected abstract val keys: List<String>

    protected abstract val rule: Rule

    override fun init() {
        register(TokenTagger.KEY, SubTokenTagger.KEY) { updatePosition(it.start) }
        register<ShutdownEvent> { updatePosition(document.text.length) }
        keys.forEach { key ->
            register(key) {
                status.computeIfAbsent(key) { mutableListOf() } += it
                update()
            }
        }
    }

    private fun updatePosition(end: Int) {
        if (end <= _position.end) return
        val start = end - length
        _position.start = max(0, start)
        _position.end = end
        update()
        status.values.forEach { tags -> tags.removeIf { it.end < start } }
        update()
    }

    private fun update() = rule.search(this).flatMap(this::tag).forEach { tags += it }

    override fun get(key: String) = status[key] ?: emptyList()

    protected abstract fun tag(status: Status): Sequence<Tag>
}

interface Rule {
    fun search(status: Status): Sequence<Status>
}
