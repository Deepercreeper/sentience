package org.deepercreeper.sentience.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id


@Entity
class Symbol(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long? = null,
    val text: String
) {
    constructor(text: String) : this(null, text)

    override fun equals(other: Any?) = this === other || other is Symbol && text == other.text

    override fun hashCode() = text.hashCode()

    override fun toString() = text
}