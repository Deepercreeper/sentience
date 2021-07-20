package org.deepercreeper.sentience.entity.tagger

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.TaggerConfig
import org.deepercreeper.sentience.tagger.rule.Rule
import org.deepercreeper.sentience.tagger.rule.RuleTagger
import org.deepercreeper.sentience.util.associateAll
import org.springframework.context.ApplicationContext
import javax.persistence.*


@Entity
class RuleTaggerConfig(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long? = null,
    var key: String,
    @ElementCollection var targets: Set<String>,
    var length: Int,
    @ManyToOne var group: ConfigGroup? = null
) : TaggerConfig {

    @OneToMany(mappedBy = "rule", cascade = [CascadeType.REMOVE])
    var entries: List<RuleEntry> = emptyList()

    var rule: Rule
        get() = createRule()
        set(rule) {
            entries = rule.toEntries()
        }

    constructor(
        key: String,
        targets: Set<String>,
        length: Int,
        group: ConfigGroup? = null
    ) : this(null, key, targets, length, group)

    override fun create(document: Document, context: ApplicationContext) = RuleTagger(document, length, key, createRule(), targets)

    private fun createRule(): Rule {
        val (withParent, withoutParent) = entries.partition { it.parent != null }
        val entries = withParent.associateAll { it.parent!!.id!! to it }
        return withoutParent.firstOrNull()?.toRule(entries) ?: Rule.empty()
    }

    private fun RuleEntry.toRule(entries: Map<Long, List<RuleEntry>>): Rule {
        val children = entries[id!!] ?: emptyList()
        val rules = children.map { it.toRule(entries) }
        return type.create(config, rules)
    }

    private fun Rule.toEntries() = if (type == Rule.Type.EMPTY) emptyList() else createEntry()

    private fun Rule.createEntry(parent: RuleEntry? = null): List<RuleEntry> {
        val entry = RuleEntry(type, this@RuleTaggerConfig, parent)
        return rules.flatMap { it.createEntry(entry) } + entry
    }

    override fun toString() = key
}

@Entity
class RuleEntry(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long? = null,
    @Enumerated(EnumType.STRING) val type: Rule.Type,
    @ManyToOne val rule: RuleTaggerConfig,
    @ManyToOne val parent: RuleEntry? = null,
    @ElementCollection val config: MutableMap<String, Any> = mutableMapOf()
) {
    constructor(type: Rule.Type, config: RuleTaggerConfig, parent: RuleEntry? = null) : this(null, type, config, parent)

    override fun toString() = "$type$config"
}