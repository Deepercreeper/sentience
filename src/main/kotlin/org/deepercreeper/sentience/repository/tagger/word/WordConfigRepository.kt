package org.deepercreeper.sentience.repository.tagger.word

import org.deepercreeper.sentience.entity.tagger.ConfigGroup
import org.deepercreeper.sentience.entity.tagger.word.WordConfig
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query


interface WordConfigRepository : JpaRepository<WordConfig, Long> {
    @Query("SELECT KEY FROM WORD_CONFIG WHERE GROUP = :group", nativeQuery = true)
    fun findKeys(group: ConfigGroup? = null): List<Array<Any>>

    fun keys(group: ConfigGroup? = null) = findKeys(group).map { it.first() }

    fun findByGroup(group: ConfigGroup? = null): List<WordConfig>

    fun getByKey(key: String): WordConfig
}