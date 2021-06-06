package org.deepercreeper.sentience.entity

import javax.persistence.*


@Entity
class Symbol(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long? = null,
    @Column(unique = true) val text: String
) {
    constructor(text: String) : this(null, text)

    override fun equals(other: Any?) = this === other || other is Symbol && text == other.text

    override fun hashCode() = text.hashCode()

    override fun toString() = text
}