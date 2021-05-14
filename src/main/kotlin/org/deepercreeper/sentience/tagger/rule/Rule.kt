package org.deepercreeper.sentience.tagger.rule

import org.deepercreeper.sentience.tagger.Tag
import kotlin.math.max
import kotlin.math.min

interface Rule {
    val keys: Set<String>

    fun search(status: Status): Sequence<Status>

    infix fun and(rule: Rule) = and(this, rule)

    infix fun or(rule: Rule) = or(this, rule)

    interface FilterRule : Rule {
        override fun search(status: Status) = if (check(status)) sequenceOf(status) else emptySequence()

        fun check(status: Status): Boolean

        operator fun not() = not(this)
    }

    companion object {
        fun and(vararg rules: Rule) = and(rules.toList())

        fun and(rules: List<Rule>) = object : Rule {
            init {
                require(rules.size > 1)
            }

            override val keys get() = rules.asSequence().flatMap { it.keys }.toSet()

            override fun search(status: Status) = rules.fold(sequenceOf(status)) { statuses, rule -> statuses.flatMap { rule.search(it) } }

            override fun toString() = rules.joinToString(" and ") { "($it)" }
        }

        fun or(vararg rules: Rule) = or(rules.toList())

        fun or(rules: List<Rule>) = object : Rule {
            init {
                require(rules.size > 1)
            }

            override val keys get() = rules.asSequence().flatMap { it.keys }.toSet()

            override fun search(status: Status) = rules.asSequence().flatMap { it.search(status) }

            override fun toString() = rules.joinToString(" or ") { "($it)" }
        }

        fun not(rule: FilterRule) = object : FilterRule {
            override val keys get() = rule.keys

            override fun check(status: Status) = !rule.check(status)

            override fun toString() = "not ($rule)"
        }

        fun ordered(vararg keys: String) = ordered(keys.toList())

        fun ordered(keys: List<String>) = ordered(keys, false)

        fun containsOrdered(vararg keys: String) = ordered(keys.toList())

        fun containsOrdered(keys: List<String>) = ordered(keys, true)

        private fun ordered(keys: List<String>, contains: Boolean) =
            paths(keys, contains, keys.joinToString(" < ")) { path, tag -> path.lastOrNull()?.end ?: 0 <= tag.start }

        fun disjoint(vararg keys: String) = disjoint(keys.toList())

        fun disjoint(keys: Iterable<String>) = disjoint(keys.toSet())

        fun disjoint(keys: Collection<String>) = disjoint(keys, false)

        fun containsDisjoint(vararg keys: String) = containsDisjoint(keys.toList())

        fun containsDisjoint(keys: Iterable<String>) = containsDisjoint(keys.toSet())

        fun containsDisjoint(keys: Collection<String>) = disjoint(keys, true)

        private fun disjoint(keys: Collection<String>, contains: Boolean) =
            paths(keys, contains, keys.joinToString(" <> ")) { path, tag -> path.all { tag.end <= it.start || it.end <= tag.start } }

        fun maxInnerDistance(distance: Int, vararg keys: String) = maxInnerDistance(distance, keys.toList())

        fun maxInnerDistance(distance: Int, keys: Iterable<String>) = maxInnerDistance(distance, keys.toSet())

        fun maxInnerDistance(distance: Int, keys: Collection<String>) = maxInnerDistance(distance, keys, false)

        fun containsMaxInnerDistance(distance: Int, vararg keys: String) = containsMaxInnerDistance(distance, keys.toList())

        fun containsMaxInnerDistance(distance: Int, keys: Iterable<String>) = containsMaxInnerDistance(distance, keys.toSet())

        fun containsMaxInnerDistance(distance: Int, keys: Collection<String>) = maxInnerDistance(distance, keys, true)

        private fun maxInnerDistance(distance: Int, keys: Collection<String>, contains: Boolean) =
            paths(keys, contains, "$distance <${keys.joinToString()}>") { path, tag -> path.isEmpty() || path.any { tag.distanceTo(it) <= distance } }

        fun maxOuterDistance(distance: Int, vararg keys: String) = maxOuterDistance(distance, keys.toList())

        fun maxOuterDistance(distance: Int, keys: Iterable<String>) = maxOuterDistance(distance, keys.toSet())

        fun maxOuterDistance(distance: Int, keys: Collection<String>) = maxOuterDistance(distance, keys, false)

        fun containsMaxOuterDistance(distance: Int, vararg keys: String) = containsMaxOuterDistance(distance, keys.toList())

        fun containsMaxOuterDistance(distance: Int, keys: Iterable<String>) = containsMaxOuterDistance(distance, keys.toSet())

        fun containsMaxOuterDistance(distance: Int, keys: Collection<String>) = maxOuterDistance(distance, keys, true)

        private fun maxOuterDistance(distance: Int, keys: Collection<String>, contains: Boolean) =
            paths(keys, contains, "$distance <${keys.joinToString()}>") { path, tag -> path.all { tag.hullSizeWith(it) <= distance } }

        private fun Tag.hullSizeWith(tag: Tag) = max(end, tag.end) - min(start, tag.start)

        private fun interface PathFilter {
            operator fun invoke(path: List<Tag>, tag: Tag): Boolean
        }

        private fun paths(keys: Collection<String>, contains: Boolean, display: String, minSize: Int = 2, filter: PathFilter) = object : Rule {
            init {
                require(keys.size >= minSize)
            }

            override val keys = keys.toSet()

            override fun search(status: Status): Sequence<Status> {
                val paths = keys.fold(sequenceOf(emptyList<Tag>())) { paths, key ->
                    val tags = status[key]
                    paths.flatMap { path -> tags.asSequence().filter { tag -> filter(path, tag) }.map { path + it } }
                }
                if (contains) return if (paths.any()) sequenceOf(status) else emptySequence()
                return paths.map { status.with(it) }
            }

            override fun toString() = display
        }

        fun without(key: String, distance: Int, vararg keys: String) = without(key, distance, keys.toSet())

        fun without(key: String, distance: Int, keys: Iterable<String>) = without(key, distance, keys.toSet())

        fun without(key: String, distance: Int, keys: Set<String>) = without(key, distance, keys, false)

        fun containsWithout(key: String, distance: Int, vararg keys: String) = containsWithout(key, distance, keys.toSet())

        fun containsWithout(key: String, distance: Int, keys: Iterable<String>) = containsWithout(key, distance, keys.toSet())

        fun containsWithout(key: String, distance: Int, keys: Set<String>) = without(key, distance, keys, true)

        private fun without(key: String, distance: Int, keys: Set<String>, contains: Boolean) = object : Rule {
            init {
                require(keys.isNotEmpty())
            }

            override val keys = keys + key

            override fun search(status: Status): Sequence<Status> {
                val paths = status[key].asSequence().filter { status.position.end - it.end >= distance }
                    .filter { tag -> keys.asSequence().flatMap { status[it].asSequence() }.none { tag.distanceTo(it) <= distance } }
                if (contains) return if (paths.any()) sequenceOf(status) else emptySequence()
                return paths.map { status.with(it) }
            }

            override fun toString() = "$key without $keys in $distance"
        }
    }
}