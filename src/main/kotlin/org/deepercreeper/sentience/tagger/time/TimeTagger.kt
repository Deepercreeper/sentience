package org.deepercreeper.sentience.tagger.time

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.tagger.rule.AbstractConditionalTagger
import org.deepercreeper.sentience.tagger.rule.Condition
import org.deepercreeper.sentience.tagger.rule.HasSlots
import org.deepercreeper.sentience.util.get
import kotlin.math.max
import kotlin.math.min

class TimeTaggerConfig : SimpleTaggerConfig(::TimeTagger)

private const val DISTANCE = 10

private val DISJOINT = Condition.Disjoint(RawTimeTagger.KEY, DaytimeTagger.KEY)

private val MAX_DISTANCE = Condition.MaxInnerDistance(DISTANCE, RawTimeTagger.KEY, DaytimeTagger.KEY)

private val TIME_WITH_DAYTIME = DISJOINT and MAX_DISTANCE

private val TIME_WITHOUT_DAYTIME = object : Condition {
    override fun matches(slots: HasSlots) = findAll(slots).any()

    fun findAll(slots: HasSlots): Sequence<Tag> {
        val timeTags = slots[RawTimeTagger.KEY].takeIf { it.isNotEmpty() } ?: return emptySequence()
        val daytimeTags = slots[DaytimeTagger.KEY]
        return timeTags.asSequence().filter { slots.index - it.end > DISTANCE }.filter { tag -> daytimeTags.isEmpty() || daytimeTags.minOf { tag.distanceTo(it) } > DISTANCE }
    }

    private fun Tag.distanceTo(tag: Tag) = when {
        end <= tag.start -> tag.start - end
        tag.end <= start -> start - tag.end
        else -> 0
    }
}

class TimeTagger(document: Document) : AbstractConditionalTagger(document) {
    override val dependencies get() = setOf(RawTimeTagger.KEY, DaytimeTagger.KEY)

    override val conditions get() = setOf(TIME_WITH_DAYTIME or TIME_WITHOUT_DAYTIME)

    override val distance get() = 15

    override fun tag() {
        TIME_WITHOUT_DAYTIME.findAll(this).forEach { tags += Tag(KEY, it.start, it.length, Key.VALUE to it.get<List<Int>>(Key.VALUE)!!) }
        MAX_DISTANCE.findAll(this).forEach { (timeTag, daytimeTag) ->
            var (hour, minute, second) = timeTag.get<List<Int>>(Key.VALUE)!!
            val daytime: Daytime = daytimeTag[Key.VALUE]!!
            hour = daytime.map(hour)
            val start = min(timeTag.start, daytimeTag.start)
            val end = max(timeTag.end, daytimeTag.end)
            tags += Tag(KEY, start, end - start, Key.VALUE to listOf(hour, minute, second))
        }
    }

    companion object {
        const val KEY = "time"

        fun configs(symbolService: SymbolService) = listOf(RawTimeTaggerConfig(symbolService), DaytimeTaggerConfig(symbolService), TimeTaggerConfig())
    }
}