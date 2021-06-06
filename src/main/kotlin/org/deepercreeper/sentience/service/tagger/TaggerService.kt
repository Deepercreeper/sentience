package org.deepercreeper.sentience.service.tagger

import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.TaggerConfig
import org.deepercreeper.sentience.tagger.date.DateTagger
import org.deepercreeper.sentience.tagger.time.TimeTagger
import org.deepercreeper.sentience.tagger.token.SubTokenTagger
import org.deepercreeper.sentience.tagger.token.TokenTagger
import org.deepercreeper.sentience.tagger.value.NumberTagger
import org.springframework.stereotype.Service

@Service
class TaggerService(symbolService: SymbolService) {
    private val defaultConfigs = listOf(
        TokenTagger.configs(),
        SubTokenTagger.configs(),
        NumberTagger.configs(symbolService),
        DateTagger.configs(symbolService),
        TimeTagger.configs(symbolService)
    ).flatten()

    private val _configs = mutableListOf<TaggerConfig>()

    val configs get() = defaultConfigs + _configs

}