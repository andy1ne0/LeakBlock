package me.andy1ne0.leakblock.bukkit;

import me.andy1ne0.leakblock.core.AbstractSettings;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Bukkit implementation of the settings
 *
 * License: LGPLv3
 *
 * @author Janmm14
 * @since 2.0.0
 */
public class BukkitSettings extends AbstractSettings {

    private final LeakBlockBukkit plugin;

    public BukkitSettings(LeakBlockBukkit plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        //reset config if its an old config
        if (((int) plugin.getConfig().getDouble("configversion", 0)) < 2) {
            plugin.saveResource("config.yml", true);
            plugin.getLogger().info("Config was reset due to new format");
        }
        readConfig();
    }

    @Override
    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        readConfig();
    }

    private void readConfig() {
        FileConfiguration cfg = plugin.getConfig();
        kickReason = ChatColor.translateAlternateColorCodes('&', cfg.getString("kickMessage"));
        if (kickReason.equalsIgnoreCase("default")) {
            kickReason = DEFAULT_KICK_MESSAGE;
        }
        maxFailedAttempts = cfg.getInt("maximumFailedPings");
        debug = cfg.getBoolean("debug");
        updateCheck = cfg.getBoolean("updatecheck");
    }
}
