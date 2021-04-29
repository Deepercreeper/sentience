package org.deepercreeper.sentience.tagger.number

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.tagger.Tagger
import org.deepercreeper.sentience.tagger.TaggerConfig
import org.deepercreeper.sentience.tagger.token.TokenTagger


class NumberTaggerConfig(private val symbolService: SymbolService) : TaggerConfig {
    override fun create(document: Document) = NumberTagger(document, symbolService)
}

//TODO Make this symbol concept abstract and use it for e.g. dates, currencies, etc.
class NumberTagger(document: Document, symbolService: SymbolService) : Tagger(document) {
    private val symbols = computeSymbols(symbolService)

    private fun computeSymbols(symbolService: SymbolService): Map<Char, List<Pair<String, String>>> {
        val symbols = mutableMapOf<Char, MutableList<Pair<String, String>>>()
        //TODO Maybe add separators (e.g. ,.;') as well?
        for (digit in 0..9) symbolService.groupOf(digit.toString()).forEach { symbols.computeIfAbsent(it.first()) { mutableListOf() } += it to digit.toString() }
        return symbols
    }

    override fun init() {
        register(TokenTagger.KEY) { process(it) }
    }

    private fun process(tag: Tag) {
        val token = document[tag]
        val indices = mutableListOf(0 to "")
        while (indices.isNotEmpty()) {
            val (index, number) = indices.removeLast()
            val symbols = symbols[token[index]] ?: continue
            for ((symbol, representation) in symbols) {
                if (index + symbol.length > token.length) continue
                if (!token.regionMatches(index, symbol, 0, symbol.length)) continue
                if (index + symbol.length == token.length) {
                    tags += Tag(KEY, tag.start, tag.length, VALUE_KEY to (number + representation).toDouble())
                    return
                }
                indices += index + symbol.length to number + representation
            }
        }
    }

    companion object {
        const val KEY = "number"

        const val VALUE_KEY = "value"
    }
}