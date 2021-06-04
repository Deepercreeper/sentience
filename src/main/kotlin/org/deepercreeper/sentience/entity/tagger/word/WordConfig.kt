package org.deepercreeper.sentience.entity.tagger.word

import org.deepercreeper.sentience.entity.tagger.ConfigGroup
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.word.WordTaggerConfig
import java.util.*
import javax.persistence.*


@Entity
class WordConfig(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long? = null,
    var key: String,
    @ElementCollection var words: Set<String>,
    @ManyToOne var group: ConfigGroup? = null
) {
    constructor(key: String, words: Set<String>, group: ConfigGroup? = null) : this(null, key, words, group)

    fun config(symbolService: SymbolService) = WordTaggerConfig(key, words, emptyMap(), symbolService)

    override fun equals(other: Any?) = this === other || other is WordConfig && key == other.key && words == other.words

    override fun hashCode() = Objects.hash(key, words)

    override fun toString() = key
}