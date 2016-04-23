package me.andy1ne0.leakblock.bungee.cache;

import lombok.RequiredArgsConstructor;
import me.andy1ne0.leakblock.bungee.event.BungeeLeakBlockPostCheckEvent;
import me.andy1ne0.leakblock.bungee.event.BungeeLeakBlockPreCheckEvent;
import me.andy1ne0.leakblock.core.Cache;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * A bungee listener which saves all results to a cache and uses all cached values to prevent a lookup of the same ip
 *
 * License: LGPLv3
 *
 * @author Janmm14
 * @since 2.0.0
 */
@RequiredArgsConstructor
public class BungeeCacheListener implements Listener {

    private final Cache cache;

    @EventHandler
    public void onPreCheck(BungeeLeakBlockPreCheckEvent evt) {
        Boolean blocked = cache.get(evt.getIp());
        if (blocked == null) {
            return;
        }
        if (blocked) {
            evt.setResult(BungeeLeakBlockPreCheckEvent.Result.DENY);
        } else {
            evt.setResult(BungeeLeakBlockPreCheckEvent.Result.ALLOW);
        }
    }

    @EventHandler
    public void onPostCheck(BungeeLeakBlockPostCheckEvent evt) {
        cache.fillCache(evt.getIp(), evt.isBlocked());
    }
}
