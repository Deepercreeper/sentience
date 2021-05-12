package org.deepercreeper.sentience.tagger.word

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.tagger.Tagger
import org.deepercreeper.sentience.tagger.token.TokenTagger


class WordTaggerConfig(
    var key: String,
    var words: Set<String>,
    var mappings: Map<String, Any>,
    symbolService: SymbolService
) : SimpleTaggerConfig({ WordTagger(it, key, words, mappings, symbolService) })

class WordTagger(
    document: Document,
    private val key: String,
    words: Set<String>,
    private val mappings: Map<String, Any>,
    symbolService: SymbolService,
    ignoreCase: Boolean = false
) : Tagger(document) {
    private val nodes = words.map { Node.parse(it, symbolService.relations, ignoreCase) }

    constructor (
        document: Document,
        key: String,
        mappings: Map<String, Any>,
        symbolService: SymbolService,
        vararg words: String
    ) : this(document, key, words.toSet(), mappings, symbolService)

    constructor (
        document: Document,
        key: String,
        words: Set<String>,
        symbolService: SymbolService,
        vararg mappings: Pair<String, Any>
    ) : this(document, key, words, mappings.toMap(), symbolService)

    override fun init() = register(TokenTagger.KEY) { process(it) }

    private fun process(tag: Tag) {
        val token = document[tag]
        if (nodes.any { it.matches(token) }) tags += Tag(key, tag.start, tag.length, mappings)
    }
}