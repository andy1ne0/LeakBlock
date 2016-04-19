package me.andy1ne0.leakblock.core;

/**
 * Settings interface which does not depend on bukkit or bungee
 *
 * License: LGPLv3
 *
 * @author Janmm14
 * @since 2.0.0
 */
public interface Settings {

    String DEFAULT_KICK_MESSAGE = "§4Prohibited proxy detected. \nPlease rejoin without using a proxy/VPN/Alt-generating server. ";

    String getKickReason();

    int getMaxFailedAttempts();

    boolean isDebug();

    boolean isUpdateCheck();

    boolean isFileCache();

    void reload();
}
