package org.deepercreeper.sentience.service.tagger

import org.deepercreeper.sentience.entity.tagger.ConfigGroup
import org.deepercreeper.sentience.entity.tagger.RuleTaggerConfig
import org.deepercreeper.sentience.repository.tagger.RuleTaggerConfigRepository
import org.springframework.stereotype.Service


@Service
class RuleConfigService(private val ruleRepository: RuleTaggerConfigRepository) {
    fun keys(group: ConfigGroup? = null) = ruleRepository.keys(group)

    fun configs(group: ConfigGroup? = null) = ruleRepository.findByGroup(group).asSequence()

    fun config(id: Long) = ruleRepository.getOne(id)

    fun save(rule: RuleTaggerConfig) = ruleRepository.save(rule)

    fun delete(id: Long) = ruleRepository.deleteById(id)
}