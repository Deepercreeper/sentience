package org.deepercreeper.sentience.service

import net.sourceforge.tess4j.Tesseract
import org.springframework.stereotype.Service
import java.io.File


@Service
class OcrService {
    private val tesseract = Tesseract().apply { setDatapath("src/main/resources/tessdata") }

    @Synchronized
    fun ocr(file: File, language: Language = Language.ENGLISH): String {
        tesseract.setLanguage(language.code)
        return tesseract.doOCR(file)
    }

    enum class Language(val code: String) {
        ENGLISH("eng"),

        GERMAN("deu")
    }
}