package org.deepercreeper.sentience.service.tagger

import org.deepercreeper.sentience.entity.tagger.ConfigGroup
import org.deepercreeper.sentience.entity.tagger.word.WordConfig
import org.deepercreeper.sentience.repository.tagger.word.WordConfigRepository
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.word.WordTaggerConfig
import org.springframework.stereotype.Service


@Service
class WordService(private val wordRepository: WordConfigRepository, private val symbolService: SymbolService) {
    fun keys(group: ConfigGroup? = null) = wordRepository.keys(group)

    fun configs(group: ConfigGroup? = null) = wordRepository.findByGroup(group).asSequence().map { it.parse() }

    fun config(id: Long) = wordRepository.getOne(id).parse()

    fun config(key: String) = wordRepository.getByKey(key).parse()

    fun save(id: Long, config: WordTaggerConfig) {
        val wordConfig = wordRepository.getOne(id)
        //TODO Either make the key non unique or automatically merge configs with the same key
        //TODO Same goes for the save function below
        wordConfig.set(config)
        wordRepository.save(wordConfig)
    }

    fun save(config: WordTaggerConfig, group: ConfigGroup? = null) = wordRepository.save(WordConfig(config, group)).id!!

    fun delete(id: Long) = wordRepository.deleteById(id)

    private fun WordConfig.parse() = WordTaggerConfig(key, words, emptyMap(), symbolService)
}