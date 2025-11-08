package weeno.pandakhoe.crops.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import weeno.pandakhoe.crops.CropType;
import weeno.pandakhoe.crops.CropsManager;

public class CropsEvents implements Listener {

    private final CropsManager manager;

    public CropsEvents(CropsManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Block b = e.getBlock();
        Material m = b.getType();

        if (m == Material.CROPS) manager.set(b, CropType.WHEAT);
        else if (m == Material.CARROT) manager.set(b, CropType.CARROT);
        else if (m == Material.POTATO) manager.set(b, CropType.POTATO);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        manager.remove(e.getBlock());
    }
}