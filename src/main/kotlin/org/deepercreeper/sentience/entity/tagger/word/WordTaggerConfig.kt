package org.deepercreeper.sentience.entity.tagger.word

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.entity.tagger.ConfigGroup
import org.deepercreeper.sentience.tagger.TaggerConfig
import org.deepercreeper.sentience.tagger.word.WordTagger
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import javax.persistence.*


@Entity
class WordTaggerConfig(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long? = null,
    var key: String,
    @ElementCollection var words: Set<String>,
    @ElementCollection val mappings: MutableMap<String, Any> = mutableMapOf(),
    @ManyToOne var group: ConfigGroup? = null
) : TaggerConfig {
    constructor(
        key: String,
        words: Set<String>,
        mappings: Map<String, Any> = mutableMapOf(),
        group: ConfigGroup? = null
    ) : this(null, key, words, mappings.toMutableMap(), group)

    override fun create(document: Document, context: ApplicationContext) = WordTagger(document, key, words, mappings.toMap(), context.getBean())

    override fun toString() = key
}