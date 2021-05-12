package org.deepercreeper.sentience.service

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.TaggerEngine
import org.springframework.stereotype.Service


@Service
class TagService(private val taggerService: TaggerService) {
    fun tag(document: Document) = TaggerEngine(document, taggerService.configs).apply { process() }.tags.tags
}