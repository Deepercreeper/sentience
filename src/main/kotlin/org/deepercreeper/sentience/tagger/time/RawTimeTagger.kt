package org.deepercreeper.sentience.tagger.time

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.token.TokenTagger
import org.deepercreeper.sentience.tagger.value.AbstractValueTagger


class RawTimeTaggerConfig(symbolService: SymbolService) : SimpleTaggerConfig({ RawTimeTagger(it, symbolService) })

class RawTimeTagger(document: Document, symbolService: SymbolService) : AbstractValueTagger<List<Int>>(document, KEY, symbolService) {
    override val keys get() = setOf(TokenTagger.KEY)

    override val maxLength get() = 10

    override fun mappings() = (0..9).asSequence().map { "$it" }.map { it to it } + (":" to ":") + sequenceOf(".", ",").map { it to "" }

    override fun convert(text: String): List<Int>? {
        val values = text.split(':', '.').mapNotNull { it.toIntOrNull() }
        if (values.size !in 1..3) return null
        val hour = values[0]
        if (hour !in 0..23) return null
        val minute = values.getOrElse(1) { 0 }
        if (minute !in 0..59) return null
        val second = values.getOrElse(2) { 0 }
        if (second !in 0..59) return null
        return listOf(hour, minute, second)
    }

    companion object {
        const val KEY = "rawTime"
    }
}