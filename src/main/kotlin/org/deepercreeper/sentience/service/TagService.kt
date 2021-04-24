package org.deepercreeper.sentience.service

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.tagger.TaggerEngine
import org.springframework.stereotype.Service


@Service
class TagService {
    fun tag(document: Document): List<Tag> {
        val engine = TaggerEngine(document, emptyList())
        engine.process()
        return engine.tags
    }
}