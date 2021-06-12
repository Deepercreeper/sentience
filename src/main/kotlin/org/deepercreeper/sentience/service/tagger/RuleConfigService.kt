package org.deepercreeper.sentience.service.tagger

import org.deepercreeper.sentience.entity.tagger.ConfigGroup
import org.deepercreeper.sentience.entity.tagger.rule.RuleConfig
import org.deepercreeper.sentience.entity.tagger.rule.RuleEntry
import org.deepercreeper.sentience.repository.tagger.rule.RuleConfigRepository
import org.deepercreeper.sentience.repository.tagger.rule.RuleEntryRepository
import org.deepercreeper.sentience.tagger.rule.Rule
import org.deepercreeper.sentience.tagger.rule.RuleTaggerConfig
import org.deepercreeper.sentience.util.associateAll
import org.deepercreeper.sentience.util.update
import org.springframework.stereotype.Service


@Service
class RuleConfigService(private val entryRepository: RuleEntryRepository, private val ruleRepository: RuleConfigRepository) {
    fun keys(group: ConfigGroup? = null) = ruleRepository.keys(group)

    fun configs(group: ConfigGroup? = null): Sequence<RuleTaggerConfig> {
        val rules = ruleRepository.findByGroup(group)
        val entries = entryRepository.findByRuleIn(rules).associateAll { it.rule.id!! to it }
        return rules.asSequence().map { it.parse(entries[it.id]!!) }
    }

    fun config(id: Long) = ruleRepository.getOne(id).parse()

    fun save(id: Long, rule: RuleTaggerConfig) {
        val ruleConfig = ruleRepository.update(id) { it.apply { this.rule = null } }
        entryRepository.deleteByRule(ruleConfig)
        val (entry, entries) = rule.rule.createEntry(ruleConfig)
        entryRepository.saveAll(entries)
        ruleConfig.rule = entry
        ruleConfig.set(rule)
        ruleRepository.save(ruleConfig)
    }

    fun save(rule: RuleTaggerConfig, group: ConfigGroup? = null): Long {
        val ruleConfig = ruleRepository.save(RuleConfig(rule, group))
        val (entry, entries) = rule.rule.createEntry(ruleConfig)
        entryRepository.saveAll(entries)
        ruleConfig.rule = entry
        return ruleRepository.save(ruleConfig).id!!
    }

    fun delete(id: Long) {
        val ruleConfig = ruleRepository.update(id) { it.apply { this.rule = null } }
        entryRepository.deleteByRule(ruleConfig)
        ruleRepository.deleteById(id)
    }

    private fun Rule.createEntry(config: RuleConfig, parent: RuleEntry? = null): Pair<RuleEntry, List<RuleEntry>> {
        val entry = RuleEntry(type, config, parent)
        return entry to rules.flatMap { it.createEntry(config, entry).second } + entry
    }

    private fun RuleConfig.parse() = parse(entryRepository.findByRule(this))

    private fun RuleConfig.parse(entries: List<RuleEntry>) = RuleTaggerConfig(key, entries.toRule(), targets, length)

    private fun List<RuleEntry>.toRule(): Rule {
        val (withoutParent, withParent) = partition { it.parent == null }
        val entries = withParent.associateAll { it.parent!!.id!! to it }
        return withoutParent.first().toRule(entries)
    }

    private fun RuleEntry.toRule(entries: Map<Long, List<RuleEntry>>): Rule {
        val children = entries[id!!] ?: return type.create(config, emptyList())
        val rules = children.map { it.toRule(entries) }
        return type.create(config, rules)
    }
}