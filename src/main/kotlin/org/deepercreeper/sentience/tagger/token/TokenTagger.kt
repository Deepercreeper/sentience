package org.deepercreeper.sentience.tagger.token

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.*


class TokenTaggerConfig : SimpleTaggerConfig(::TokenTagger)

class TokenTagger(document: Document) : Tagger(document) {
    override fun init() = register<ProcessEvent> { process() }

    private fun process() {
        val text = document.text
        var length = 0
        for (index in text.indices) {
            val char = text[index]
            if (char.isBoundary()) {
                if (length > 0) {
                    addToken(index - length, length)
                    length = 0
                }
            } else {
                length++
            }
        }
        if (length > 0) addToken(text.length - length, length)
    }

    private fun addToken(start: Int, length: Int) {
        tags += Tag(KEY, start, length, type = Tag.Type.TOKEN)
    }

    private fun Char.isBoundary() = isWhitespace()

    companion object {
        const val KEY = "token"
    }
}