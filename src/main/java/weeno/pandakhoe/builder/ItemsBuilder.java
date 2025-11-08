package weeno.pandakhoe.builder;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemsBuilder {

    public static ItemStack build(ConfigurationSection sec) {
        return build(sec, new HashMap<>());
    }

    public static ItemStack build(ConfigurationSection sec, Map<String, String> placeholders) {
        Material mat = Material.valueOf(sec.getString("material"));
        int data = sec.getInt("data", 0);

        ItemStack item = new ItemStack(mat, 1, (short) data);
        ItemMeta meta = item.getItemMeta();

        String name = sec.getString("name", "");
        meta.setDisplayName(replace(name, placeholders));

        List<String> lore = sec.getStringList("lore");
        if (!lore.isEmpty()) {
            meta.setLore(lore.stream().map(l -> replace(l, placeholders)).collect(Collectors.toList()));
        }

        if (sec.contains("hide-flags")) {
            sec.getStringList("hide-flags").forEach(f -> {
                try {
                    meta.addItemFlags(ItemFlag.valueOf(f));
                } catch (Exception ignored) {}
            });
        }

        if (sec.contains("enchants")) {
            sec.getStringList("enchants").forEach(e -> {
                String[] split = e.split(":");
                try {
                    Enchantment ench = Enchantment.getByName(split[0]);
                    int lvl = Integer.parseInt(split[1]);
                    meta.addEnchant(ench, lvl, true);
                } catch (Exception ignored) {}
            });
        }

        item.setItemMeta(meta);

        NBTItem nbt = new NBTItem(item);
        String type = sec.getString("type", "NORMAL");
        nbt.setString("ph_type", type);
        nbt.setString("ph_id", sec.getName());

        if (type.equals("HOE")) {
            ConfigurationSection settings = sec.getConfigurationSection("settings");
            if (settings != null) {
                nbt.setInteger("ph_radius", settings.getInt("radius", 1));
                nbt.setString("ph_next", settings.getString("next-hoe", ""));

                ConfigurationSection req = settings.getConfigurationSection("require-to-level-up.BREAK");
                if (req != null) {
                    req.getKeys(false).forEach(crop -> {
                        nbt.setInteger("ph_req_" + crop.toLowerCase(), req.getInt(crop));
                        nbt.setInteger("ph_prog_" + crop.toLowerCase(), 0);
                    });
                }
            }
        }

        return nbt.getItem();
    }

    public static ItemStack updateLore(ItemStack item, ConfigurationSection sec) {
        NBTItem nbt = new NBTItem(item);
        Map<String, String> placeholders = new HashMap<>();

        String type = nbt.getString("ph_type");
        if (type != null && type.equals("HOE")) {
            ConfigurationSection settings = sec.getConfigurationSection("settings");
            if (settings != null) {
                ConfigurationSection req = settings.getConfigurationSection("require-to-level-up.BREAK");
                if (req != null) {
                    int total = 0;
                    int current = 0;

                    for (String crop : req.getKeys(false)) {
                        String key = crop.toLowerCase();
                        int required = nbt.getInteger("ph_req_" + key);
                        int actual = nbt.getInteger("ph_prog_" + key);

                        placeholders.put("required_" + key, String.valueOf(required));
                        placeholders.put("actual_" + key, String.valueOf(actual));

                        double percent = required > 0 ? (actual * 100.0 / required) : 0;
                        placeholders.put("actual_" + key + "_percent", String.format("%.1f%%", percent));

                        total += required;
                        current += actual;
                    }

                    double globalPercent = total > 0 ? (current * 100.0 / total) : 0;
                    placeholders.put("global_progression_bar", createBar(globalPercent));
                }
            }

            placeholders.put("level_hoe", nbt.getString("ph_id").replace("hoe", ""));
        }

        ItemMeta meta = item.getItemMeta();
        String name = sec.getString("name", "");
        meta.setDisplayName(replace(name, placeholders));

        List<String> lore = sec.getStringList("lore");
        if (!lore.isEmpty()) {
            meta.setLore(lore.stream().map(l -> replace(l, placeholders)).collect(Collectors.toList()));
        }

        item.setItemMeta(meta);
        return item;
    }

    private static String replace(String str, Map<String, String> placeholders) {
        String result = str.replace("&", "§");
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            result = result.replace("%" + e.getKey() + "%", e.getValue());
        }
        return result;
    }

    private static String createBar(double percent) {
        int filled = (int) (percent / 10);
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? "█" : "§7█");
        }
        return bar.toString() + " §f" + String.format("%.1f%%", percent);
    }
}