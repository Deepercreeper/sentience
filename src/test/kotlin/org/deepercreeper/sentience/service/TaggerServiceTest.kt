package org.deepercreeper.sentience.service

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.util.MockUtil
import java.io.File
import kotlin.test.Test

class TaggerServiceTest {
    private val tagService = TagService(MockUtil.taggerService(), MockUtil.context())

    private val ocrService = OcrService()

    @Test
    fun test() {
        val text = ocrService.ocr(File("src/test/resources/image.jpg"), OcrService.Language.GERMAN)
        val document = Document(text)
        val tags = tagService.tag(document)
        println(tags)
    }
}