package org.deepercreeper.sentience.service

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.TaggerEngine
import org.springframework.stereotype.Service


@Service
class TagService {
    fun tag(document: Document) = TaggerEngine(document, emptyList()).apply { process() }.tags.tags
}