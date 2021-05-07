package org.deepercreeper.sentience.tagger.rule

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.tagger.Tagger

class RuleTaggerConfig(
    var key: String,
    val dependencies: MutableSet<String>,
    val conditions: MutableSet<Condition>,
    val targets: MutableSet<String>,
    var distance: Int
) : SimpleTaggerConfig({ RuleTagger(it, key, dependencies.toSet(), conditions.toSet(), targets.toSet(), distance) })

typealias Slots = Map<String, List<Tag>>

abstract class AbstractConditionalTagger(document: Document) : Tagger(document) {
    private val slots = mutableMapOf<String, MutableList<Tag>>()

    protected abstract val dependencies: Set<String>

    protected abstract val conditions: Set<Condition>

    protected abstract val distance: Int

    override fun init() {
        dependencies.forEach { key ->
            register(key) { tag ->
                slots.values.forEach { tags -> tags.removeIf { tag.start - it.end > distance } }
                slots.computeIfAbsent(tag.key) { mutableListOf() } += tag
                update()
            }
        }
        //TODO Add listener for ShutdownEvent that allows to react to the document ending
    }

    protected abstract fun tag(slots: Slots)

    private fun update() {
        if (conditions.all { it.matches(slots) }) {
            tag(slots)
            slots.values.forEach { it.clear() }
        }
    }
}

class RuleTagger(
    document: Document,
    private val key: String,
    override val dependencies: Set<String>,
    override val conditions: Set<Condition>,
    private val targets: Set<String>,
    override val distance: Int,
    private val mapping: (Slots) -> Map<String, Any>
) : AbstractConditionalTagger(document) {
    init {
        require(dependencies.containsAll(targets))
    }

    constructor(
        document: Document,
        key: String,
        dependencies: Set<String>,
        conditions: Set<Condition>,
        targets: Set<String>,
        distance: Int,
        mapping: Map<String, Any>
    ) : this(document, key, dependencies, conditions, targets, distance, { mapping })

    constructor(
        document: Document,
        key: String,
        dependencies: Set<String>,
        conditions: Set<Condition>,
        targets: Set<String>,
        distance: Int,
        vararg mappings: Pair<String, Any>
    ) : this(document, key, dependencies, conditions, targets, distance, mappings.toMap())

    override fun tag(slots: Slots) {
        val targets = targets.asSequence().map { slots[it] }.filterNotNull().filter { it.isNotEmpty() }.map { it.last() }.toSet()
        if (targets.isEmpty()) error("No target found")
        val start = targets.minOf { it.start }
        val end = targets.maxOf { it.end }
        tags += Tag(key, start, end - start, mapping(slots))
    }
}

fun interface Condition {
    fun matches(slots: Slots): Boolean

    infix fun and(condition: Condition) = Condition { slots -> matches(slots) && condition.matches(slots) }

    infix fun or(condition: Condition) = Condition { slots -> matches(slots) || condition.matches(slots) }

    infix fun xor(condition: Condition) = Condition { slots -> matches(slots) xor condition.matches(slots) }

    operator fun not() = Condition { slots -> !matches(slots) }

    class Ordered(keys: List<String>) : AbstractKeyCondition(keys) {
        constructor(vararg keys: String) : this(keys.toList())

        override fun matches(tags: List<Tag>) = tags.asSequence().zipWithNext().all { (left, right) -> left.end <= right.start }

        override fun toString() = keys.joinToString(separator = " < ")
    }

    class MaxInnerDistance(private val distance: Int, keys: List<String>) : AbstractKeyCondition(keys) {
        init {
            require(distance > 0)
        }

        constructor(distance: Int, vararg keys: String) : this(distance, keys.toList())

        override fun matches(tags: List<Tag>) = tags.asSequence().sorted().zipWithNext().all { (left, right) -> left.end - right.start <= distance }

        override fun toString() = "$distance < ${keys.joinToString()} >"
    }

    class MaxOuterDistance(private val distance: Int, keys: List<String>) : AbstractKeyCondition(keys) {
        init {
            require(distance > 0)
        }

        constructor(distance: Int, vararg keys: String) : this(distance, keys.toList())

        override fun matches(tags: List<Tag>) = tags.maxOf { it.end } - tags.minOf { it.start } <= distance

        override fun toString() = "$distance > ${keys.joinToString()} <"
    }

    class Disjoint(keys: List<String>) : AbstractKeyCondition(keys) {
        constructor(vararg keys: String) : this(keys.toList())

        override fun matches(tags: List<Tag>) = tags.asSequence().sorted().zipWithNext().all { (left, right) -> left.end <= right.start }

        override fun toString() = keys.joinToString(separator = " <> ")
    }

    abstract class AbstractKeyCondition(protected val keys: List<String>, minKeySize: Int = 2) : Condition {
        init {
            require(keys.size >= minKeySize)
        }

        constructor(vararg keys: String, minKeySize: Int = 2) : this(keys.toList(), minKeySize)

        override fun matches(slots: Slots): Boolean {
            val slotTags = keys.map { key -> slots[key]?.takeIf { it.isNotEmpty() } ?: return false }
            return slotTags.anyCombination { matches(it) }
        }

        fun findAll(slots: Slots): Sequence<List<Tag>> {
            val slotTags = keys.map { key -> slots[key]?.takeIf { it.isNotEmpty() } ?: return emptySequence() }
            return slotTags.findAll(this::matches)
        }

        protected abstract fun matches(tags: List<Tag>): Boolean
    }
}

private fun <T> List<List<T>>.anyCombination(condition: (List<T>) -> Boolean): Boolean {
    if (isEmpty()) return condition(emptyList())
    if (size == 1) return first().any { condition(listOf(it)) }
    val next = subList(0, size - 1)
    return last().any { item -> next.anyCombination { condition(it + item) } }
}

private fun <T> List<List<T>>.findAll(condition: (List<T>) -> Boolean): Sequence<List<T>> {
    if (isEmpty()) return if (condition(emptyList())) sequenceOf(emptyList()) else emptySequence()
    if (size == 1) return first().asSequence().map { listOf(it) }.filter(condition)
    val next = subList(0, size - 1)
    return last().asSequence().flatMap { item -> next.findAll { condition(it + item) } }
}