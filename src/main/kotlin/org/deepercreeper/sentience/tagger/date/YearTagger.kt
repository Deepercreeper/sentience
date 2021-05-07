package org.deepercreeper.sentience.tagger.date

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.value.AbstractValueTagger

class YearTaggerConfig(symbolService: SymbolService) : SimpleTaggerConfig({ YearTagger(it, symbolService) })

class YearTagger(document: Document, symbolService: SymbolService) : AbstractValueTagger<Int>(document, KEY, symbolService) {
    override val maxLength get() = 4

    override fun mappings() = (0..9).asSequence().map { it.toString() }.map { it to it }

    override fun convert(text: String) = text.toIntOrNull()?.takeIf { it in 0..9999 }

    companion object {
        const val KEY = "year"
    }
}