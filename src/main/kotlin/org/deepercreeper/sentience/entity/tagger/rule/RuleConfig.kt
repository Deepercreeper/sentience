package org.deepercreeper.sentience.entity.tagger.rule

import org.deepercreeper.sentience.entity.tagger.ConfigGroup
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.rule.Rule
import org.deepercreeper.sentience.tagger.rule.RuleTaggerConfig
import javax.persistence.*


@Entity
class RuleConfig(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long? = null,
    var key: String,
    //TODO Find a smart way to persist a rule in JPA
    @ElementCollection var targets: Set<String>,
    var length: Int,
    @ManyToOne var group: ConfigGroup? = null
) {
    constructor(key: String, targets: Set<String>, length: Int, group: ConfigGroup? = null) : this(null, key, targets, length, group)

    fun config(symbolService: SymbolService) = RuleTaggerConfig(key, Rule.ordered(), targets, length)

    override fun equals(other: Any?) = this === other || other is RuleConfig && key == other.key

    override fun hashCode() = key.hashCode()

    override fun toString() = key
}