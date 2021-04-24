package org.deepercreeper.sentience.service

import java.io.File
import kotlin.test.Test


class OcrServiceTest {
    private val service = OcrService()

    @Test
    fun testOcr() {
        println(service.ocr(File("E:/git/sentience/src/test/resources/image.jpg"), OcrService.Language.GERMAN))
    }
}