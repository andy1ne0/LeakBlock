package me.andy1ne0.leakblock.bukkit.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Event called just before checking the ip with ip-api.com.
 *
 * Do not check and block join: setResult(Event.Result.DENY)
 * Do not check and allow join: setResult(Event.Result.ALLOW)
 *
 * License: LGPLv3
 *
 * @author Janmm14
 * @since 2.0.0
 */
@Getter
@Setter
public class BukkitLeakBlockPreCheckEvent extends Event {

    private final String name;
    private final UUID uuid;
    private final String ip;
    private String kickMessage;
    private Event.Result result = Event.Result.DEFAULT;

    public BukkitLeakBlockPreCheckEvent(String name, UUID uuid, String ip, String kickMessage) {
        this.name = name;
        this.uuid = uuid;
        this.ip = ip;
        this.kickMessage = kickMessage;
    }

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
