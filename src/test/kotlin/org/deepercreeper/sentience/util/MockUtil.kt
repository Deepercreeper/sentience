package org.deepercreeper.sentience.util

import org.deepercreeper.sentience.entity.Relation
import org.deepercreeper.sentience.repository.RelationRepository
import org.deepercreeper.sentience.repository.SymbolRepository
import org.deepercreeper.sentience.service.SymbolService
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock


object MockUtil {
    private val symbolRepository = mock<SymbolRepository> { on { findAll() } doReturn emptyList() }

    private val relationRepository = mock<RelationRepository> { on { findAll() } doReturn emptyList() }

    fun symbolService() = SymbolService(symbolRepository, relationRepository)

    fun symbolService(relations: Set<Relation>) = symbolService().apply {
        relations.asSequence().flatMap { it.symbols }.distinct().forEach { this += it }
        relations.forEach { this += it }
    }
}
