package me.andy1ne0.leakblock.bukkit.cache;

import lombok.RequiredArgsConstructor;
import me.andy1ne0.leakblock.bukkit.event.BukkitLeakBlockPostCheckEvent;
import me.andy1ne0.leakblock.bukkit.event.BukkitLeakBlockPreCheckEvent;
import me.andy1ne0.leakblock.core.Cache;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * A bukkit listener which saves all results to a cache and uses all cached values to prevent a lookup of the same ip
 *
 * License: LGPLv3
 *
 * @author Janmm14
 * @since 2.0.0
 */
@RequiredArgsConstructor
public class BukkitCacheListener implements Listener {

    private final Cache cache;

    @EventHandler
    public void onPreCheck(BukkitLeakBlockPreCheckEvent evt) {
        Boolean blocked = cache.get(evt.getIp());
        if (blocked == null) {
            return;
        }
        if (blocked) {
            evt.setResult(Event.Result.DENY);
        } else {
            evt.setResult(Event.Result.ALLOW);
        }
    }

    @EventHandler
    public void onPostCheck(BukkitLeakBlockPostCheckEvent evt) {
        cache.fillCache(evt.getIp(), evt.isBlocked());
    }
}
