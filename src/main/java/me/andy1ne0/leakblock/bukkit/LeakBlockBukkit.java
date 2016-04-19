package me.andy1ne0.leakblock.bukkit;

import me.andy1ne0.leakblock.core.IpApi;
import me.andy1ne0.leakblock.core.VersionInformation;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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
public class LeakBlockBukkit extends JavaPlugin implements Listener {

    private int failedAttempts = 0;
    private BukkitSettings settings;

    @Override
    public void onEnable() {
        settings = new BukkitSettings(this);

        if (settings.isUpdateCheck()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    getLogger().info("Checking for updates... ");
                    final String latestVersion = VersionInformation.getLatestVersion();

                    if (latestVersion == null) {
                        getLogger().warning("Could not check for updates.");
                        return;
                    }
                    if (latestVersion.equalsIgnoreCase(getDescription().getVersion())) {
                        getLogger().info("Your version is up to date.");
                    } else {
                        getLogger().info("An update is available, or will be soon. Check the Spigot forums for more information.");
                        getServer().getPluginManager().registerEvents(new UpdateListener(getDescription().getVersion(), latestVersion), LeakBlockBukkit.this);
                    }
                }
            }.runTaskAsynchronously(this);
        } else {
            getLogger().info("Update checking is disabled.");
        }

        getServer().getPluginManager().registerEvents(this, this);

        new BukkitRunnable() {
            @Override
            public void run() {
                failedAttempts = 0;
            }
        }.runTaskTimer(this, 60 * 20, 60 * 20);
    }

    @EventHandler
    public void onPlayerLogin(final AsyncPlayerPreLoginEvent evt) {
        if (evt.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        String ip = evt.getAddress().getHostAddress();
        IpApi.IpApiResponse res = IpApi.requestData(ip);
        if (res == null) {
            failedAttempts++;
            if (failedAttempts >= settings.getMaxFailedAttempts()) {
                getLogger().info("Maximum failure limit reached. Plugin terminated.");
                getServer().getPluginManager().disablePlugin(LeakBlockBukkit.this);
            }
            return;
        }

        if (IpApi.isFailAndLog(res, settings, getLogger(), ip)) {
            return;
        }
        if (IpApi.shouldBlock(res)) {
            evt.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, settings.getKickReason());
        }
    }

    private static class UpdateListener implements Listener {

        private final String message;

        public UpdateListener(String currentVersion, String latestVersion) {
            message = ChatColor.translateAlternateColorCodes('&', "&2[&aLeakBlock&2] &7An update is available. Current version: " + currentVersion + ", latest version: " + latestVersion);
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent evt) {
            if (!evt.getPlayer().hasPermission("leakblock.notifyupdate")) {
                return;
            }
            evt.getPlayer().sendMessage(message);
        }
    }
}
