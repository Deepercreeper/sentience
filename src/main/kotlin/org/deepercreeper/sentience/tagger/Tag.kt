package org.deepercreeper.sentience.tagger

import org.deepercreeper.sentience.util.HasValues
import org.deepercreeper.sentience.util.ValueMap
import java.util.*


class Tag private constructor(val type: Type, val key: String, val start: Int, val length: Int, values: ValueMap) : Event, HasValues by values, Comparable<Tag> {
    init {
        require(length > 0)
    }

    val end get() = start + length

    constructor(key: String, start: Int, length: Int, mappings: Map<String, Any>, type: Type = Type.TAG) : this(type, key, start, length, ValueMap(mappings))

    constructor(key: String, start: Int, length: Int, vararg mappings: Pair<String, Any>, type: Type = Type.TAG) : this(type, key, start, length, ValueMap(*mappings))

    fun distanceTo(tag: Tag) = when {
        end <= tag.start -> tag.start - end
        tag.end <= start -> start - tag.end
        else -> 0
    }

    override fun compareTo(other: Tag): Int {
        if (start != other.start) return start.compareTo(other.start)
        if (length != other.length) return length.compareTo(other.length)
        if (type != other.type) return type.compareTo(other.type)
        return key.compareTo(other.key)
    }

    override fun equals(other: Any?) = other === this || other is Tag && type == other.type && key == other.key && start == other.start && end == other.end

    override fun hashCode() = Objects.hash(type, key, start, end)

    override fun toString() = "$key[$start, $length]"

    enum class Type {
        TOKEN,
        TAG
    }
}