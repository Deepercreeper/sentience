package org.deepercreeper.sentience.tagger

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.deepercreeper.sentience.util.HasValues
import org.deepercreeper.sentience.util.ValueMap
import org.deepercreeper.sentience.util.readValueAs
import java.util.*

@JsonSerialize(using = Tag.Serializer::class)
@JsonDeserialize(using = Tag.Deserializer::class)
class Tag private constructor(
    val type: Type,
    val key: String,
    val start: Int,
    val length: Int,
    private val values: ValueMap
) : Event, HasValues by values, Comparable<Tag> {
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

    override fun equals(other: Any?) =
        other === this || other is Tag && type == other.type && key == other.key && start == other.start && end == other.end && values == other.values

    override fun hashCode() = Objects.hash(type, key, start, end, values)

    override fun toString() = "$key[$start, $length]$values"

    enum class Type {
        TOKEN,
        TAG
    }

    object Serializer : StdSerializer<Tag>(Tag::class.java) {
        override fun serialize(tag: Tag, generator: JsonGenerator, provider: SerializerProvider) = with(generator) {
            writeStartObject()
            writeObjectField("type", tag.type)
            writeStringField("key", tag.key)
            writeNumberField("start", tag.start)
            writeNumberField("length", tag.length)
            writeObjectField("values", tag.values)
            writeEndObject()
        }
    }

    object Deserializer : StdDeserializer<Tag>(Tag::class.java) {
        override fun deserialize(parser: JsonParser, context: DeserializationContext): Tag {
            val node: JsonNode = parser.codec.readTree(parser)
            val type = node["type"].readValueAs<Type>(parser.codec)
            val values = node["values"].readValueAs<ValueMap>(parser.codec)
            return Tag(type, node["key"].textValue(), node["start"].intValue(), node["length"].intValue(), values)
        }
    }
}