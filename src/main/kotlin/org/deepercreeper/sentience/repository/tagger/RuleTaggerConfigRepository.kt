package org.deepercreeper.sentience.repository.tagger

import org.deepercreeper.sentience.entity.tagger.ConfigGroup
import org.deepercreeper.sentience.entity.tagger.RuleTaggerConfig
import org.deepercreeper.sentience.util.associateAll
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query


interface RuleTaggerConfigRepository : JpaRepository<RuleTaggerConfig, Long> {
    @Query("SELECT ID, KEY FROM RULE_CONFIG WHERE GROUP = :group", nativeQuery = true)
    fun findKeys(group: ConfigGroup? = null): List<Array<Any>>

    fun keys(group: ConfigGroup? = null) = findKeys(group).associateAll { (id, key) -> key as String to id as Long }

    fun findByGroup(group: ConfigGroup? = null): List<RuleTaggerConfig>
}