package org.deepercreeper.sentience.tagger.value

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.tagger.Tagger
import org.deepercreeper.sentience.tagger.token.TokenTagger

abstract class AbstractValueTagger<T : Any>(document: Document, private val key: String, symbolService: SymbolService) : Tagger(document) {
    private val symbols: Map<Char, List<Pair<String, String>>> = mutableMapOf<Char, MutableList<Pair<String, String>>>().apply {
        for ((symbol, representation) in mappings()) symbolService.groupOf(symbol).forEach { computeIfAbsent(it.first()) { mutableListOf() } += it to representation }
    }

    override fun init() {
        //TODO Maybe not always tokens
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
                    convert(number + representation)?.let { tags += Tag(key, tag.start, tag.length, VALUE_KEY to it) }
                    return
                }
                indices += index + symbol.length to number + representation
            }
        }
    }

    protected abstract fun mappings(): Sequence<Pair<String, String>>

    protected abstract fun convert(text: String): T?
}