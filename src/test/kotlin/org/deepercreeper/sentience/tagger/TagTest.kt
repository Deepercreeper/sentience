package org.deepercreeper.sentience.tagger

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.test.Test


class TagTest {
    private val mapper = ObjectMapper()

    @Test
    fun testSerialize() {
        val tag = Tag("key", 0, 10, "key" to "value")
        val value = mapper.writeValueAsString(tag)
        println(value)
        val result: Tag = mapper.readValue(value)
    }
}