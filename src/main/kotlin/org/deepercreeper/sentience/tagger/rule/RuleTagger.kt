package org.deepercreeper.sentience.tagger.rule

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.tagger.Tagger

class RuleTaggerConfig(
    var key: String,
    val dependencies: MutableSet<String>,
    val restrictions: MutableSet<Restriction>,
    val targets: MutableSet<String>
) : SimpleTaggerConfig({ RuleTagger(it, key, dependencies.toSet(), restrictions.toSet(), targets.toSet()) })

open class RuleTagger(
    document: Document,
    private val key: String,
    private val dependencies: Set<String>,
    private val restrictions: Set<Restriction>,
    private val targets: Set<String>,
    private val mappings: Map<String, Any>
) : Tagger(document) {
    private val slots = mutableMapOf<String, Tag>()

    init {
        require(dependencies.containsAll(targets))
    }

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
        if (restrictions.any { !it.matches(slots) }) return
        val targets = targets.asSequence().map { slots[it] }.filterNotNull().toSet()
        if (targets.isEmpty()) error("No target found")
        val start = targets.minOf { it.start }
        val end = targets.maxOf { it.end }
        tags += Tag(key, start, end - start, mappings)
        slots.clear()
    }
}

fun interface Restriction {
    fun matches(slots: Map<String, Tag>): Boolean

    infix fun and(restriction: Restriction) = Restriction { slots -> matches(slots) && restriction.matches(slots) }

    infix fun or(restriction: Restriction) = Restriction { slots -> matches(slots) || restriction.matches(slots) }

    infix fun xor(restriction: Restriction) = Restriction { slots -> matches(slots) xor restriction.matches(slots) }

    operator fun not() = Restriction { slots -> !matches(slots) }

    class Ordered(private vararg val keys: String) : Restriction {
        init {
            require(keys.size > 1)
        }

        override fun matches(slots: Map<String, Tag>): Boolean {
            val tags = keys.map { slots[it] ?: return false }
            return tags.asSequence().zipWithNext().all { (left, right) -> left < right }
        }

        override fun toString() = keys.joinToString(separator = " < ")
    }

    class Distance(private val distance: Int, private vararg val keys: String, private val inner: Boolean = true) : Restriction {
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