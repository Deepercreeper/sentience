package org.deepercreeper.sentience.tagger.time

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.tagger.Tagger
import org.deepercreeper.sentience.tagger.rule.AbstractConditionalTagger
import org.deepercreeper.sentience.tagger.rule.Condition
import org.deepercreeper.sentience.tagger.rule.Slots
import org.deepercreeper.sentience.util.get

class TimeTaggerConfig : SimpleTaggerConfig(::TimeTagger)

private val KEYS = setOf(HourTagger.KEY, MinuteTagger.KEY, SecondTagger.KEY, DaytimeTagger.KEY)

private val CONDITION = TimeFormat.values().asSequence().map { it.condition }.reduce(Condition::or)

//TODO This is tagging to many different interpretations of one time. Use one value tagger instead
private enum class TimeFormat {
    HOUR_MINUTE_SECOND_DAYTIME {
        override val condition = Condition.Ordered(HourTagger.KEY, MinuteTagger.KEY, SecondTagger.KEY, DaytimeTagger.KEY)

        override fun parseTag(tags: List<Tag>): Int {
            val (hourTag, minuteTag, secondTag, daytimeTag) = tags
            val daytime: Daytime = daytimeTag[Tagger.Key.VALUE]!!
            val hour: Int = daytime.map(hourTag[Tagger.Key.VALUE]!!)
            val minute: Int = minuteTag[Tagger.Key.VALUE]!!
            val second: Int = secondTag[Tagger.Key.VALUE]!!
            return ((hour * 60 + minute) * 60 + second) * 1000
        }
    },

    HOUR_MINUTE_SECOND {
        override val condition = Condition.Ordered(HourTagger.KEY, MinuteTagger.KEY, SecondTagger.KEY)

        override fun parseTag(tags: List<Tag>): Int {
            val (hourTag, minuteTag, secondTag) = tags
            val hour: Int = hourTag[Tagger.Key.VALUE]!!
            val minute: Int = minuteTag[Tagger.Key.VALUE]!!
            val second: Int = secondTag[Tagger.Key.VALUE]!!
            return ((hour * 60 + minute) * 60 + second) * 1000
        }
    },

    HOUR_MINUTE_DAYTIME {
        override val condition = Condition.Ordered(HourTagger.KEY, MinuteTagger.KEY, DaytimeTagger.KEY)

        override fun parseTag(tags: List<Tag>): Int {
            val (hourTag, minuteTag, daytimeTag) = tags
            val daytime: Daytime = daytimeTag[Tagger.Key.VALUE]!!
            val hour: Int = daytime.map(hourTag[Tagger.Key.VALUE]!!)
            val minute: Int = minuteTag[Tagger.Key.VALUE]!!
            return (hour * 60 + minute) * 60 * 1000
        }
    },

    HOUR_MINUTE {
        override val condition = Condition.Ordered(HourTagger.KEY, MinuteTagger.KEY)

        override fun parseTag(tags: List<Tag>): Int {
            val (hourTag, minuteTag) = tags
            val hour: Int = hourTag[Tagger.Key.VALUE]!!
            val minute: Int = minuteTag[Tagger.Key.VALUE]!!
            return (hour * 60 + minute) * 60 * 1000
        }
    },

    HOUR_DAYTIME {
        override val condition = Condition.Ordered(HourTagger.KEY, DaytimeTagger.KEY)

        override fun parseTag(tags: List<Tag>): Int {
            val (hourTag, daytimeTag) = tags
            val daytime: Daytime = daytimeTag[Tagger.Key.VALUE]!!
            val hour: Int = daytime.map(hourTag[Tagger.Key.VALUE]!!)
            return hour * 60 * 60 * 1000
        }
    };

    abstract val condition: Condition

    fun parseTags(slots: Slots) = condition.findAll(slots).map { Triple(parseTag(it), it.first().start, it.last().end) }

    protected abstract fun parseTag(tags: List<Tag>): Int
}

class TimeTagger(document: Document) : AbstractConditionalTagger(document) {
    override val dependencies get() = KEYS

    override val conditions get() = setOf(CONDITION)

    override val distance get() = 15

    override fun tag(slots: Slots) {
        TimeFormat.values().first { it.condition.matches(slots) }.parseTags(slots).map { (time, start, end) -> Tag(KEY, start, end - start, Key.VALUE to time) }
            .forEach { tags += it }
    }

    companion object {
        const val KEY = "time"
    }
}