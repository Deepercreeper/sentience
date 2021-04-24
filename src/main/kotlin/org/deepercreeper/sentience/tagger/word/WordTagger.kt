package org.deepercreeper.sentience.tagger.word

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.tagger.Tagger
import org.deepercreeper.sentience.tagger.TaggerConfig
import org.deepercreeper.sentience.tagger.token.TokenTagger


class WordTaggerConfig(var key: String, var word: String, val symbolService: SymbolService) : TaggerConfig {
    override fun create(document: Document) = WordTagger(key, createWord(), document)

    private fun createWord(): Word {

    }
}

class WordTagger(private val key: String, private val word: Word, document: Document) : Tagger(document) {
    override fun init() = register(TokenTagger.KEY, this::process)

    private fun process(tag: Tag) {
        if (matches(document[tag])) tags += Tag(key, tag.start, tag.length)
    }

    private fun matches(token: String): Boolean {
        //TODO Parse the token using symbols and relations
        //TODO Maybe do this either lazy and cache the result in the tag or inside the token tagger
        return false
    }
}

class Word(word: String, symbolService: SymbolService) {
    private val parts = parse(word, symbolService)

    fun matches(token: String): Boolean {
        /*
         * TODO:
         *  - For each part:
         *   - Check if the part matches the current token part
         */
        return true
    }

    private fun parse(word: String, symbolService: SymbolService): List<Part> {
        /*
         * TODO:
         *  1. Cut word into parts, that are independent from each other when looking at relations
         *  2. Expand each of those parts into a complete list of possible combinations using relations
         *  3. Return a list of expanded or literal parts
         */
        return emptyList()
    }

    interface Part {
        class Literal : Part

        class Options : Part
    }
}