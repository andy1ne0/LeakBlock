package me.andy1ne0.leakblock.bungee.cache;

import me.andy1ne0.leakblock.bungee.LeakBlockBungee;
import me.andy1ne0.leakblock.core.Cache;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * A cache which can save its data through Bungee's configuration api to a file
 *
 * License: LGPLv3
 *
 * @author Janmm14
 * @since 2.0.0
 */
public class BungeeCache extends Cache {

    private static final ConfigurationProvider CONFIGURATION_PROVIDER = ConfigurationProvider.getProvider(YamlConfiguration.class);

    private final File file;
    private Configuration cfg;
    private LeakBlockBungee inst;

    public BungeeCache(LeakBlockBungee plugin) throws IOException {
        super(new ConcurrentHashMap<String, Boolean>(64, .75f, Runtime.getRuntime().availableProcessors()));
        inst = plugin;
        File dataFolder = plugin.getDataFolder();
        file = new File(dataFolder, "cache.yml");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IOException("Could not create plugin data folder.");
        } else if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Could not create cache file.");
            }
        }
        reload();
    }

    private void reload() throws IOException {
        reloadFile();
        readFile();
    }

    private void reloadFile() throws IOException {
        cfg = CONFIGURATION_PROVIDER.load(file);
    }

    private void readFile() {
        for (String ip : cfg.getKeys()) {
            super.fillCache(ip, cfg.getBoolean(ip));
        }
    }

    @Override
    public void fillCache(String ip, boolean blocked) {
        super.fillCache(ip, blocked);
        cfg.set(ip, blocked);
        try {
            saveCache();
        } catch (IOException e){
            inst.getLogger().log(Level.SEVERE, "Could not save cache. ", e);
            e.printStackTrace();
        }
    }

    @Override
    public void saveCache() throws IOException {
        CONFIGURATION_PROVIDER.save(cfg, file);
    }
}
