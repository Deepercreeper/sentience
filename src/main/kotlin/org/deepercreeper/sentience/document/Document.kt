package org.deepercreeper.sentience.document

import org.deepercreeper.sentience.tagger.Tag


class Document(val text: String) {
    operator fun get(tag: Tag) = text.substring(tag.start, tag.end)
}