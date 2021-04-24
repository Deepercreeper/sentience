package org.deepercreeper.sentience.service

import org.deepercreeper.sentience.entity.Symbol
import org.deepercreeper.sentience.entity.distance
import org.deepercreeper.sentience.repository.RelationRepository
import org.deepercreeper.sentience.repository.SymbolRepository
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.test.assertEquals


class SymbolServiceTest {
    private val symbolRepository = mock<SymbolRepository> { on { findAll() } doReturn emptyList() }

    private val relationRepository = mock<RelationRepository> { on { findAll() } doReturn emptyList() }

    private val service = SymbolService(symbolRepository, relationRepository)

    private val a = Symbol("a")
    private val b = Symbol("b")
    private val c = Symbol("c")
    private val d = Symbol("d")
    private val e = Symbol("e")

    @Test
    fun testGroupOf() {
        service += listOf(a, b, c, d, e)
        service += a to b
        service += a to c
        service += b to c
        service += d to e

        listOf(a, b, c).forEach { assertEquals(setOf(a, b, c), service.groupOf(it)) }
        listOf(d, e).forEach { assertEquals(setOf(d, e), service.groupOf(it)) }

        service += c to d

        listOf(a, b, c, d, e).forEach { assertEquals(setOf(a, b, c, d, e), service.groupOf(it)) }

        service -= b to c

        listOf(a, b, c, d, e).forEach { assertEquals(setOf(a, b, c, d, e), service.groupOf(it)) }

        service -= a to b

        assertEquals(setOf(b), service.groupOf(b))
        listOf(a, c, d, e).forEach { assertEquals(setOf(a, c, d, e), service.groupOf(it)) }

        service -= d

        assertEquals(setOf(b), service.groupOf(b))
        listOf(a, c, e).forEach { assertEquals(setOf(a, c, e), service.groupOf(it)) }

        service += d

        assertEquals(setOf(b), service.groupOf(b))
        assertEquals(setOf(d), service.groupOf(d))
        listOf(a, c, e).forEach { assertEquals(setOf(a, c, e), service.groupOf(it)) }
    }

    @Test
    fun testDistance() {
        service += listOf(a, b, c, d, e)
        service += a to b distance 0.5
        service += a to c distance 1.0
        service += b to d distance 2.0
        service += c to d distance 1.0
        service += c to e distance 2.5
        service += d to e distance 1.0

        assertEquals(0.5, service.distance(a, b))
        assertEquals(1.0, service.distance(a, c))
        assertEquals(2.0, service.distance(a, d))
        assertEquals(3.0, service.distance(a, e))
        assertEquals(1.5, service.distance(b, c))
        assertEquals(2.0, service.distance(b, d))
        assertEquals(3.0, service.distance(b, e))
        assertEquals(1.0, service.distance(c, d))
        assertEquals(2.0, service.distance(c, e))
        assertEquals(1.0, service.distance(d, e))
    }
}