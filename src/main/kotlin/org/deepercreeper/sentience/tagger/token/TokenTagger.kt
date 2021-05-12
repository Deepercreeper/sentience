package org.deepercreeper.sentience.tagger.token

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.*


class TokenTaggerConfig : SimpleTaggerConfig(::TokenTagger)

class TokenTagger(document: Document) : Tagger(document) {
    override fun init() = register<ProcessEvent> { process() }

    private fun process() = document.text.forEachToken(Char::isWhitespace) { index, length -> tags += Tag(KEY, index, length, type = Tag.Type.TOKEN) }

    companion object {
        const val KEY = "token"
    }
}

class SubTokenTaggerConfig : SimpleTaggerConfig(::SubTokenTagger)

class SubTokenTagger(document: Document) : Tagger(document) {
    override fun init() = register(TokenTagger.KEY) { process(it) }

    private fun process(tag: Tag) {
        val token = document[tag]
        token.forEachToken({ !isLetterOrDigit() }) { index, length ->
            if (length < token.length) tags += Tag(KEY, tag.start + index, length, type = Tag.Type.TOKEN)
        }
    }

    companion object {
        const val KEY = "subToken"
    }
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