package org.deepercreeper.sentience.util

import org.deepercreeper.sentience.repository.RelationRepository
import org.deepercreeper.sentience.repository.SymbolRepository
import org.deepercreeper.sentience.service.SymbolService
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock


object MockUtil {
    private val symbolRepository = mock<SymbolRepository> { on { findAll() } doReturn emptyList() }

    private val relationRepository = mock<RelationRepository> { on { findAll() } doReturn emptyList() }

    fun symbolService() = SymbolService(symbolRepository, relationRepository)
}
