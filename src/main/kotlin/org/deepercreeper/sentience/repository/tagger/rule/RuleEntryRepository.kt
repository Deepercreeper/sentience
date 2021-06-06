package org.deepercreeper.sentience.repository.tagger.rule

import org.deepercreeper.sentience.entity.tagger.ConfigGroup
import org.deepercreeper.sentience.entity.tagger.rule.RuleConfig
import org.deepercreeper.sentience.entity.tagger.rule.RuleEntry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query


interface RuleEntryRepository : JpaRepository<RuleEntry, Long> {
    fun findByRuleIn(rules: Collection<RuleConfig>): List<RuleEntry>

    fun findByRule(rule: RuleConfig): List<RuleEntry>

    fun deleteByRule(rule: RuleConfig)
}