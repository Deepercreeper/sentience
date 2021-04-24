package org.deepercreeper.sentience.repository

import org.deepercreeper.sentience.entity.Symbol
import org.springframework.data.jpa.repository.JpaRepository


interface SymbolRepository: JpaRepository<Symbol, Long>