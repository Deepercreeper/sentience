package org.deepercreeper.sentience.service

import org.deepercreeper.sentience.entity.Relation
import org.deepercreeper.sentience.entity.Symbol
import org.deepercreeper.sentience.repository.RelationRepository
import org.deepercreeper.sentience.repository.SymbolRepository
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import kotlin.math.min


@Service
class SymbolService(private val symbolRepository: SymbolRepository, private val relationRepository: RelationRepository) {
    private val _symbols = mutableSetOf<Symbol>()

    private val _relations = mutableSetOf<Relation>()

    private val _groups = mutableMapOf<Symbol, MutableSet<Symbol>>()

    val symbols get() = _symbols.toSet()

    val relations get() = _relations.toSet()

    @PostConstruct
    private fun init() {
        symbolRepository.findAll().forEach { this += it }
        relationRepository.findAll().forEach { this += it }
    }

    fun groupOf(text: String) = get(text)?.let { groupOf(it) }?.asSequence()?.map { it.text }?.toSet() ?: setOf(text)

    fun groupOf(symbol: Symbol) = _groups[symbol]!!.toSet()

    fun neighboursOf(symbol: Symbol) = _relations.asSequence().filter { symbol in it }

    fun distance(left: Symbol, right: Symbol): Double? {
        val group = groupOf(left)
        if (right !in group) return null
        val unvisited = group.toMutableSet()
        val distances = mutableMapOf(left to 0.0)
        var current = 0.0 to left
        while (true) {
            if (current.second == right) return current.first
            neighboursOf(current.second).filter { it.other(current.second) in unvisited }.forEach { relation ->
                val other = relation.other(current.second)
                distances.compute(other) { _, distance ->
                    min(current.first + relation.distance, distance ?: Double.POSITIVE_INFINITY)
                }
            }
            unvisited -= current.second
            current = unvisited.asSequence().mapNotNull { distances[it]?.to(it) }.minByOrNull { it.first }!!
        }
    }

    @Synchronized
    operator fun get(text: String) = _symbols.firstOrNull { it.text == text }

    @Synchronized
    operator fun plusAssign(symbols: Iterable<Symbol>) = symbols.forEach { plusAssign(it) }

    @Synchronized
    operator fun plusAssign(symbol: Symbol) {
        if (!_symbols.add(symbol)) return
        added(symbol)
        symbolRepository.save(symbol)
    }

    @Synchronized
    operator fun minusAssign(symbol: Symbol) {
        if (!_symbols.remove(symbol)) return
        _relations.removeIf { symbol in it }
        _groups[symbol]!! -= symbol
        _groups -= symbol
        symbolRepository.delete(symbol)
    }

    operator fun plusAssign(relation: Pair<Symbol, Symbol>) = plusAssign(Relation(relation.first, relation.second))

    @Synchronized
    operator fun plusAssign(relation: Relation) {
        require(_symbols.containsAll(relation.symbols)) { "Relation contains unknown symbols: $relation" }
        if (!_relations.add(relation)) return
        added(relation)
        relationRepository.save(relation)
    }

    operator fun minusAssign(relation: Pair<Symbol, Symbol>) = minusAssign(Relation(relation.first, relation.second))

    @Synchronized
    operator fun minusAssign(relation: Relation) {
        if (!_relations.remove(relation)) return
        val group = _groups[relation.left]!!
        group.forEach(this::added)
        _relations.asSequence().filter { relation.left in group }.forEach(this::added)
        relationRepository.delete(relation)
    }

    private fun added(symbol: Symbol) {
        _groups[symbol] = mutableSetOf(symbol)
    }

    private fun added(relation: Relation) {
        val group = _groups[relation.left]!!
        group += _groups[relation.right]!!
        group.forEach { _groups[it] = group }
    }
}