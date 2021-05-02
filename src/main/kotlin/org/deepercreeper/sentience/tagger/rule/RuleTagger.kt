package org.deepercreeper.sentience.tagger.rule

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.tagger.Tagger

class RuleTaggerConfig(
    var key: String,
    val dependencies: MutableSet<String>,
    val conditions: MutableSet<Condition>,
    val targets: MutableSet<String>
) : SimpleTaggerConfig({ RuleTagger(it, key, dependencies.toSet(), conditions.toSet(), targets.toSet()) })

open class RuleTagger(
    document: Document,
    private val key: String,
    private val dependencies: Set<String>,
    private val conditions: Set<Condition>,
    private val targets: Set<String>,
    private val mappings: Map<String, Any>
) : Tagger(document) {
    private val slots = mutableMapOf<String, Tag>()

    init {
        require(dependencies.containsAll(targets))
    }

    constructor(
        document: Document,
        key: String,
        dependencies: Set<String>,
        conditions: Set<Condition>,
        targets: Set<String>,
        vararg mappings: Pair<String, Any>
    ) : this(document, key, dependencies, conditions, targets, mappings.toMap())

    override fun init() {
        dependencies.forEach {
            register(it) { tag ->
                slots[tag.key] = tag
                update()
            }
        }
        //TODO Add listener for ShutdownEvent that allows to react to the document ending
    }

    private fun update() {
        if (conditions.any { !it.matches(slots) }) return
        val targets = targets.asSequence().map { slots[it] }.filterNotNull().toSet()
        if (targets.isEmpty()) error("No target found")
        val start = targets.minOf { it.start }
        val end = targets.maxOf { it.end }
        tags += Tag(key, start, end - start, mappings)
        slots.clear()
    }
}

fun interface Condition {
    fun matches(slots: Map<String, Tag>): Boolean

    infix fun and(condition: Condition) = Condition { slots -> matches(slots) && condition.matches(slots) }

    infix fun or(condition: Condition) = Condition { slots -> matches(slots) || condition.matches(slots) }

    infix fun xor(condition: Condition) = Condition { slots -> matches(slots) xor condition.matches(slots) }

    operator fun not() = Condition { slots -> !matches(slots) }

    class Ordered(private vararg val keys: String) : Condition {
        init {
            require(keys.size > 1)
        }

        override fun matches(slots: Map<String, Tag>): Boolean {
            val tags = keys.map { slots[it] ?: return false }
            return tags.asSequence().zipWithNext().all { (left, right) -> left < right }
        }

        override fun toString() = keys.joinToString(separator = " < ")
    }

    class Distance(private val distance: Int, private vararg val keys: String, private val inner: Boolean = true) : Condition {
        init {
            require(distance > 0)
            require(keys.size > 1)
        }

        override fun matches(slots: Map<String, Tag>): Boolean {
            val tags = keys.map { slots[it] ?: return false }
            return if (inner) tags.asSequence().sorted().zipWithNext().all { (left, right) -> right.start - left.end <= distance }
            else tags.maxOf { it.end } - tags.minOf { it.start } < distance
        }

        override fun toString() = if (inner) "$distance < ${keys.joinToString()} >" else "$distance > ${keys.joinToString()} <"
    }
}