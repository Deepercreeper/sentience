package org.deepercreeper.sentience.tagger

import java.util.*


class EventManager {
    private val events: MutableList<Event> = LinkedList()

    private val queue = mutableListOf<Event>()

    operator fun plusAssign(event: Event) {
        queue += event
    }

    fun poll(): Event? {
        if (queue.isNotEmpty()) {
            events.addAll(0, queue)
            queue.clear()
        }
        return if (events.isNotEmpty()) events.removeAt(0) else null
    }
}