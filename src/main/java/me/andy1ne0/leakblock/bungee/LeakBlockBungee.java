package me.andy1ne0.leakblock.bungee;

import lombok.Getter;
import me.andy1ne0.leakblock.bungee.cache.BungeeCache;
import me.andy1ne0.leakblock.bungee.cache.BungeeCacheListener;
import me.andy1ne0.leakblock.bungee.event.BungeeLeakBlockPostCheckEvent;
import me.andy1ne0.leakblock.bungee.event.BungeeLeakBlockPreCheckEvent;
import me.andy1ne0.leakblock.core.IpApi;
import me.andy1ne0.leakblock.core.VersionInformation;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * All contents of this file, except where specified elsewhere, is the copyrighted property of Andrew Petersen.
 * Any works derived from this must only be done so if permission is given beforehand by the author (Andrew Petersen).
 * Failure to receive proper permission to use the works provided will result in termination of any agreements between the third party and Andrew Petersen.
 * If permission to use the contents of this file are granted to you, the client party, you must not take credit and/or claim these works as your own.
 * Doing so will void any contracts, or permission given, by Andrew Petersen, unless notified otherwise.
 * This file is provided as-is, with absolutely no warranty provided. By using this file, you understand this
 * - regardless of if you have been warned or not prior to using this file, any liabilities or harm caused by this file is your problem.
 * If this file is available for public download, by intention of the author, derirative works without the consent of Andrew Petersen are allowed.
 * However, once again, you must give credit to the author of the code.
 * If this has been downloaded or distributed without the permission of the author, all permissions granted above are voided.
 * It is also required that in these circumstances, you cease to use the file/contents.
 * Created: Year: 2016, Month: 04, Day: 02, 09:33
 * Package Name: me.andy1ne0.leakblock.bungee
 * Project Name: LeakBlock
 *
 * @author Andrew Petersen (and Janmm14)
 */
public class LeakBlockBungee extends Plugin implements Listener {

    private int failedAttempts = 0;
    private BungeeSettings settings;
    @Getter
    private BungeeCache cache;

    @Override
    public void onEnable() {
        try {
            settings = new BungeeSettings(this);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Error while initializing the config, plugin stopped", ex);
            return;
        }

        if (settings.isFileCache()) {
            try {
                cache = new BungeeCache(this);
                getProxy().getPluginManager().registerListener(this, new BungeeCacheListener(cache));
            } catch (IOException ex) {
                getLogger().log(Level.WARNING, "Could not initialize the cache", ex);
            }
        }

        if (settings.isUpdateCheck()) {
            getProxy().getScheduler().runAsync(this, new Runnable() {
                @Override
                public void run() {
                    getLogger().info("Checking for updates... ");
                    String latestVersion = VersionInformation.getLatestVersion();

                    if (latestVersion == null) {
                        getLogger().warning("Could not check for updates.");
                        return;
                    }

                    if (latestVersion.equalsIgnoreCase(getDescription().getVersion())) {
                        getLogger().info("Your version is up to date.");
                    } else {
                        getLogger().info("An update is available, or will be soon. Check the Spigot forums for more information.");
                        getProxy().getPluginManager().registerListener(LeakBlockBungee.this, new UpdateListener(getDescription().getVersion(), latestVersion));
                    }
                }
            });
        } else {
            getLogger().info("Update checking is disabled. ");
        }

        getProxy().getPluginManager().registerListener(this, this);

        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                failedAttempts = 0;
            }
        }, 60, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        if (cache != null) {
            try {
                cache.saveCache();
            } catch (IOException ex) {
                getLogger().log(Level.WARNING, "Could not save the cache", ex);
            }
        }
    }

    @EventHandler
    public void onPlayerLogin(final LoginEvent evt) {
        if (evt.isCancelled()) {
            return;
        }
        final String ip = evt.getConnection().getAddress().getAddress().getHostAddress();

        BungeeLeakBlockPreCheckEvent preCheckEvent = getProxy().getPluginManager().callEvent(new BungeeLeakBlockPreCheckEvent(evt.getConnection(), settings.getKickReason()));

        if (preCheckEvent.getResult() == BungeeLeakBlockPreCheckEvent.Result.DENY) {
            evt.setCancelled(true);
            evt.setCancelReason(settings.getKickReason());
            return;
        } else if (preCheckEvent.getResult() == BungeeLeakBlockPreCheckEvent.Result.ALLOW) {
            return;
        }

        evt.registerIntent(this);

        //make really sure we complete the intent
        try {
            getProxy().getScheduler().runAsync(this, new Runnable() {
                @Override
                public void run() {
                    try {
                        IpApi.IpApiResponse res = IpApi.requestData(ip);
                        if (res == null) {
                            failedAttempts++;
                            if (failedAttempts >= settings.getMaxFailedAttempts()) {
                                getLogger().info("Maximum failure limit reached. Plugin terminated.");
                                getProxy().getPluginManager().unregisterListeners(LeakBlockBungee.this);
                            }
                        } else if (!IpApi.isFailAndLog(res, settings, getLogger(), ip)) {
                            boolean block = IpApi.shouldBlock(res);
                            if (block) {
                                evt.setCancelled(true);
                                evt.setCancelReason(settings.getKickReason());
                            }
                            getProxy().getPluginManager().callEvent(new BungeeLeakBlockPostCheckEvent(evt.getConnection(), block));
                        }
                    } finally {
                        evt.completeIntent(LeakBlockBungee.this);
                    }
                }
            });
        } catch (Throwable t) {
            evt.completeIntent(this);
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new RuntimeException(t);
            }
        }
    }

    public static class UpdateListener implements Listener {

        private final BaseComponent[] message;

        public UpdateListener(String currentVersion, String latestVersion) {
            this.message = new ComponentBuilder("")
                    .append("[").color(ChatColor.DARK_GREEN)
                    .append("LeakBlock").color(ChatColor.GREEN)
                    .append("] ").color(ChatColor.DARK_GREEN)
                    .append("An update is available. Current version: " + currentVersion + ", latest version: " + latestVersion).color(ChatColor.GRAY)
                    .create();
        }

        @EventHandler
        public void onPlayerJoin(PostLoginEvent evt) {
            ProxiedPlayer plr = evt.getPlayer();
            if (plr.hasPermission("leakblock.notifyupdate")) {
                plr.sendMessage(message);
            } else {
                for (String group : plr.getGroups()) {
                    if (group.toLowerCase().contains("admin")) {
                        plr.sendMessage(message);
                        break;
                    }
                }
            }
        }
    }
}
