package org.deepercreeper.sentience.tagger.token

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.*


class TokenTaggerConfig : SimpleTaggerConfig(::TokenTagger)

class TokenTagger(document: Document) : Tagger(document) {
    override fun init() = register<ProcessEvent> { process() }

    private fun process() = document.text.forEachToken(Char::isWhitespace, this::addToken)

    private fun addToken(index: Int, length: Int) {
        tags += Tag(KEY, index, length, type = Tag.Type.TOKEN)
        val token = document.text.substring(index, index + length)
        token.forEachToken({ !isLetterOrDigit() }) { tokenIndex, tokenLength -> addSubToken(token, index + tokenIndex, tokenLength) }
    }

    private fun addSubToken(token: String, index: Int, length: Int) {
        if (length < token.length) tags += Tag(KEY, index, length, type = Tag.Type.TOKEN)
    }

    private fun String.forEachToken(isSeparator: Char.() -> Boolean, operation: (Int, Int) -> Unit) {
        var length = 0
        for (index in indices) {
            val char = this[index]
            if (char.isSeparator()) {
                if (length > 0) {
                    operation(index - length, length)
                    length = 0
                }
            } else length++
        }
        if (length > 0) operation(this.length - length, length)
    }

    companion object {
        const val KEY = "token"
    }
}