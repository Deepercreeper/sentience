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

        fun ordered(keys: List<String>) = object : Rule {
            init {
                require(keys.size > 1)
            }

            override val keys = keys.toSet()

            override fun search(status: Status): Sequence<SortedStatus> {
                return keys.fold(sequenceOf(emptyList<Tag>())) { paths, key ->
                    val tags = status[key]
                    paths.flatMap { path -> tags.asSequence().filter { path.lastOrNull()?.end ?: 0 <= it.start }.map { path + it } }
                }.map { status.with(it) }
            }

            override fun toString() = keys.joinToString(" < ")
        }

        fun disjoint(vararg keys: String) = disjoint(keys.toList())

        fun disjoint(keys: Iterable<String>) = disjoint(keys.toSet())

        fun disjoint(keys: Collection<String>) = object : Rule {
            init {
                require(keys.size > 1)
            }

            override val keys = keys.toSet()

            override fun search(status: Status): Sequence<Status> {
                return keys.fold(sequenceOf(emptyList<Tag>())) { paths, key ->
                    val tags = status[key]
                    paths.flatMap { path -> tags.asSequence().filter { tag -> path.all { tag.end <= it.start || it.end <= tag.start } }.map { path + it } }
                }.map { status.with(it) }
            }

            override fun toString() = keys.joinToString(" <> ")
        }

        fun maxInnerDistance(distance: Int, vararg keys: String) = maxInnerDistance(distance, keys.toList())

        fun maxInnerDistance(distance: Int, keys: Iterable<String>) = maxInnerDistance(distance, keys.toSet())

        fun maxInnerDistance(distance: Int, keys: Collection<String>) = object : Rule {
            init {
                require(keys.size > 1)
            }

            override val keys = keys.toSet()

            override fun search(status: Status): Sequence<Status> {
                return keys.fold(sequenceOf(emptyList<Tag>())) { paths, key ->
                    val tags = status[key]
                    paths.flatMap { path -> tags.asSequence().filter { tag -> path.isEmpty() || path.any { tag.distanceTo(it) <= distance } }.map { path + it } }
                }.map { status.with(it) }
            }

            override fun toString() = "$distance <${keys.joinToString()}>"
        }

        fun maxOuterDistance(distance: Int, vararg keys: String) = maxOuterDistance(distance, keys.toList())

        fun maxOuterDistance(distance: Int, keys: Iterable<String>) = maxOuterDistance(distance, keys.toSet())

        fun maxOuterDistance(distance: Int, keys: Collection<String>) = object : Rule {
            init {
                require(keys.size > 1)
            }

            override val keys = keys.toSet()

            override fun search(status: Status): Sequence<Status> {
                return keys.fold(sequenceOf(emptyList<Tag>())) { paths, key ->
                    val tags = status[key]
                    paths.flatMap { path -> tags.asSequence().filter { tag -> path.all { tag.hullSizeWith(it) <= distance } }.map { path + it } }
                }.map { status.with(it) }
            }

            private fun Tag.hullSizeWith(tag: Tag) = max(end, tag.end) - min(start, tag.start)

            override fun toString() = "$distance <${keys.joinToString()}>"
        }

        fun without(key: String, distance: Int, vararg keys: String) = without(key, distance, keys.toSet())

        fun without(key: String, distance: Int, keys: Iterable<String>) = without(key, distance, keys.toSet())

        fun without(key: String, distance: Int, keys: Set<String>) = object : Rule {
            init {
                require(keys.isNotEmpty())
            }

            override val keys = keys + key

            override fun search(status: Status) = status[key].asSequence().filter { status.position.end - it.end >= distance }
                .filter { tag -> keys.asSequence().flatMap { status[it].asSequence() }.none { tag.distanceTo(it) <= distance } }.map { status.with(it) }

            override fun toString() = "$key without $keys in $distance"
        }
    }
}