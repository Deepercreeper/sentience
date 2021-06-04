package org.deepercreeper.sentience.tagger.rule

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.*
import org.deepercreeper.sentience.tagger.token.SubTokenTagger
import org.deepercreeper.sentience.tagger.token.TokenTagger
import kotlin.math.max


class RuleTaggerConfig(
    var key: String,
    var rule: Rule,
    var targets: Set<String>,
    var length: Int
) : SimpleTaggerConfig({ RuleTagger(it, length, key, rule, targets) })

class RuleTagger(
    document: Document,
    length: Int,
    private val key: String,
    override val rule: Rule,
    private val targets: Set<String> = rule.keys,
    private val mapping: (Status) -> Map<String, Any> = { emptyMap() }
) : AbstractRuleTagger(document, length) {
    init {
        require(rule.keys.containsAll(targets))
    }

    override fun tag(status: Status): Sequence<Tag> {
        val targets = targets.asSequence().flatMap { status[it].asSequence() }.toSet().takeIf { it.isNotEmpty() } ?: error("No target found")
        val start = targets.minOf { it.start }
        val end = targets.minOf { it.start }
        return sequenceOf(Tag(key, start, end - start, mapping(status)))
    }
}

abstract class AbstractRuleTagger(document: Document, private val length: Int) : Tagger(document), Status {
    init {
        require(length >= 0)
    }

    private val status = mutableMapOf<String, MutableList<Tag>>()

    private val _position = MutablePosition()

    override val position: Position get() = _position

    protected abstract val rule: Rule

    override fun init() {
        register(TokenTagger.KEY, SubTokenTagger.KEY) { updatePosition(it.start) }
        register<ShutdownEvent> { updatePosition(document.text.length) }
        rule.keys.forEach { key ->
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

