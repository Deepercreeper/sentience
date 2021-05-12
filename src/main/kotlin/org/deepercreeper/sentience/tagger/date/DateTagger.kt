package org.deepercreeper.sentience.tagger.date

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.tagger.rule.AbstractConditionalTagger
import org.deepercreeper.sentience.tagger.rule.Condition
import org.deepercreeper.sentience.util.get
import java.time.LocalDate

class DateTaggerConfig : SimpleTaggerConfig(::DateTagger)

private val KEYS = setOf(DayTagger.KEY, MonthTagger.KEY, YearTagger.KEY)

private val CONDITION = Condition.Disjoint(KEYS.toList())

class DateTagger(document: Document) : AbstractConditionalTagger(document) {
    override val dependencies get() = KEYS

    override val conditions = setOf(CONDITION)

    override val distance get() = 25

    override fun tag() = CONDITION.findAll(this).map { it.associateBy(Tag::key) }.forEach { tags ->
        val day: Int = tags[DayTagger.KEY]!![Key.VALUE]!!
        val month: Int = tags[MonthTagger.KEY]!![Key.VALUE]!!
        val year: Int = tags[YearTagger.KEY]!![Key.VALUE]!!
        val date = LocalDate.of(year, month, day)
        val start = tags.values.minOf { it.start }
        val end = tags.values.maxOf { it.end }
        this.tags += Tag(KEY, start, end - start, Key.VALUE to date)
    }

    companion object {
        const val KEY = "date"

        fun configs(symbolService: SymbolService) = listOf(DayTaggerConfig(symbolService), MonthTaggerConfig(symbolService), YearTaggerConfig(symbolService), DateTaggerConfig())
    }
}