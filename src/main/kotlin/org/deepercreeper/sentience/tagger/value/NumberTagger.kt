package org.deepercreeper.sentience.tagger.value

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.TaggerConfig
import org.deepercreeper.sentience.tagger.token.TokenTagger
import org.springframework.beans.factory.getBean


object NumberTaggerConfig : SimpleTaggerConfig({ document, context -> NumberTagger(document, context.getBean()) })

class NumberTagger(document: Document, symbolService: SymbolService) : AbstractValueTagger<Double>(document, KEY, symbolService) {
    override val keys get() = setOf(TokenTagger.KEY)

    override fun mappings() = (0..9).asSequence().map { it.toString() }.map { it to it } + sequenceOf("." to ".", "'" to "")

    override fun convert(text: String) = when (text.count { it == '.' }) {
        0 -> sequenceOf(text).mapNotNull { it.toDoubleOrNull() }
        1 -> {
            if (text.indexOf('.') == text.length - 4) sequenceOf(text, text.replace(".", "")).mapNotNull { it.toDoubleOrNull() }
            else sequenceOf(text).mapNotNull { it.toDoubleOrNull() }
        }
        else -> parseNumber(text)
    }

    private fun parseNumber(text: String): Sequence<Double> {
        val parts = text.split('.')
        for ((index, part) in parts.withIndex()) {
            when (index) {
                0 -> if (part.length > 3) return emptySequence()
                parts.size - 1 -> continue
                else -> if (part.length != 3) return emptySequence()
            }
        }
        val numbers = mutableListOf<Double>()
        (parts.dropLast(1).joinToString("") + "." + parts.last()).toDoubleOrNull()?.let { numbers += it }
        if (parts.last().length == 3) parts.joinToString("").toDoubleOrNull()?.let { numbers += it }
        return numbers.asSequence()
    }

    companion object {
        const val KEY = "number"

        fun configs(): List<TaggerConfig> = listOf(NumberTaggerConfig)
    }
}