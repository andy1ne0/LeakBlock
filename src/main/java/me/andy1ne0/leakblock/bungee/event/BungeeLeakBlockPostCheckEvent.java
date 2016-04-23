package me.andy1ne0.leakblock.bungee.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Event;

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
public class BungeeLeakBlockPostCheckEvent extends Event {

    private final PendingConnection connection;
    private final boolean blocked;

    public String getIp() {
        return connection.getAddress().getAddress().getHostAddress();
    }
}
