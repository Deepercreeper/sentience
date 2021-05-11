package org.deepercreeper.sentience.tagger.word

import org.deepercreeper.sentience.entity.Relation
import java.util.*

class Node private constructor(private val depth: Double, private val ignoreCase: Boolean = false) {
    private val nodes = mutableMapOf<Char, MutableSet<Node>>()

    private val terminal get() = nodes.isEmpty()

    private val max: Int by lazy { (nodes.values.asSequence().flatten().map { it.max }.maxOrNull() ?: -1) + 1 }

    private val min: Int by lazy { (nodes.values.asSequence().flatten().map { it.min }.minOrNull() ?: -1) + 1 }

    private constructor(text: String, depth: Double = 0.0, ignoreCase: Boolean = false) : this(depth, ignoreCase) {
        if (text.isEmpty()) return
        val first = if(ignoreCase) text.first().lowercaseChar() else text.first()
        nodes[first] = mutableSetOf(Node(text.drop(1), depth + 1, ignoreCase))
    }

    fun matches(text: String) = text.length in min..max && matchesRecursive(text)

    private fun matchesRecursive(text: String): Boolean {
        if (terminal) return text.isEmpty()
        if (text.isEmpty()) return false
        val next = text.drop(1)
        val first = if (ignoreCase) text.first().lowercaseChar() else text.first()
        return nodes[first]?.any { it.matchesRecursive(next) } ?: false
    }

    private fun relate(text: String, alt: String) = this[text].map { relate(alt, it) }.any { it }

    private fun relate(text: String, target: Node): Boolean {
        var nodes = setOf(this)
        var index = 0
        val casedText = if(ignoreCase) text.lowercase() else text
        while (index < casedText.lastIndex) {
            val char = casedText[index]
            nodes = nodes.asSequence().flatMap { it[char] }.filter { it.depth < target.depth }.toSet().takeIf { it.isNotEmpty() } ?: break
            index++
        }
        if (index == casedText.lastIndex) {
            val last = casedText.last()
            if (target in nodes.asSequence().flatMap { it[last] }.toSet()) return false
            return nodes.first().add(last, target)
        }
        var node = nodes.first()
        val depthStep = (target.depth - node.depth) / (casedText.length - index)
        while (index < casedText.lastIndex) {
            val char = casedText[index]
            node = Node(node.depth + depthStep).also { node.add(char, it) }
            index++
        }
        node.add(casedText.last(), target)
        return true
    }

    private fun add(char: Char, node: Node) = nodes.computeIfAbsent(char) { mutableSetOf() }.add(node)

    private operator fun get(char: Char) = nodes[char] ?: emptySet()

    private operator fun get(text: String): Set<Node> {
        if (text.isEmpty()) return setOf(this)
        val next = text.drop(1)
        val first = if (ignoreCase) text.first().lowercaseChar() else text.first()
        return nodes[first]?.asSequence()?.flatMap { it[next] }?.toSet() ?: emptySet()
    }

    private operator fun contains(text: String): Boolean {
        if (text.isEmpty()) return true
        val next = text.drop(1)
        val first = if(ignoreCase) text.first().lowercaseChar() else text.first()
        return nodes[first]?.any { next in it } ?: false
    }

    private fun forEach(operation: (Node) -> Unit) {
        val nodes = LinkedList<Node>()
        val done = mutableSetOf<Node>()
        nodes += this
        while (nodes.isNotEmpty()) {
            val node = nodes.removeAt(0)
            if (node in done) continue
            operation(node)
            done += node
            nodes += node.nodes.values.flatten()
        }
    }

    override fun toString() = nodes.keys.toString()

    companion object {
        private const val MAX_ITERATIONS = 10

        fun parse(word: String, relations: Set<Relation>, ignoreCase: Boolean = false): Node {
            val node = Node(word, ignoreCase = ignoreCase)
            for (i in 1..MAX_ITERATIONS) {
                var modified = false
                relations.forEach { (left, right) ->
                    node.forEach {
                        if (it.relate(left.text, right.text)) modified = true
                        if (it.relate(right.text, left.text)) modified = true
                    }
                }
                if (!modified) break
            }
            return node
        }
    }
}