package org.deepercreeper.sentience.repository

import org.deepercreeper.sentience.entity.Relation
import org.springframework.data.jpa.repository.JpaRepository


interface RelationRepository : JpaRepository<Relation, Long>