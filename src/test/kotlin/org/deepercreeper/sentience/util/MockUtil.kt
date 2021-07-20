package org.deepercreeper.sentience.util

import org.deepercreeper.sentience.entity.Relation
import org.deepercreeper.sentience.repository.RelationRepository
import org.deepercreeper.sentience.repository.SymbolRepository
import org.deepercreeper.sentience.repository.tagger.RuleTaggerConfigRepository
import org.deepercreeper.sentience.repository.tagger.WordTaggerConfigRepository
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.service.tagger.TaggerService
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.springframework.context.ApplicationContext


object MockUtil {
    private val symbolRepository = mock<SymbolRepository> { on { findAll() } doReturn emptyList() }

    private val relationRepository = mock<RelationRepository> { on { findAll() } doReturn emptyList() }

    private val ruleRepository = mock<RuleTaggerConfigRepository> { on { findByGroup(anyOrNull()) } doReturn emptyList() }

    private val wordRepository = mock<WordTaggerConfigRepository> { on { findByGroup(anyOrNull()) } doReturn emptyList() }

    fun symbolService() = SymbolService(symbolRepository, relationRepository)

    fun symbolService(relations: Iterable<Relation>) = symbolService().apply {
        relations.asSequence().flatMap { it.symbols }.distinct().forEach { this += it }
        relations.forEach { this += it }
    }

    fun taggerService() = TaggerService(wordRepository, ruleRepository)

    fun context(symbolService: SymbolService? = null) = mock<ApplicationContext> { on { getBean(SymbolService::class.java) } doReturn (symbolService ?: symbolService()) }
}
