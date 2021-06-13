package org.deepercreeper.sentience.tagger.time

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.TaggerEngine
import org.deepercreeper.sentience.tagger.token.TokenTagger
import org.deepercreeper.sentience.util.MockUtil
import kotlin.test.Test


class TimeTaggerTest {
    private val document = Document("Right now it's 12:15:21 am and we are going to tag the time 17:14. Also at 3 pm we have another example.")

    private val engine = TaggerEngine(MockUtil.context())

    private val configs = TokenTagger.configs() + TimeTagger.configs()

    @Test
    fun test() {
        engine.init(document, configs)
        engine.process()
        engine.print()
    }
}