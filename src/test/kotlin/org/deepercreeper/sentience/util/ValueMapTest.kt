package org.deepercreeper.sentience.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.test.Test


class ValueMapTest {
    private val mapper = ObjectMapper()

    @Test
    fun testSerialize(){
        val map = ValueMap("string" to "value", "int" to 5)
        val string = mapper.writeValueAsString(map)
        println(string)
        val result = mapper.readValue<ValueMap>(string)
        println(result)
    }
}