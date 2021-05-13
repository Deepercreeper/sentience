package org.deepercreeper.sentience.tagger.rule

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.*
import org.deepercreeper.sentience.tagger.token.SubTokenTagger
import org.deepercreeper.sentience.tagger.token.TokenTagger

class RuleTaggerConfig(
    var key: String,
    val dependencies: MutableSet<String>,
    val conditions: MutableSet<Condition>,
    val targets: MutableSet<String>,
    var distance: Int
) : SimpleTaggerConfig({ RuleTagger(it, key, dependencies.toSet(), conditions.toSet(), targets.toSet(), distance) })

interface HasSlots {
    val distance: Int

    val index: Int

    operator fun get(key: String): List<Tag>
}

abstract class AbstractConditionalTagger(document: Document) : Tagger(document), HasSlots {
    private val slots = mutableMapOf<String, MutableList<Tag>>()

    protected abstract val dependencies: Set<String>

    protected abstract val conditions: Set<Condition>

    final override var index = 0
        private set

    override fun init() {
        register(TokenTagger.KEY, SubTokenTagger.KEY) { updateIndex(it.start) }
        dependencies.forEach { key ->
            register(key) {
                slots.computeIfAbsent(it.key) { mutableListOf() } += it
                update()
            }
        }
        register<ShutdownEvent> { updateIndex(document.text.length) }
    }

    override fun get(key: String) = slots[key] ?: emptyList()

    private fun updateIndex(index: Int) {
        if (index <= this.index) return
        this.index = index
        update()
        slots.values.forEach { slot -> slot.removeIf { index - it.end > distance } }
        update()
    }

    private fun update() {
        if (conditions.all { it.matches(this) }) tag()
    }

    protected abstract fun tag()
}

class RuleTagger(
    document: Document,
    private val key: String,
    override val dependencies: Set<String>,
    override val conditions: Set<Condition>,
    private val targets: Set<String>,
    override val distance: Int,
    private val mapping: (HasSlots) -> Map<String, Any>
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

    override fun tag() {
        val targets = targets.asSequence().map { this[it] }.filterNotNull().filter { it.isNotEmpty() }.map { it.last() }.toSet()
        if (targets.isEmpty()) error("No target found")
        val start = targets.minOf { it.start }
        val end = targets.maxOf { it.end }
        tags += Tag(key, start, end - start, mapping(this))
    }
}

fun interface Condition {
    fun matches(slots: HasSlots): Boolean

    infix fun and(condition: Condition) = and(this, condition)

    infix fun or(condition: Condition) = or(this, condition)

    infix fun xor(condition: Condition) = xor(this, condition)

    operator fun not() = not(this)

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

        override fun matches(tags: List<Tag>) = tags.asSequence().sorted().zipWithNext().all { (left, right) -> right.start - left.end <= distance }

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

        override fun matches(slots: HasSlots): Boolean {
            val slotTags = keys.map { key -> slots[key].takeIf { it.isNotEmpty() } ?: return false }
            return slotTags.anyCombination { matches(it) }
        }

        fun findAll(slots: HasSlots): Sequence<List<Tag>> {
            val slotTags = keys.map { key -> slots[key].takeIf { it.isNotEmpty() } ?: return emptySequence() }
            return slotTags.findAll(this::matches)
        }

        protected abstract fun matches(tags: List<Tag>): Boolean
    }

    companion object {
        fun and(vararg conditions: Condition) = object : Condition {
            init {
                require(conditions.size > 1)
            }

            override fun matches(slots: HasSlots) = conditions.all { it.matches(slots) }

            override fun toString() = conditions.joinToString(separator = " and ") { "($it)" }
        }

        fun or(vararg conditions: Condition) = object : Condition {
            init {
                require(conditions.size > 1)
            }

            override fun matches(slots: HasSlots) = conditions.any { it.matches(slots) }

            override fun toString() = conditions.joinToString(separator = " or ") { "($it)" }
        }

        fun xor(vararg conditions: Condition) = object : Condition {
            init {
                require(conditions.size > 1)
            }

            override fun matches(slots: HasSlots) = conditions.count { it.matches(slots) } % 2 == 1

            override fun toString() = conditions.joinToString(separator = " xor ") { "($it)" }
        }

        fun not(condition: Condition) = object : Condition {
            override fun matches(slots: HasSlots) = !condition.matches(slots)

            override fun toString() = "not ($condition)"
        }
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
    return last().asSequence().flatMap { item -> next.findAll { condition(it + item) }.map { it + item } }
}