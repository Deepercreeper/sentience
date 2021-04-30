package org.deepercreeper.sentience.tagger.date

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.entity.Relation
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.Tagger
import org.deepercreeper.sentience.tagger.word.WordTagger
import java.text.DateFormatSymbols
import java.util.*

class MonthTaggerConfig(private val relations: Set<Relation>) : SimpleTaggerConfig({ MonthTagger(it, relations) })

class MonthTagger(document: Document, relations: Set<Relation>) : Tagger(document) {
    private val symbols = DateFormatSymbols.getInstance(Locale.ENGLISH)!!

    private val taggers = (0..11).map { createTagger(it, relations) }

    private fun createTagger(month: Int, relations: Set<Relation>): Tagger {
        val long = symbols.months[month]
        val short = long.take(3)
        return WordTagger(document, KEY, setOf(long, short), relations, VALUE_KEY to month)
    }

    override fun init() = taggers.forEach { it.init(engine) }

    companion object {
        const val KEY = "month"
    }
}