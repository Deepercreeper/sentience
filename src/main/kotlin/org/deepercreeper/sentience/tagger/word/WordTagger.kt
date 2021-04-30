package org.deepercreeper.sentience.tagger.word

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.entity.Relation
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.tagger.Tagger
import org.deepercreeper.sentience.tagger.token.TokenTagger


class WordTaggerConfig(
    var key: String,
    var words: Set<String>,
    var mappings: Map<String, Any>,
    private val relations: Set<Relation>
) : SimpleTaggerConfig({ WordTagger(it, key, words, mappings, relations) })

class WordTagger(
    document: Document,
    private val key: String,
    words: Set<String>,
    private val mappings: Map<String, Any>,
    relations: Set<Relation>,
    ignoreCase: Boolean = false
) : Tagger(document) {
    private val nodes = words.map { Node.parse(it, relations, ignoreCase) }

    constructor (
        document: Document,
        key: String,
        mappings: Map<String, Any>,
        relations: Set<Relation>,
        vararg words: String
    ) : this(document, key, words.toSet(), mappings, relations)

    constructor (
        document: Document,
        key: String,
        words: Set<String>,
        relations: Set<Relation>,
        vararg mappings: Pair<String, Any>
    ) : this(document, key, words, mappings.toMap(), relations)

    override fun init() = register(TokenTagger.KEY, this::process)

    private fun process(tag: Tag) {
        val token = document[tag]
        if (nodes.any { it.matches(token) }) tags += Tag(key, tag.start, tag.length, mappings)
    }
}