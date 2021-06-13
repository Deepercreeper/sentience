package org.deepercreeper.sentience.tagger.date

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.tagger.TaggerConfig
import org.deepercreeper.sentience.tagger.rule.AbstractRuleTagger
import org.deepercreeper.sentience.tagger.rule.Rule
import org.deepercreeper.sentience.tagger.rule.Status
import org.deepercreeper.sentience.util.get
import java.time.LocalDate

object DateTaggerConfig : SimpleTaggerConfig(::DateTagger)

private val KEYS = setOf(DayTagger.KEY, MonthTagger.KEY, YearTagger.KEY)

class DateTagger(document: Document) : AbstractRuleTagger(document, 25) {
    override val rule = Rule.disjoint(KEYS)

    override fun tag(status: Status): Sequence<Tag> {
        val tags = KEYS.flatMap { status[it] }
        val day: Int = status[DayTagger.KEY].first()[Key.VALUE]!!
        val month: Int = status[MonthTagger.KEY].first()[Key.VALUE]!!
        val year: Int = status[YearTagger.KEY].first()[Key.VALUE]!!
        val date = LocalDate.of(year, month, day)
        val start = tags.minOf { it.start }
        val end = tags.maxOf { it.end }
        return sequenceOf(Tag(KEY, start, end - start, Key.VALUE to date))
    }

    companion object {
        const val KEY = "date"

        fun configs(): List<TaggerConfig> = listOf(DayTaggerConfig, MonthTaggerConfig, YearTaggerConfig, DateTaggerConfig)
    }
}