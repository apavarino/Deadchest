package me.crylonz;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DeadchestPickUpEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final ChestData chest;
    private boolean cancelled;

    public DeadchestPickUpEvent(ChestData chest) {
        this.chest = chest;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public ChestData getChest() {
        return chest;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}