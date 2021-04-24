package org.deepercreeper.sentience.tagger.word

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.tagger.Tagger
import org.deepercreeper.sentience.tagger.TaggerConfig
import org.deepercreeper.sentience.tagger.token.TokenTagger


class WordTaggerConfig(var key: String, var word: String, private val symbolService: SymbolService) : TaggerConfig {
    override fun create(document: Document) = WordTagger(key, Word(word, symbolService), document)
}

class WordTagger(private val key: String, private val word: Word, document: Document) : Tagger(document) {
    override fun init() = register(TokenTagger.KEY, this::process)

    private fun process(tag: Tag) {
        if (word.matches(document[tag])) tags += Tag(key, tag.start, tag.length)
    }
}

class Word(word: String, symbolService: SymbolService) {
    private val node = WordParser(word, symbolService).parse()

    fun matches(token: String): Boolean {
        //TODO Iterate simultaneously through the nodes and return true if we end on a terminal node
        return true
    }
}

private class Node(val text: String = "", val distance: Double = 0.0) {
    private val nodes = mutableMapOf<String, Node>()

    fun appendAll(text: String) {
        var node = this
        for (char in text) node = node.append(char.toString())
    }

    fun append(text: String) = Node(this.text + text).also { nodes += text to it }
}

private class WordParser(word: String, symbolService: SymbolService) {
    private val node = Node().also { it.appendAll(word) }

    fun parse(): Node {
        //TODO Expand existing nodes using relations until nothing changes or a max distance is reached

        return node
    }
}