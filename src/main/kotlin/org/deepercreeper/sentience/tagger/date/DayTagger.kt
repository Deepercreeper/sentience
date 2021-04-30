package org.deepercreeper.sentience.tagger.date

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.entity.Relation
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.Tagger
import org.deepercreeper.sentience.tagger.word.WordTagger
import java.text.DateFormatSymbols
import java.util.*

class DayTaggerConfig(private val relations: Set<Relation>) : SimpleTaggerConfig({ DayTagger(it, relations) })

class DayTagger(document: Document, relations: Set<Relation>) : Tagger(document) {
    private val symbols = DateFormatSymbols.getInstance(Locale.ENGLISH)!!

    private val taggers = (0..6).map { createTagger(it, relations) }

    private fun createTagger(day: Int, relations: Set<Relation>): Tagger {
        val long = symbols.weekdays[day]
        val short = long.take(3)
        val veryShort = long.take(2)
        return WordTagger(document, KEY, setOf(long, short, veryShort), relations, VALUE_KEY to day)
    }

    override fun init() = taggers.forEach { it.init(engine) }

    companion object {
        const val KEY = "day"
    }
}