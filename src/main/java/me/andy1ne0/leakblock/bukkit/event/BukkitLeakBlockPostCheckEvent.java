package me.andy1ne0.leakblock.bukkit.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Event called after checking an ip with ip-api.com
 *
 * This allowes you to see whether the connection attempt was blocked or not based on the data from ip-api.com
 *
 * License: LGPLv3
 *
 * @author Janmm14
 * @since 2.0.0
 */
@Getter
@AllArgsConstructor
public class BukkitLeakBlockPostCheckEvent extends Event {

    private final String name;
    private final UUID uuid;
    private final String ip;
    private final boolean blocked;

    //required things for custom events
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
