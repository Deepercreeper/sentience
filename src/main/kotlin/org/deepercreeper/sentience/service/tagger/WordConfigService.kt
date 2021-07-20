package org.deepercreeper.sentience.service.tagger

import org.deepercreeper.sentience.entity.tagger.ConfigGroup
import org.deepercreeper.sentience.entity.tagger.WordTaggerConfig
import org.deepercreeper.sentience.repository.tagger.WordTaggerConfigRepository
import org.springframework.stereotype.Service


@Service
class WordConfigService(private val wordRepository: WordTaggerConfigRepository) {
    fun keys(group: ConfigGroup? = null) = wordRepository.keys(group)

    fun configs(group: ConfigGroup? = null) = wordRepository.findByGroup(group).asSequence()

    fun config(id: Long) = wordRepository.getOne(id)

    fun save(config: WordTaggerConfig) = wordRepository.save(config)

    fun delete(id: Long) = wordRepository.deleteById(id)
}