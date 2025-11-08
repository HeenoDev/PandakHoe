package weeno.pandakhoe;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import weeno.pandakhoe.crops.CropsManager;

public final class PandakHoe extends JavaPlugin {

    @Getter private static PandakHoe instance;
    @Getter private CropsManager cropsManager;

    @Override
    public void onEnable() {
        instance = this;
        cropsManager = new CropsManager(this);
    }

    @Override
    public void onDisable() {
        if (cropsManager != null) cropsManager.shutdown();
    }
}