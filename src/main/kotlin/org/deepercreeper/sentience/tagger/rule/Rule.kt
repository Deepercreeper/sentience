package org.deepercreeper.sentience.tagger.rule

import org.deepercreeper.sentience.tagger.Tag
import kotlin.math.max
import kotlin.math.min

interface Rule {
    val type: Type

    val dependencies: Set<String>

    val rules: Collection<Rule> get() = emptyList()

    fun search(status: Status): Sequence<Status>

    fun config(): Map<String, Any> = emptyMap()

    infix fun and(rule: Rule) = and(this, rule)

    infix fun or(rule: Rule) = or(this, rule)

    enum class Type(private val constructor: (Map<String, Any>, List<Rule>) -> Rule) {
        AND(And::parse),
        OR(Or::parse),
        NOT(Not::parse),
        ORDERED(Ordered::parse),
        DISJOINT(Disjoint::parse),
        MAX_INNER_DISTANCE(MaxInnerDistance::parse),
        MAX_OUTER_DISTANCE(MaxOuterDistance::parse),
        WITHOUT(Without::parse),
        EMPTY({ _, _ -> Empty });

        constructor(constructor: (Map<String, Any>) -> Rule) : this({ config, _ -> constructor(config) })

        fun create(config: Map<String, Any>, rules: List<Rule>) = constructor(config, rules)
    }

    interface FilterRule : Rule {
        override fun search(status: Status) = if (check(status)) sequenceOf(status) else emptySequence()

        fun check(status: Status): Boolean

        operator fun not() = not(this)
    }

    companion object {
        fun and(vararg rules: Rule) = and(rules.toList())

        fun and(rules: List<Rule>): Rule = And(rules)

        fun or(vararg rules: Rule) = or(rules.toList())

        fun or(rules: List<Rule>): Rule = Or(rules)

        fun not(rule: FilterRule): FilterRule = Not(rule)

        fun ordered(vararg keys: String) = ordered(keys.toList())

        fun ordered(keys: List<String>): Rule = Ordered(keys, false)

        fun containsOrdered(vararg keys: String) = ordered(keys.toList())

        fun containsOrdered(keys: List<String>): Rule = Ordered(keys, true)

        fun disjoint(vararg keys: String) = disjoint(keys.toList())

        fun disjoint(keys: Iterable<String>) = disjoint(keys.toSet())

        fun disjoint(keys: Collection<String>): Rule = Disjoint(keys, false)

        fun containsDisjoint(vararg keys: String) = containsDisjoint(keys.toList())

        fun containsDisjoint(keys: Iterable<String>) = containsDisjoint(keys.toSet())

        fun containsDisjoint(keys: Collection<String>): Rule = Disjoint(keys, true)

        fun maxInnerDistance(distance: Int, vararg keys: String) = maxInnerDistance(distance, keys.toList())

        fun maxInnerDistance(distance: Int, keys: Iterable<String>) = maxInnerDistance(distance, keys.toSet())

        fun maxInnerDistance(distance: Int, keys: Collection<String>): Rule = MaxInnerDistance(distance, keys, false)

        fun containsMaxInnerDistance(distance: Int, vararg keys: String) = containsMaxInnerDistance(distance, keys.toList())

        fun containsMaxInnerDistance(distance: Int, keys: Iterable<String>) = containsMaxInnerDistance(distance, keys.toSet())

        fun containsMaxInnerDistance(distance: Int, keys: Collection<String>): Rule = MaxInnerDistance(distance, keys, true)

        fun maxOuterDistance(distance: Int, vararg keys: String) = maxOuterDistance(distance, keys.toList())

        fun maxOuterDistance(distance: Int, keys: Iterable<String>) = maxOuterDistance(distance, keys.toSet())

        fun maxOuterDistance(distance: Int, keys: Collection<String>): Rule = MaxOuterDistance(distance, keys, false)

        fun containsMaxOuterDistance(distance: Int, vararg keys: String) = containsMaxOuterDistance(distance, keys.toList())

        fun containsMaxOuterDistance(distance: Int, keys: Iterable<String>) = containsMaxOuterDistance(distance, keys.toSet())

        fun containsMaxOuterDistance(distance: Int, keys: Collection<String>): Rule = MaxOuterDistance(distance, keys, true)

        fun without(key: String, distance: Int, vararg keys: String) = without(key, distance, keys.toSet())

        fun without(key: String, distance: Int, keys: Iterable<String>) = without(key, distance, keys.toSet())

        fun without(key: String, distance: Int, keys: Set<String>): Rule = Without(key, distance, keys, false)

        fun containsWithout(key: String, distance: Int, vararg keys: String) = containsWithout(key, distance, keys.toSet())

        fun containsWithout(key: String, distance: Int, keys: Iterable<String>) = containsWithout(key, distance, keys.toSet())

        fun containsWithout(key: String, distance: Int, keys: Set<String>): Rule = Without(key, distance, keys, true)

        fun empty(): Rule = Empty
    }

    private class And(override val rules: List<Rule>) : Rule {
        init {
            require(rules.size > 1)
        }

        override val type get() = Type.AND

        override val dependencies get() = rules.asSequence().flatMap { it.dependencies }.toSet()

        override fun search(status: Status) = rules.fold(sequenceOf(status)) { statuses, rule -> statuses.flatMap { rule.search(it) } }

        override fun toString() = rules.joinToString(" and ") { "($it)" }

        companion object {
            fun parse(config: Map<String, Any>, rules: List<Rule>) = And(rules)
        }
    }

    private class Or(override val rules: List<Rule>) : Rule {
        init {
            require(rules.size > 1)
        }

        override val type get() = Type.OR

        override val dependencies get() = rules.asSequence().flatMap { it.dependencies }.toSet()

        override fun search(status: Status) = rules.asSequence().flatMap { it.search(status) }

        override fun toString() = rules.joinToString(" or ") { "($it)" }

        companion object {
            fun parse(config: Map<String, Any>, rules: List<Rule>) = Or(rules)
        }
    }

    private class Not(private val rule: FilterRule) : FilterRule {
        override val type get() = Type.NOT

        override val dependencies get() = rule.dependencies

        override val rules get() = listOf(rule)

        override fun check(status: Status) = !rule.check(status)

        override fun toString() = "not ($rule)"

        companion object {
            fun parse(config: Map<String, Any>, rules: List<Rule>) = Not(rules.first() as FilterRule)
        }
    }

    private abstract class PathRule(
        override val type: Type,
        protected val keys: Collection<String>,
        protected val contains: Boolean,
        private val display: String,
        minSize: Int = 2
    ) : Rule {
        init {
            require(keys.size >= minSize)
        }

        override val dependencies = keys.toSet()

        override fun search(status: Status): Sequence<Status> {
            val paths = keys.fold(sequenceOf(emptyList<Tag>())) { paths, key ->
                val tags = status[key]
                paths.flatMap { path -> tags.asSequence().filter { tag -> filter(path, tag) }.map { path + it } }
            }
            if (contains) return if (paths.any()) sequenceOf(status) else emptySequence()
            return paths.map { status.with(it) }
        }

        protected abstract fun filter(path: List<Tag>, tag: Tag): Boolean

        override fun toString() = display
    }

    private class Ordered(keys: List<String>, contains: Boolean) : PathRule(Type.ORDERED, keys, contains, keys.joinToString(" < ")) {
        override fun filter(path: List<Tag>, tag: Tag) = path.lastOrNull()?.end ?: 0 <= tag.start

        override fun config() = mapOf("keys" to keys, "contains" to contains)

        companion object {
            @Suppress("UNCHECKED_CAST")
            fun parse(config: Map<String, Any>) = Ordered(config["keys"]!! as List<String>, config["contains"]!! as Boolean)
        }
    }

    private class Disjoint(keys: Collection<String>, contains: Boolean) : PathRule(Type.DISJOINT, keys, contains, keys.joinToString(" <> ")) {
        override fun filter(path: List<Tag>, tag: Tag) = path.all { tag.end <= it.start || it.end <= tag.start }

        override fun config() = mapOf("keys" to keys, "contains" to contains)

        companion object {
            @Suppress("UNCHECKED_CAST")
            fun parse(config: Map<String, Any>) = Disjoint(config["keys"]!! as Collection<String>, config["contains"]!! as Boolean)
        }
    }

    private class MaxInnerDistance(
        private val distance: Int,
        keys: Collection<String>,
        contains: Boolean
    ) : PathRule(Type.MAX_INNER_DISTANCE, keys, contains, "$distance <${keys.joinToString()}>") {

        override fun filter(path: List<Tag>, tag: Tag) = path.isEmpty() || path.any { tag.distanceTo(it) <= distance }

        override fun config() = mapOf("distance" to distance, "keys" to keys, "contains" to contains)

        companion object {
            @Suppress("UNCHECKED_CAST")
            fun parse(config: Map<String, Any>) = MaxInnerDistance(config["distance"]!! as Int, config["keys"]!! as Collection<String>, config["contains"]!! as Boolean)
        }
    }

    private class MaxOuterDistance(
        private val distance: Int,
        keys: Collection<String>,
        contains: Boolean
    ) : PathRule(Type.MAX_OUTER_DISTANCE, keys, contains, "$distance >${keys.joinToString()}<") {

        override fun filter(path: List<Tag>, tag: Tag) = path.all { tag.hullSizeWith(it) <= distance }

        override fun config() = mapOf("distance" to distance, "keys" to keys, "contains" to contains)

        companion object {
            @Suppress("UNCHECKED_CAST")
            fun parse(config: Map<String, Any>) = MaxOuterDistance(config["distance"]!! as Int, config["keys"]!! as Collection<String>, config["contains"]!! as Boolean)

            private fun Tag.hullSizeWith(tag: Tag) = max(end, tag.end) - min(start, tag.start)
        }
    }

    private class Without(private val key: String, private val distance: Int, private val keys: Set<String>, private val contains: Boolean) : Rule {
        init {
            require(keys.isNotEmpty())
        }

        override val type get() = Type.WITHOUT

        override val dependencies = keys + key

        override fun search(status: Status): Sequence<Status> {
            val paths = status[key].asSequence().filter { status.position.end - it.end >= distance }
                .filter { tag -> dependencies.asSequence().flatMap { status[it].asSequence() }.none { tag.distanceTo(it) <= distance } }
            if (contains) return if (paths.any()) sequenceOf(status) else emptySequence()
            return paths.map { status.with(it) }
        }

        override fun config() = mapOf("key" to key, "distance" to distance, "keys" to keys, "contains" to contains)

        override fun toString() = "$key without $dependencies in $distance"

        companion object {
            @Suppress("UNCHECKED_CAST")
            fun parse(config: Map<String, Any>) = Without(config["key"]!! as String, config["distance"]!! as Int, config["keys"]!! as Set<String>, config["contains"]!! as Boolean)
        }
    }

    private object Empty : Rule {
        override val type get() = Type.EMPTY

        override val dependencies get() = emptySet<String>()

        override fun search(status: Status) = emptySequence<Status>()

        override fun toString() = "Empty"
    }
}