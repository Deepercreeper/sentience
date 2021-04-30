package org.deepercreeper.sentience.tagger.date

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.Tagger
import org.deepercreeper.sentience.tagger.rule.RuleTagger
import org.deepercreeper.sentience.tagger.value.NumberTagger

class DateTaggerConfig : SimpleTaggerConfig(::DateTagger)

private val KEYS = setOf(DayTagger.KEY, MonthTagger.KEY, NumberTagger.KEY)

private val RESTRICTIONS = setOf(

)

class DateTagger(document: Document) : RuleTagger(
    document,
    KEY,
    KEYS,
    RESTRICTIONS,
    KEYS
) {
    companion object {
        const val KEY = "date"
    }
}