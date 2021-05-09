package org.deepercreeper.sentience.tagger.time

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.value.AbstractValueTagger

class HourTaggerConfig(symbolService: SymbolService) : SimpleTaggerConfig({ HourTagger(it, symbolService) })

class HourTagger(document: Document, symbolService: SymbolService) : AbstractValueTagger<Int>(document, KEY, symbolService) {
    override val maxLength get() = 2

    override fun mappings() = (0..9).asSequence().map { it.toString() }.map { it to it }

    override fun convert(text: String) = text.toIntOrNull()?.takeIf { it in 0..23 }

    companion object {
        const val KEY = "hour"
    }
}