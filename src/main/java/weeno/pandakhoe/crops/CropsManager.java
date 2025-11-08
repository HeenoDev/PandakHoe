package weeno.pandakhoe.crops;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import weeno.pandakhoe.PandakHoe;
import weeno.pandakhoe.crops.listener.CropsEvents;
import weeno.pandakhoe.crops.save.DataSave;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CropsManager implements Listener {

    private static final String META_KEY = "ph_crop";
    @Getter
    private final PandakHoe plugin;
    private final Map<Long, Byte> data;
    private final DataSave saver;

    public CropsManager(PandakHoe plugin) {
        this.plugin = plugin;
        this.saver = new DataSave(plugin);
        this.data = saver.load();
        saver.startAutoSave(data);
        Bukkit.getPluginManager().registerEvents(new CropsEvents(this), plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void shutdown() {
        saver.stopAutoSave();
        saver.saveSync(data);
        data.clear();
    }

    private long key(Block b) {
        return ((long) b.getWorld().getName().hashCode() << 48) |
                ((long) (b.getX() & 0xFFFF) << 32) |
                ((long) (b.getZ() & 0xFFFF) << 16) |
                (b.getY() & 0xFFFF);
    }

    public void set(Block b, CropType type) {
        long k = key(b);
        data.put(k, (byte) type.getId());
        b.setMetadata(META_KEY, new FixedMetadataValue(plugin, type.getId()));
    }

    public void remove(Block b) {
        data.remove(key(b));
        if (b.hasMetadata(META_KEY)) b.removeMetadata(META_KEY, plugin);
    }

    public Optional<CropType> get(Block b) {
        if (b.hasMetadata(META_KEY)) {
            return Optional.ofNullable(CropType.fromId(b.getMetadata(META_KEY).get(0).asInt()));
        }

        Byte id = data.get(key(b));
        return id != null ? Optional.ofNullable(CropType.fromId(id)) : Optional.empty();
    }

    public boolean isCustom(Block b) {
        return b.hasMetadata(META_KEY) || data.containsKey(key(b));
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        int cx = e.getChunk().getX();
        int cz = e.getChunk().getZ();
        String world = e.getWorld().getName();
        int worldHash = world.hashCode();

        data.forEach((k, id) -> {
            long wh = (k >> 48) & 0xFFFFL;
            if (wh != (worldHash & 0xFFFFL)) return;

            int x = (int) ((k >> 32) & 0xFFFF);
            int z = (int) ((k >> 16) & 0xFFFF);
            int y = (int) (k & 0xFFFF);

            if ((x >> 4) == cx && (z >> 4) == cz) {
                Block b = e.getWorld().getBlockAt(x, y, z);
                b.setMetadata(META_KEY, new FixedMetadataValue(plugin, id));
            }
        });
    }

}