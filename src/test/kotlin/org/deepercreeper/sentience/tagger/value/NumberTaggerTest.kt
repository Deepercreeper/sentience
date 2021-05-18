package org.deepercreeper.sentience.tagger.value

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.entity.Relation
import org.deepercreeper.sentience.entity.Symbol
import org.deepercreeper.sentience.tagger.Tagger
import org.deepercreeper.sentience.tagger.TaggerEngine
import org.deepercreeper.sentience.tagger.token.TokenTagger
import org.deepercreeper.sentience.util.MockUtil
import org.deepercreeper.sentience.util.get
import kotlin.test.Test
import kotlin.test.assertEquals


class NumberTaggerTest {
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

    private val configs = TokenTagger.configs() + NumberTagger.configs(service)

    @Test
    fun testSimple() {
        val document = Document("abc 01234 def 1ZOl23 ghi 45S67 jkl")
        val engine = TaggerEngine(document, configs)
        engine.process()
        engine.print()
        assertEquals(setOf(1234.0, 120123.0, 45567.0), engine.tags[NumberTagger.KEY].asSequence().map { it.get<Double>(Tagger.Key.VALUE) }.toSet())
    }

    @Test
    fun testHard() {
        val document = Document("1.234 123.4567 12345.67 123.456.789 12.345.6789 12.3456.78")
        val engine = TaggerEngine(document, configs)
        engine.process()
        engine.print()
        val numbers = engine.tags[NumberTagger.KEY].asSequence().map { it.get<Double>(Tagger.Key.VALUE) }.toSet()
        assertEquals(setOf(1.234, 1234.0, 123.4567, 12345.67, 123456.789, 123456789.0, 12345.6789), numbers)
    }
}