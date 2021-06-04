package org.deepercreeper.sentience.entity.tagger

import javax.persistence.*


@Entity
class ConfigGroup(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long? = null,
    @Column(unique = true) var name: String
) {
    constructor(name: String) : this(null, name)

    override fun equals(other: Any?) = this === other || other is ConfigGroup && name == other.name

    override fun hashCode() = name.hashCode()

    override fun toString() = name
}