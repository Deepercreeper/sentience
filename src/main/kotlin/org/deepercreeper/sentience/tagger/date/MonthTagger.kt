package org.deepercreeper.sentience.tagger.date

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.Tagger
import org.deepercreeper.sentience.tagger.value.AbstractValueTagger
import org.deepercreeper.sentience.tagger.word.WordTagger
import java.text.DateFormatSymbols
import java.util.*

class MonthTaggerConfig(symbolService: SymbolService) : SimpleTaggerConfig({ MonthTagger(it, symbolService) })

class MonthTagger(document: Document, symbolService: SymbolService) : Tagger(document) {
    private val symbols = DateFormatSymbols.getInstance(Locale.ENGLISH)!!

    private val taggers = (1..12).map { createTagger(it, symbolService) } + createNumberTagger(symbolService)

    private fun createTagger(month: Int, symbolService: SymbolService): Tagger {
        val long = symbols.months[month - 1].lowercase()
        val short = long.take(3)
        return WordTagger(document, KEY, setOf(long, short), symbolService, Key.VALUE to month)
    }

    private fun createNumberTagger(symbolService: SymbolService) = object : AbstractValueTagger<Int>(document, KEY, symbolService) {
        override val maxLength get() = 2

        override fun mappings() = (0..9).asSequence().map { "$it" }.map { it to it }

        override fun convert(text: String) = text.toIntOrNull()?.takeIf { it in 1..12 }?.let { sequenceOf(it) } ?: emptySequence()
    }

    override fun init() = taggers.forEach { it.init(engine) }

    companion object {
        const val KEY = "month"
    }
}