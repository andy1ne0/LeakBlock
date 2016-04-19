package me.andy1ne0.leakblock.bungee.event;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event called just before checking the ip with ip-api.com.
 *
 * Do not check and block join: setResult(BungeeLeakBlockPreCheckEvent.Result.DENY)
 * Do not check and allow join: setResult(BungeeLeakBlockPreCheckEvent.Result.ALLOW)
 *
 * License: LGPLv3
 *
 * @author Janmm14
 * @since 2.0.0
 */
@Getter
@Setter
public class BungeeLeakBlockPreCheckEvent extends Event {

    private final PendingConnection pendingConnection;
    private String kickMessage;
    private Result result = Result.DEFAULT;

    public BungeeLeakBlockPreCheckEvent(PendingConnection pendingConnection, String kickMessage) {
        this.pendingConnection = pendingConnection;
        this.kickMessage = kickMessage;
    }

    public String getIp() {
        return pendingConnection.getAddress().getAddress().getHostAddress();
    }

    public enum Result {
        DENY,
        DEFAULT,
        ALLOW
    }
}
