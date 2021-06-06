package org.deepercreeper.sentience.entity.tagger.rule

import org.deepercreeper.sentience.tagger.rule.Rule
import javax.persistence.*


@Entity
class RuleEntry(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long? = null,
    @Enumerated(EnumType.STRING) val type: Rule.Type,
    @ManyToOne val rule: RuleConfig,
    @ManyToOne val parent: RuleEntry? = null,
    @ElementCollection val config: MutableMap<String, Any> = mutableMapOf()
) {
    constructor(type: Rule.Type, config: RuleConfig, parent: RuleEntry? = null) : this(null, type, config, parent)

    override fun toString() = "$type$config"
}