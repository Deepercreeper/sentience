package org.deepercreeper.sentience.tagger.word

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.entity.Relation
import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.tagger.Tagger
import org.deepercreeper.sentience.tagger.TaggerConfig
import org.deepercreeper.sentience.tagger.token.TokenTagger
import java.util.*


class WordTaggerConfig(var key: String, var word: String, private val relations: Set<Relation>) : TaggerConfig {
    override fun create(document: Document) = WordTagger(key, Word(word, relations), document)
}

class WordTagger(private val key: String, private val word: Word, document: Document) : Tagger(document) {
    override fun init() = register(TokenTagger.KEY, this::process)

    private fun process(tag: Tag) {
        if (word.matches(document[tag])) tags += Tag(key, tag.start, tag.length)
    }
}

class Word(word: String, relations: Set<Relation>) {
    private val node = Node.parse(word, relations)

    fun matches(token: String) = token in node
}

private class Node {
    private val nodes = mutableMapOf<Char, Node>()

    fun addAll(text: String) {
        var node = this
        for (char in text) node = node.add(char)
    }

    fun add(char: Char) = get(char) ?: Node().also { nodes[char] = it }

    fun relate(text: String, alt: String) {
        val target = get(text) ?: return
        var node = this
        for (char in alt.dropLast(1)) node = node.add(char)
        node[alt.last()] = target
    }

    operator fun set(char: Char, node: Node) {
        nodes[char] = node
    }

    operator fun get(char: Char) = nodes[char]

    operator fun get(text: String): Node? {
        var node = this
        for (char in text) node = node[char] ?: return null
        return node
    }

    operator fun contains(char: Char) = char in nodes

    operator fun contains(text: String) = get(text) != null

    fun forEach(operation: (Node) -> Unit) {
        val nodes = LinkedList<Node>()
        val done = mutableSetOf<Node>()
        nodes += this
        while (nodes.isNotEmpty()) {
            val node = nodes.removeAt(0)
            if (node in done) continue
            operation(node)
            done += node
            nodes += node.nodes.values
        }
    }

    override fun toString() = nodes.keys.toString()

    companion object {
        fun parse(word: String, relations: Set<Relation>): Node {
            val node = Node().also { it.addAll(word) }
            relations.forEach { (left, right) ->
                node.forEach {
                    it.relate(left.text, right.text)
                    it.relate(right.text, left.text)
                }
            }
            return node
        }
    }
}