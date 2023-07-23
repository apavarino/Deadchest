package me.crylonz.deadchest

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList


class DeadChestPickUpEvent : Event(), Cancellable {
    private var cancelled = false

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(value: Boolean) {
        cancelled = value
    }

    companion object {
        val handlerList = HandlerList()
    }
}