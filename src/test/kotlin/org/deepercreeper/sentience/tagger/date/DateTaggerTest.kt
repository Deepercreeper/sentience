package org.deepercreeper.sentience.tagger.date

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.TaggerEngine
import org.deepercreeper.sentience.tagger.token.TokenTagger
import org.deepercreeper.sentience.util.MockUtil
import kotlin.test.Test


class DateTaggerTest {
    private val document = Document("Today is the 20th of february 2021 and we are going to tag the date")

    private val engine = TaggerEngine(MockUtil.context())

    private val configs = TokenTagger.configs() + DateTagger.configs()

    @Test
    fun test() {
        engine.init(document, configs)
        engine.process()
        engine.print()
    }
}