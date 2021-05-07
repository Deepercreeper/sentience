package org.deepercreeper.sentience.tagger.time

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.value.AbstractValueTagger


class DayTimeTaggerConfig(symbolService: SymbolService) : SimpleTaggerConfig({ DayTimeTagger(it, symbolService) })

class DayTimeTagger(document: Document, symbolService: SymbolService) : AbstractValueTagger<DayTime>(document, KEY, symbolService) {
    override val maxLength get() = 5

    override fun mappings() = sequenceOf("a", "p", "m").map { it to it } + sequenceOf("." to "")

    override fun convert(text: String) = DayTime.of(text)

    companion object {
        const val KEY = "dayTime"
    }

}

enum class DayTime(private val text: String) {
    AM("am") {
        override fun map(hour: Int) = when (hour) {
            12 -> 0
            else -> hour
        }
    },

    PM("pm") {
        override fun map(hour: Int) = when (hour) {
            12 -> 12
            else -> hour + 12
        }
    };

    fun matches(text: String) = this.text == text

    abstract fun map(hour: Int): Int

    companion object {
        fun of(text: String) = values().firstOrNull { it.matches(text) }
    }
}
