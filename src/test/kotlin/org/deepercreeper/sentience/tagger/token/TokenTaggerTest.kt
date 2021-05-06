package org.deepercreeper.sentience.tagger.token

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.Tagger
import org.deepercreeper.sentience.tagger.TaggerEngine
import kotlin.test.Test
import kotlin.test.assertEquals


class TokenTaggerTest {
    private val document = Document("Here we have a few exa.mple tokens")

    private val tokens = document.text.split(' ')

    private val counter = object : Tagger(document) {
        var count = 0

        override fun init() {
            register(TokenTagger.KEY) { count++ }
        }
    }

    private val engine = TaggerEngine(document, TokenTaggerConfig(), SimpleTaggerConfig { counter })

    @Test
    fun test() {
        engine.process()
        engine.print()
        assertEquals(tokens.size, counter.count)
        assertEquals(tokens, engine.tags.tags.map { document[it] })
    }
}