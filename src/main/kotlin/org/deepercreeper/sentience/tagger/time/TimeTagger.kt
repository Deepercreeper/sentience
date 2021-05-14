package org.deepercreeper.sentience.tagger.time

import org.deepercreeper.sentience.document.Document
import org.deepercreeper.sentience.service.SymbolService
import org.deepercreeper.sentience.tagger.SimpleTaggerConfig
import org.deepercreeper.sentience.tagger.Tag
import org.deepercreeper.sentience.tagger.rule.AbstractRuleTagger
import org.deepercreeper.sentience.tagger.rule.Rule
import org.deepercreeper.sentience.tagger.rule.Status
import org.deepercreeper.sentience.util.get
import kotlin.math.max
import kotlin.math.min

class TimeTaggerConfig : SimpleTaggerConfig(::TimeTagger)

private const val DISTANCE = 10

private val TIME_WITH_DAYTIME = Rule.disjoint(RawTimeTagger.KEY, DaytimeTagger.KEY) and Rule.maxInnerDistance(DISTANCE, RawTimeTagger.KEY, DaytimeTagger.KEY)

private val TIME_WITHOUT_DAYTIME = Rule.without(RawTimeTagger.KEY, DISTANCE, DaytimeTagger.KEY)

class TimeTagger(document: Document) : AbstractRuleTagger(document, 20){
    override val rule = TIME_WITH_DAYTIME or TIME_WITHOUT_DAYTIME

    override fun tag(status: Status): Sequence<Tag> {
        val timeTag = status[RawTimeTagger.KEY].first()
        var (hour, minute, second) = timeTag.get<List<Int>>(Key.VALUE)!!
        var start = timeTag.start
        var end = timeTag.end
        val daytimeTag = status[DaytimeTagger.KEY].firstOrNull()
        if(daytimeTag != null){
           start = min(start, daytimeTag.start)
           end = max(end, daytimeTag.end)
           hour =  daytimeTag.get<Daytime>(Key.VALUE)!!.map(hour)
        }
        return sequenceOf(Tag(KEY, start, end - start, Key.VALUE to listOf(hour, minute, second)))
    }

    companion object{
        const val KEY = "time"

        fun configs(symbolService: SymbolService) = listOf(RawTimeTaggerConfig(symbolService), DaytimeTaggerConfig(symbolService), TimeTaggerConfig())
    }
}