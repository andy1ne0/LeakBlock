package me.andy1ne0.leakblock.bukkit.cache;

import me.andy1ne0.leakblock.bukkit.LeakBlockBukkit;
import me.andy1ne0.leakblock.core.Cache;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * A cache which can save its data through Bukkit's configuration api to a file
 *
 * License: LGPLv3
 *
 * @author Janmm14
 * @since 2.0.0
 */
public class BukkitCache extends Cache {

    private final File file;
    private YamlConfiguration cfg;
    private LeakBlockBukkit inst;

    public BukkitCache(LeakBlockBukkit plugin) throws IOException {
        super(new HashMap<String, Boolean>(64));
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
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    private void readFile() {
        for (String ip : cfg.getKeys(false)) {
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
        cfg.save(file);
    }
}
