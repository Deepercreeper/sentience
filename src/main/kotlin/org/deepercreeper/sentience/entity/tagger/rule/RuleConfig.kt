package org.deepercreeper.sentience.entity.tagger.rule

import org.deepercreeper.sentience.entity.tagger.ConfigGroup
import org.deepercreeper.sentience.tagger.rule.RuleTaggerConfig
import javax.persistence.*


@Entity
class RuleConfig(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long? = null,
    var key: String,
    @ElementCollection var targets: Set<String>,
    var length: Int,
    @OneToOne var rule: RuleEntry? = null,
    @ManyToOne var group: ConfigGroup? = null
) {
    constructor(key: String, targets: Set<String>, length: Int, rule: RuleEntry? = null, group: ConfigGroup? = null) : this(null, key, targets, length, rule, group)

    constructor(rule: RuleTaggerConfig, group: ConfigGroup? = null): this(rule.key, rule.targets, rule.length, group = group)

    fun set(rule: RuleTaggerConfig) {
        key = rule.key
        targets = rule.targets
        length = rule.length
    }

    override fun toString() = key
}