package me.andy1ne0.leakblock.bungee;

import com.google.common.io.ByteStreams;
import lombok.SneakyThrows;
import me.andy1ne0.leakblock.core.AbstractSettings;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * BungeeCord implementation of the settings
 *
 * License: LGPLv3
 *
 * @author Janmm14
 * @since 2.0.0
 */
public class BungeeSettings extends AbstractSettings {

    private static final ConfigurationProvider CONFIGURATION_PROVIDER = ConfigurationProvider.getProvider(YamlConfiguration.class);

    private final LeakBlockBungee plugin;
    private final File file;
    private Configuration cfg;

    public BungeeSettings(LeakBlockBungee plugin) throws IOException {
        this.plugin = plugin;
        File dataFolder = plugin.getDataFolder();
        file = new File(dataFolder, "config.yml");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IOException("Could not create plugin data folder.");
        } else if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Could not create configuration file.");
            }
            saveDefaultConfig();
        }
        reload();
        //reset config if its an old config
        if (((int) cfg.getDouble("configversion", 0)) < 2) {
            saveDefaultConfig();
            plugin.getLogger().info("Config was reset due to new format");
        }
    }

    private void saveDefaultConfig() throws IOException {
        ByteStreams.copy(plugin.getResourceAsStream("config.yml"), new FileOutputStream(file));
    }

    @Override
    @SneakyThrows(IOException.class)
    public void reload() {
        reloadConfig();
        readConfig();
    }

    private void reloadConfig() throws IOException {
        cfg = CONFIGURATION_PROVIDER.load(file);
    }

    private void readConfig() {
        kickReason = ChatColor.translateAlternateColorCodes('&', cfg.getString("kickMessage"));
        if (kickReason.equalsIgnoreCase("default")) {
            kickReason = DEFAULT_KICK_MESSAGE;
        }
        maxFailedAttempts = cfg.getInt("maximumFailedPings");
        debug = cfg.getBoolean("debug");
        updateCheck = cfg.getBoolean("updatecheck");
    }
}
