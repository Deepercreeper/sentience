package org.deepercreeper.sentience.service.tagger

import org.deepercreeper.sentience.entity.tagger.ConfigGroup
import org.deepercreeper.sentience.repository.tagger.rule.RuleTaggerConfigRepository
import org.deepercreeper.sentience.repository.tagger.word.WordTaggerConfigRepository
import org.deepercreeper.sentience.tagger.date.DateTagger
import org.deepercreeper.sentience.tagger.time.TimeTagger
import org.deepercreeper.sentience.tagger.token.SubTokenTagger
import org.deepercreeper.sentience.tagger.token.TokenTagger
import org.deepercreeper.sentience.tagger.value.NumberTagger
import org.springframework.stereotype.Service

@Service
class TaggerService(private val wordRepository: WordTaggerConfigRepository, private val ruleRepository: RuleTaggerConfigRepository) {
    private val defaultConfigs = listOf(
        TokenTagger.configs(),
        SubTokenTagger.configs(),
        NumberTagger.configs(),
        DateTagger.configs(),
        TimeTagger.configs()
    ).flatten()

    fun configs(group: ConfigGroup? = null) = defaultConfigs + wordRepository.findByGroup(group) + ruleRepository.findByGroup(group)
}