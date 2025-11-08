package weeno.pandakhoe;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import weeno.pandakhoe.crops.CropsManager;
import weeno.pandakhoe.hoe.HoeManager;
import weeno.pandakhoe.utils.ConfigManager;

public final class PandakHoe extends JavaPlugin {

    @Getter private static PandakHoe instance;
    @Getter private CropsManager cropsManager;
    @Getter private ConfigManager configManager;
    @Getter private HoeManager hoeManager;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager(this);
        cropsManager = new CropsManager(this);
        hoeManager = new HoeManager(this, configManager);
    }

    @Override
    public void onDisable() {
        if (hoeManager != null) hoeManager.cleanup();
        if (cropsManager != null) cropsManager.shutdown();
    }
}