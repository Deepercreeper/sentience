package org.deepercreeper.sentience.tagger.date

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.TaggerEngine
import org.deepercreeper.sentience.tagger.token.TokenTaggerConfig
import org.deepercreeper.sentience.util.MockUtil
import kotlin.test.Test


class DateTaggerTest {
    private val document = Document("Today is the 20th of february 2021 and we are going to tag the date")

    private val service = MockUtil.symbolService()

    private val engine = TaggerEngine(document, TokenTaggerConfig(), DayTaggerConfig(service), MonthTaggerConfig(service), YearTaggerConfig(service), DateTaggerConfig())

    @Test
    fun test() {
        engine.process()
        engine.print()
    }
}