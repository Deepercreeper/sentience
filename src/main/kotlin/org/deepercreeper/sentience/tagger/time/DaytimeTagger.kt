package org.deepercreeper.sentience.tagger.time

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.value.AbstractValueTagger


class DaytimeTaggerConfig(symbolService: SymbolService) : SimpleTaggerConfig({ DaytimeTagger(it, symbolService) })

class DaytimeTagger(document: Document, symbolService: SymbolService) : AbstractValueTagger<Daytime>(document, KEY, symbolService) {
    override val maxLength get() = 5

    override fun mappings() = sequenceOf("a", "p", "m").map { it to it } + sequenceOf("." to "")

    override fun convert(text: String) = Daytime.of(text)

    companion object {
        const val KEY = "daytime"
    }

}

enum class Daytime(private val text: String) {
    AM("am") {
        override fun map(hour: Int) = when (hour) {
            12 -> 0
            else -> hour
        }
    },

    PM("pm") {
        override fun map(hour: Int) = when (hour) {
            in 1..11 -> hour + 12
            else -> hour
        }
    };

    fun matches(text: String) = this.text == text

    abstract fun map(hour: Int): Int

    companion object {
        fun of(text: String) = values().firstOrNull { it.matches(text) }
    }
}
