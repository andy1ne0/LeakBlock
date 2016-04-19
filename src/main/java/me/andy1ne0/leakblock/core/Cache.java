package me.andy1ne0.leakblock.core;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic cache class without being dependant on bukkit or bungee
 *
 * License: LGPLv3
 *
 * @author Janmm14
 * @since 2.0.0
 */
public abstract class Cache {

    protected final Map<String, Boolean> cache = new ConcurrentHashMap<String, Boolean>(64, .75f, Runtime.getRuntime().availableProcessors());

    /**
     * @param ip the ip to look up
     * @return true if blocked, false if not blocked, null if not cached
     */
    public Boolean get(String ip) {
        return cache.get(ip);
    }

    public void fillCache(String ip, boolean blocked) {
        cache.put(ip, blocked);
    }

    public abstract void saveCache() throws IOException;
}
