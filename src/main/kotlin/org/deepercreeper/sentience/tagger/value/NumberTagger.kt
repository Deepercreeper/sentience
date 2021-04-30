package org.deepercreeper.sentience.tagger.value

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig


class NumberTaggerConfig(private val symbolService: SymbolService) : SimpleTaggerConfig({ NumberTagger(it, symbolService) })

class NumberTagger(document: Document, symbolService: SymbolService) : AbstractValueTagger<Double>(document, KEY, symbolService) {
    //TODO Maybe add separators (e.g. ,.;') as well?
    override fun mappings() = (0..9).asSequence().map { it.toString() }.map { it to it }

    override fun convert(text: String) = text.toDouble()

    companion object {
        const val KEY = "number"
    }
}