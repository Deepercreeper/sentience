package org.deepercreeper.sentience.service.tagger

import org.deepercreeper.sentience.entity.tagger.ConfigGroup
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.date.DateTagger
import org.deepercreeper.sentience.tagger.time.TimeTagger
import org.deepercreeper.sentience.tagger.token.SubTokenTagger
import org.deepercreeper.sentience.tagger.token.TokenTagger
import org.deepercreeper.sentience.tagger.value.NumberTagger
import org.springframework.stereotype.Service

@Service
class TaggerService(symbolService: SymbolService, private val wordService: WordConfigService, private val ruleService: RuleConfigService) {
    private val defaultConfigs = listOf(
        TokenTagger.configs(),
        SubTokenTagger.configs(),
        NumberTagger.configs(symbolService),
        DateTagger.configs(symbolService),
        TimeTagger.configs(symbolService)
    ).flatten()

    fun configs(group: ConfigGroup? = null) = defaultConfigs + wordService.configs(group) + ruleService.configs(group)
}