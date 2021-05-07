package org.deepercreeper.sentience.tagger.date

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.value.AbstractValueTagger

class DayTaggerConfig(symbolService: SymbolService) : SimpleTaggerConfig({ DayTagger(it, symbolService) })

private val SUFFIXES = setOf("st", "nd", "rd", "th")

class DayTagger(document: Document, symbolService: SymbolService) : AbstractValueTagger<Int>(document, KEY, symbolService) {
    override val maxLength get() = 5

    override fun mappings() = (0..9).asSequence().map { it.toString() }.map { it to it } + SUFFIXES.asSequence().map { it to "$" }

    override fun convert(text: String) = text.takeIf { it.matches(Regex("^(\\d{1,2})\\$?$")) }?.replace("$", "")?.toInt()?.takeIf { it in 1..31 }

    companion object {
        const val KEY = "day"
    }
}