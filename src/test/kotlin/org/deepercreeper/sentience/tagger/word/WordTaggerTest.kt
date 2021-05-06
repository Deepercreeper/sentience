package org.deepercreeper.sentience.tagger.word

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.entity.Relation
import org.deepercreeper.sentience.entity.Symbol
import org.deepercreeper.sentience.tagger.TaggerEngine
import org.deepercreeper.sentience.tagger.token.TokenTaggerConfig
import org.deepercreeper.sentience.util.MockUtil
import kotlin.test.Test
import kotlin.test.assertEquals


class WordTaggerTest {
    private val document = Document("abc lockbar bckbar lodcbar def bdcbar bdcloar ghi lockbdr jkl bckbdr lodcbdr bdcbdr mno bdclodr pqr")

    private val relations = sequenceOf(
        "lo" to "b",
        "ck" to "dc",
        "a" to "d"
    ).map { (left, right) -> Relation(Symbol(left), Symbol(right)) }.toSet()

    private val service = MockUtil.symbolService(relations)

    private val engine = TaggerEngine(document, TokenTaggerConfig(), WordTaggerConfig("word", setOf("lockbar"), emptyMap(), service))

    @Test
    fun test() {
        engine.process()
        assertEquals(10, engine.tags["word"].size)
    }
}

class NodeTest {
    @Test
    fun test() {
        val relations = sequenceOf(
            "cx" to "dy",
            "ad" to "be",
            "ey" to "fx",
            "bf" to "ac"
        ).map { (left, right) -> Relation(Symbol(left), Symbol(right)) }.toSet()
        Node.parse("acx", relations)
    }
}