package weeno.pandakhoe.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import weeno.pandakhoe.PandakHoe;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {

    private final PandakHoe plugin;
    private final File file;
    private YamlConfiguration config;
    private final Map<String, ConfigurationSection> cache = new HashMap<>();

    public ConfigManager(PandakHoe plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "items.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            plugin.saveResource("items.yml", false);
        }

        try {
            config = YamlConfiguration.loadConfiguration(file);
            cache.clear();

            ConfigurationSection items = config.getConfigurationSection("items");
            if (items != null) {
                items.getKeys(false).forEach(key -> cache.put(key, items.getConfigurationSection(key)));
            }

            plugin.getLogger().info("Loaded " + cache.size() + " items");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load config", e);
        }
    }

    public ConfigurationSection getItem(String key) {
        return cache.get(key);
    }

    public Map<String, ConfigurationSection> getAllItems() {
        return cache;
    }
}