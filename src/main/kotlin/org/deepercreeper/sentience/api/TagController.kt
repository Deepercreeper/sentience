package org.deepercreeper.sentience.api

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.TagService
import org.deepercreeper.sentience.tagger.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController


@RestController("tag")
class TagController(private val tagService: TagService) {
    @PostMapping
    fun tag(document: Document): List<Tag> {
        return tagService.tag(document)
    }
}