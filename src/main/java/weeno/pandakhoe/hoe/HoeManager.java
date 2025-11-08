package weeno.pandakhoe.hoe;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import weeno.pandakhoe.PandakHoe;
import weeno.pandakhoe.builder.ItemsBuilder;
import weeno.pandakhoe.crops.CropType;
import weeno.pandakhoe.utils.ConfigManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HoeManager implements Listener {

    private final PandakHoe plugin;
    private final ConfigManager config;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public HoeManager(PandakHoe plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        ItemStack hand = p.getItemInHand();
        if (hand == null || !hand.getType().name().contains("HOE")) return;

        NBTItem nbt = new NBTItem(hand);
        String type = nbt.getString("ph_type");
        if (type == null || !type.equals("HOE")) return;

        UUID uuid = p.getUniqueId();
        long now = System.currentTimeMillis();
        Long last = cooldowns.get(uuid);
        if (last != null && now - last < 100) return;
        cooldowns.put(uuid, now);

        Block center = e.getClickedBlock();
        int radius = nbt.getInteger("ph_radius");
        String id = nbt.getString("ph_id");

        List<Block> toBreak = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block b = center.getRelative(x, 0, z);
                if (isHarvestable(b)) toBreak.add(b);
            }
        }

        if (toBreak.isEmpty()) return;
        e.setCancelled(true);

        Map<String, Integer> broken = new HashMap<>();
        toBreak.forEach(b -> {
            String cropName = getCropName(b.getType());
            harvestCrop(b, p);
            broken.merge(cropName, 1, Integer::sum);
        });

        boolean updated = false;
        for (Map.Entry<String, Integer> entry : broken.entrySet()) {
            String crop = entry.getKey();
            int amount = entry.getValue();
            int current = nbt.getInteger("ph_prog_" + crop);
            nbt.setInteger("ph_prog_" + crop, current + amount);
            updated = true;
        }

        if (updated) {
            ItemStack newItem = nbt.getItem();
            ConfigurationSection sec = config.getItem(id);

            if (sec != null) {
                newItem = ItemsBuilder.updateLore(newItem, sec);
            }

            nbt.applyNBT(newItem);
            checkLevelUp(p, new NBTItem(newItem), id);
        }

        cooldowns.entrySet().removeIf(en -> now - en.getValue() > 5000);
    }

    private boolean isHarvestable(Block b) {
        Material m = b.getType();
        if (m != Material.CROPS && m != Material.CARROT && m != Material.POTATO && m != Material.NETHER_WARTS) return false;
        if (!plugin.getCropsManager().isCustom(b)) return false;

        byte data = b.getData();
        if (m == Material.NETHER_WARTS) return data >= 3;
        return data >= 7;
    }

    private void harvestCrop(Block b, Player p) {
        Optional<CropType> cropOpt = plugin.getCropsManager().get(b);
        if (!cropOpt.isPresent()) return;

        CropType crop = cropOpt.get();
        Material m = b.getType();

        b.setData((byte) 0);

        String itemKey = getItemKey(m, crop);
        ConfigurationSection sec = config.getItem(itemKey);
        if (sec != null) {
            ItemStack drop = ItemsBuilder.build(sec);
            b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), drop);
        }
    }

    private String getCropName(Material m) {
        if (m == Material.CROPS) return "wheat";
        if (m == Material.CARROT) return "carrot";
        if (m == Material.POTATO) return "potato";
        if (m == Material.NETHER_WARTS) return "nether_warts";
        return "unknown";
    }

    private String getItemKey(Material m, CropType crop) {
        if (crop.isMutated()) {
            if (m == Material.CROPS) return "bleMutee";
        }
        return getCropName(m);
    }

    private void checkLevelUp(Player p, NBTItem nbt, String currentId) {
        ConfigurationSection sec = config.getItem(currentId);
        if (sec == null) return;

        ConfigurationSection settings = sec.getConfigurationSection("settings");
        if (settings == null) return;

        String nextHoe = settings.getString("next-hoe", "");
        if (nextHoe.isEmpty()) return;

        ConfigurationSection req = settings.getConfigurationSection("require-to-level-up.BREAK");
        if (req == null) return;

        boolean canLevelUp = true;
        for (String crop : req.getKeys(false)) {
            String key = crop.toLowerCase();
            int required = req.getInt(crop);
            int actual = nbt.getInteger("ph_prog_" + key);
            if (actual < required) {
                canLevelUp = false;
                break;
            }
        }

        if (canLevelUp) {
            ConfigurationSection nextSec = config.getItem(nextHoe);
            if (nextSec != null) {
                ItemStack newHoe = ItemsBuilder.build(nextSec);
                p.setItemInHand(newHoe);
                p.sendMessage("§aHoe améliorée vers " + nextHoe + " !");
            }
        }
    }

    public void cleanup() {
        cooldowns.clear();
    }
}