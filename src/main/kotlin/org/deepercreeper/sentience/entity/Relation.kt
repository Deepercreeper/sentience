package org.deepercreeper.sentience.entity

import javax.persistence.*


@Entity
class Relation(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long? = null,
    @ManyToOne val left: Symbol,
    @ManyToOne val right: Symbol,
    val distance: Double = 1.0
) : Comparable<Relation> {
    val symbols get() = setOf(left, right)

    init {
        require(distance > 0)
    }

    constructor(left: Symbol, right: Symbol, distance: Double = 1.0) : this(null, left, right, distance)

    fun other(symbol: Symbol) = when (symbol) {
        left -> right
        right -> left
        else -> throw IllegalArgumentException("Symbol $symbol not part of relation $this")
    }

    operator fun component1() = left

    operator fun component2() = right

    operator fun component3() = distance

    operator fun contains(symbol: Symbol) = left == symbol || right == symbol

    override fun compareTo(other: Relation) = distance.compareTo(other.distance)

    override fun equals(other: Any?) = this === other || other is Relation && setOf(left, right) == setOf(other.left, right)

    override fun hashCode() = setOf(left, right).hashCode()

    override fun toString() = "$left <-$distance-> $right"
}

infix fun Pair<Symbol, Symbol>.distance(distance: Double) = Relation(first, second, distance)