package org.deepercreeper.sentience.tagger.value

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.entity.Relation
import org.deepercreeper.sentience.entity.Symbol
import org.deepercreeper.sentience.tagger.Tagger
import org.deepercreeper.sentience.tagger.TaggerEngine
import org.deepercreeper.sentience.tagger.token.TokenTaggerConfig
import org.deepercreeper.sentience.util.MockUtil
import org.deepercreeper.sentience.util.get
import kotlin.test.Test
import kotlin.test.assertEquals


class NumberTaggerTest {
    private val document = Document("abc 01234 def 1ZOl23 ghi 45S67 jkl")

    private val service = MockUtil.symbolService()

    init {
        sequenceOf(
            "0" to "O",
            "1" to "l",
            "5" to "S",
            "2" to "Z"
        ).map { (left, right) -> Relation(Symbol(left), Symbol(right)) }.forEach {
            service += it.symbols
            service += it
        }
    }

    private val engine = TaggerEngine(document, TokenTaggerConfig(), NumberTaggerConfig(service))

    @Test
    fun test() {
        engine.process()
        assertEquals(setOf(1234.0, 120123.0, 45567.0), engine.tags[NumberTagger.KEY].asSequence().map { it.get<Double>(Tagger.Key.VALUE) }.toSet())
    }
}