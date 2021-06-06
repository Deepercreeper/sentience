package org.deepercreeper.sentience.entity.tagger.word

import org.deepercreeper.sentience.entity.tagger.ConfigGroup
import org.deepercreeper.sentience.tagger.word.WordTaggerConfig
import javax.persistence.*


@Entity
class WordConfig(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long? = null,
    var key: String,
    @ElementCollection var words: Set<String>,
    @ElementCollection val mappings: MutableMap<String, Any> = mutableMapOf(),
    @ManyToOne var group: ConfigGroup? = null
) {
    constructor(
        key: String,
        words: Set<String>,
        mappings: Map<String, Any> = mutableMapOf(),
        group: ConfigGroup? = null
    ) : this(null, key, words, mappings.toMutableMap(), group)

    constructor(config: WordTaggerConfig, group: ConfigGroup? = null) : this(config.key, config.words, config.mappings, group)

    fun set(word: WordTaggerConfig) {
        key = word.key
        words = word.words
    }

    override fun equals(other: Any?) = this === other || other is WordConfig && key == other.key

    override fun hashCode() = key.hashCode()

    override fun toString() = key
}