package org.deepercreeper.sentience.service

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.entity.tagger.ConfigGroup
import org.deepercreeper.sentience.service.tagger.TaggerService
import org.deepercreeper.sentience.tagger.TaggerEngine
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service


@Service
class TagService(private val taggerService: TaggerService, private val context: ApplicationContext) {
    fun tag(document: Document, group: ConfigGroup? = null) = context.getBean<TaggerEngine>().apply {
        init(document, taggerService.configs(group))
        process()
        print()
    }.tags.tags
}